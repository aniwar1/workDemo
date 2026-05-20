package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgMultimodalData;
import com.example.kgplatform.service.KgGraphService;
import com.example.kgplatform.service.KgMultimodalDataService;
import com.example.kgplatform.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "多模态数据更新")
@RestController
@RequestMapping("/api/data/multimodal")
public class MultimodalDataController {

    private final KgMultimodalDataService dataService;
    private final MinioService minioService;
    private final KgGraphService graphService;

    public MultimodalDataController(KgMultimodalDataService dataService,
                                   MinioService minioService,
                                   KgGraphService graphService) {
        this.dataService = dataService;
        this.minioService = minioService;
        this.graphService = graphService;
    }

    @Operation(summary = "分页查询多模态数据")
    @GetMapping("/list")
    public R<PageResult<KgMultimodalData>> list(PageQuery query,
                                                 @RequestParam(required = false) Long graphId,
                                                 @RequestParam(required = false) String dataType) {
        PageResult<KgMultimodalData> result = dataService.pageQuery(query);
        if (graphId != null) {
            result.getList().removeIf(item -> !graphId.equals(item.getGraphId()));
            result.setTotal(result.getList().size());
        }
        if (dataType != null && !dataType.isBlank()) {
            result.getList().removeIf(item -> !dataType.equals(item.getDataType()));
            result.setTotal(result.getList().size());
        }
        return R.ok(result);
    }

    @Operation(summary = "获取多模态数据详情")
    @GetMapping("/{id}")
    public R<KgMultimodalData> getById(@PathVariable Long id) {
        return R.ok(dataService.getById(id));
    }

    @Operation(summary = "上传多模态数据")
    @PostMapping("/upload")
    public R<KgMultimodalData> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(required = false) Long graphId,
                                       @RequestParam(required = false) String dataType,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) Long nodeId) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            return R.fail("文件名不能为空");
        }

        String ext = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = filename.substring(dotIndex + 1).toLowerCase();
        }

        String detectedType = detectType(ext);
        if (dataType != null && !dataType.isBlank()) {
            detectedType = dataType;
        }

        String objectName = "multimodal/" + System.currentTimeMillis() + "_" + filename;
        String url = minioService.uploadFile(file, objectName);

        KgMultimodalData data = new KgMultimodalData();
        data.setGraphId(graphId);
        data.setDataType(detectedType);
        data.setSourceUrl(url);
        data.setLocalPath(objectName);
        data.setDescription(description);
        data.setStatus("uploaded");
        data.setCreateTime(LocalDateTime.now());
        data.setUpdateTime(LocalDateTime.now());

        if (nodeId != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("nodeId", nodeId);
            data.setMetadata(toJson(metadata));
        }

        dataService.save(data);
        return R.ok(data);
    }

    @Operation(summary = "修改多模态数据信息")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id,
                          @RequestParam(required = false) String description,
                          @RequestParam(required = false) Long graphId,
                          @RequestParam(required = false) String status) {
        KgMultimodalData data = dataService.getById(id);
        if (data == null) {
            return R.fail("记录不存在");
        }
        if (description != null) {
            data.setDescription(description);
        }
        if (graphId != null) {
            data.setGraphId(graphId);
        }
        if (status != null) {
            data.setStatus(status);
        }
        data.setUpdateTime(LocalDateTime.now());
        dataService.updateById(data);
        return R.ok();
    }

    @Operation(summary = "删除多模态数据")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dataService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "关联多模态数据到图谱节点")
    @PostMapping("/{id}/link-node")
    public R<Void> linkToNode(@PathVariable Long id,
                              @RequestParam Long nodeId,
                              @RequestParam(required = false) String metadata) {
        KgMultimodalData data = dataService.getById(id);
        if (data == null) {
            return R.fail("记录不存在");
        }
        Map<String, Object> metaMap = new HashMap<>();
        metaMap.put("nodeId", nodeId);
        if (metadata != null && !metadata.isBlank()) {
            metaMap.put("extra", metadata);
        }
        data.setMetadata(toJson(metaMap));
        data.setUpdateTime(LocalDateTime.now());
        dataService.updateById(data);
        return R.ok();
    }

    @Operation(summary = "获取支持的多模态数据类型")
    @GetMapping("/types")
    public R<List<String>> getTypes() {
        return R.ok(List.of("image", "video", "audio", "text", "document", "3d_model"));
    }

    private String detectType(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg" -> "image";
            case "mp4", "avi", "mov", "mkv", "webm" -> "video";
            case "mp3", "wav", "ogg", "flac", "aac" -> "audio";
            case "pdf", "doc", "docx", "txt", "md" -> "document";
            case "obj", "stl", "fbx", "gltf", "glb" -> "3d_model";
            default -> "text";
        };
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            Object v = entry.getValue();
            if (v instanceof Number) {
                sb.append(v);
            } else {
                sb.append("\"").append(v).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
