package com.example.banhcanh.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

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
            String uploadPath = uploadDir + File.separator + folder;
            Path uploadDirPath = Paths.get(uploadPath);
            Files.createDirectories(uploadDirPath);

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + ext;

            Path filePath = uploadDirPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            String fileUrl = "/api/uploads/" + folder + "/" + fileName;

            return ResponseEntity.ok(Map.of(
                "url", fileUrl,
                "fileName", fileName,
                "folder", folder
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
