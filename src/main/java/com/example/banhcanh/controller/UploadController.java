package com.example.banhcanh.controller;

import com.example.banhcanh.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private S3Service s3Service;

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder,
            @RequestParam(value = "id", required = false) String entityId,
            @RequestParam(value = "name", required = false) String entityName) {

        // Kiểm tra S3 đã được cấu hình chưa
        if (!s3Service.isConfigured()) {
            return ResponseEntity.status(503).body(Map.of(
                "error", "Tính năng tải ảnh chưa được cấu hình trên server. Vui lòng liên hệ quản trị viên để thiết lập Object Storage."
            ));
        }

        String[] allowedFolders = {"product_Image", "avatar_Image", "review_Image", "category_Image", "payment_Image"};
        boolean valid = false;
        for (String f : allowedFolders) {
            if (f.equals(folder)) { valid = true; break; }
        }
        if (!valid) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid folder: " + folder));
        }

        try {
            String url = s3Service.uploadFile(file, folder, entityId, entityName);
            return ResponseEntity.ok(Map.of(
                "url", url,
                "folder", folder
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/presigned")
    public ResponseEntity<?> getPresignedUrl(
            @RequestParam("key") String key,
            @RequestParam(value = "expireHours", defaultValue = "24") int expireHours) {
        try {
            String url = s3Service.getPresignedUrl(key, Duration.ofHours(expireHours));
            return ResponseEntity.ok(Map.of("url", url, "key", key));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
