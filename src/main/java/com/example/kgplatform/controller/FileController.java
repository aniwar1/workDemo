package com.example.kgplatform.controller;

import com.example.kgplatform.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("请选择文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return R.fail("只能上传图片文件");
        }
        long maxSize = 2 * 1024 * 1024; // 2MB
        if (file.getSize() > maxSize) {
            return R.fail("图片大小不能超过 2MB");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID() + ext;

        try {
            Path uploadDir = Paths.get(uploadPath, "avatar");
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(newFileName);
            file.transferTo(filePath.toFile());
            return R.ok("/api/file/avatar/" + newFileName);
        } catch (IOException e) {
            return R.fail("文件保存失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取头像")
    @GetMapping("/avatar/{filename}")
    public byte[] getAvatar(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(uploadPath, "avatar", filename);
        if (!Files.exists(filePath)) {
            return new byte[0];
        }
        String contentType = Files.probeContentType(filePath);
        return Files.readAllBytes(filePath);
    }
}
