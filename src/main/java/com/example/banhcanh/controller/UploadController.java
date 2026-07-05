package com.example.banhcanh.controller;

import com.example.banhcanh.model.Driver;
import com.example.banhcanh.repository.DriverRepository;
import com.example.banhcanh.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private DriverRepository driverRepository;

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder) {

        String[] allowedFolders = {"product_Image", "avatar_Image", "review_Image", "category_Image"};
        boolean valid = false;
        for (String f : allowedFolders) {
            if (f.equals(folder)) { valid = true; break; }
        }
        if (!valid) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid folder: " + folder));
        }

        try {
            String url = s3Service.uploadFile(file, folder);
            return ResponseEntity.ok(Map.of(
                "url", url,
                "folder", folder
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "url", uploadToLocal(file),
                "folder", folder
            ));
        }
    }

    @PostMapping("/avatar-base64")
    public ResponseEntity<?> uploadAvatarBase64(@RequestBody Map<String, String> body) {
        try {
            String base64Data = body.get("data");
            Long driverId = body.get("driverId") != null ? Long.valueOf(body.get("driverId")) : null;
            if (base64Data == null || base64Data.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu dữ liệu ảnh"));
            }
            String dataUrl = "data:image/png;base64," + base64Data;
            if (driverId != null) {
                driverRepository.findById(driverId).ifPresent(driver -> {
                    driver.setAvatarUrl(dataUrl);
                    driverRepository.save(driver);
                });
            }
            return ResponseEntity.ok(Map.of("url", dataUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String uploadToLocal(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return "data:" + file.getContentType() + ";base64," + base64;
        } catch (Exception e) {
            throw new RuntimeException("Không thể đọc file: " + e.getMessage());
        }
    }
}
