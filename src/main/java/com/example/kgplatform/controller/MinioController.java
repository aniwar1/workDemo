package com.example.kgplatform.controller;

import com.example.kgplatform.common.R;
import com.example.kgplatform.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "MinIO 文件管理")
@RestController
@RequestMapping("/api/file/minio")
public class MinioController {

    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @Operation(summary = "上传文件到 MinIO")
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file,
                            @RequestParam(value = "prefix", defaultValue = "uploads") String prefix) {
        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }
        String ext = "";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = prefix + "/" + UUID.randomUUID() + ext;
        String url = minioService.uploadFile(file, objectName);
        return R.ok(url);
    }

    @Operation(summary = "获取预签名下载地址")
    @GetMapping("/presigned")
    public R<String> getPresignedUrl(@RequestParam String objectName,
                                     @RequestParam(value = "expiry", defaultValue = "60") int expiryMinutes) {
        String url = minioService.getPresignedUrl(objectName, expiryMinutes);
        return R.ok(url);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping
    public R<Void> delete(@RequestParam String objectName) {
        minioService.deleteFile(objectName);
        return R.ok();
    }

    @Operation(summary = "检查文件是否存在")
    @GetMapping("/exists")
    public R<Boolean> exists(@RequestParam String objectName) {
        return R.ok(minioService.fileExists(objectName));
    }
}
