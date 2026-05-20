package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgCorpus;
import com.example.kgplatform.mapper.KgCorpusMapper;
import com.example.kgplatform.service.KgCorpusService;
import com.example.kgplatform.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Tag(name = "语料管理")
@RestController
@RequestMapping("/api/corpus")
public class CorpusController extends ServiceImpl<KgCorpusMapper, KgCorpus> {

    private final KgCorpusService kgCorpusService;
    private final MinioService minioService;

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    public CorpusController(KgCorpusService kgCorpusService, MinioService minioService) {
        this.kgCorpusService = kgCorpusService;
        this.minioService = minioService;
    }

    @Operation(summary = "分页查询语料")
    @GetMapping("/list")
    public R<PageResult<KgCorpus>> list(PageQuery query) {
        return R.ok(kgCorpusService.pageQuery(query));
    }

    @Operation(summary = "获取语料详情")
    @GetMapping("/{id}")
    public R<KgCorpus> getById(@PathVariable Long id) {
        return R.ok(kgCorpusService.getById(id));
    }

    @Operation(summary = "新增语料")
    @PostMapping
    public R<Void> add(@RequestBody KgCorpus corpus) {
        corpus.setStatus("1");
        kgCorpusService.save(corpus);
        return R.ok();
    }

    @Operation(summary = "修改语料")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody KgCorpus corpus) {
        corpus.setId(id);
        kgCorpusService.updateById(corpus);
        return R.ok();
    }

    @Operation(summary = "删除语料")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        kgCorpusService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "上传语料文件")
    @PostMapping("/upload")
    public R<KgCorpus> uploadFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "graphId", required = false) Long graphId) {
        if (file.isEmpty()) {
            return R.fail("请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        String fileType = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        String newFileName = UUID.randomUUID() + "." + fileType;

        try {
            String storedPath;
            try {
                storedPath = minioService.uploadFile(file, "corpus/" + newFileName);
            } catch (Exception e) {
                Path uploadDir = Paths.get(uploadPath, "corpus");
                Files.createDirectories(uploadDir);
                Path filePath = uploadDir.resolve(newFileName);
                file.transferTo(filePath.toFile());
                storedPath = "/api/corpus/file/" + newFileName;
            }

            KgCorpus corpus = new KgCorpus();
            corpus.setName(name != null && !name.isEmpty() ? name : originalFilename);
            corpus.setFilePath(storedPath);
            corpus.setFileType(fileType);
            corpus.setFileSize(file.getSize());
            corpus.setStatus("1");
            if (graphId != null) {
                corpus.setGraphId(graphId);
            }
            kgCorpusService.save(corpus);
            return R.ok(corpus);
        } catch (IOException e) {
            return R.fail("文件保存失败: " + e.getMessage());
        }
    }

    @Operation(summary = "下载语料文件")
    @GetMapping("/file/{filename}")
    public byte[] downloadFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(uploadPath, "corpus", filename);
        if (Files.exists(filePath)) {
            return Files.readAllBytes(filePath);
        }
        return new byte[0];
    }
}
