package com.example.banhcanh.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;

    @Value("${s3.region:ap-southeast-2}")
    private String region;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Value("${s3.endpoint:https://yrzsuspxgiggepfnzqgi.storage.supabase.co/storage/v1/s3}")
    private String endpoint;

    private S3Client s3Client;
    private S3Presigner presigner;
    private boolean configured = false;

    @PostConstruct
    public void init() {
        // Kiểm tra nếu credentials là dummy/chưa được cấu hình
        if (isDummy(accessKey) || isDummy(secretKey) || isDummy(bucketName) || isDummy(endpoint)) {
            configured = false;
            return;
        }
        try {
            this.s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .endpointOverride(URI.create(endpoint))
                    .forcePathStyle(true)
                    .build();

            this.presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build())
                    .build();
            configured = true;
        } catch (Exception e) {
            configured = false;
        }
    }

    private boolean isDummy(String value) {
        return value == null || value.isBlank()
                || value.startsWith("dummy")
                || value.contains("dummy.example.com");
    }

    public boolean isConfigured() {
        return configured;
    }

    public String getPresignedUrl(String key, Duration expiration) {
        if (!configured) throw new RuntimeException("Dịch vụ lưu trữ file chưa được cấu hình trên server");
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(getObjectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    public String getPresignedUrl(String key) {
        return getPresignedUrl(key, Duration.ofHours(24));
    }

    public String uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, null, null);
    }

    public String uploadFile(MultipartFile file, String folder, String entityId, String entityName) {
        if (!configured) {
            throw new RuntimeException("Dịch vụ lưu trữ file (S3/Object Storage) chưa được cấu hình. " +
                    "Vui lòng thiết lập các biến môi trường: SUPABASE_S3_ACCESS_KEY, SUPABASE_S3_SECRET_KEY, SUPABASE_S3_BUCKET_NAME, SUPABASE_S3_ENDPOINT trên Render.");
        }
        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            StringBuilder subfolder = new StringBuilder();
            if (entityId != null && !entityId.isBlank()) {
                subfolder.append(entityId);
            } else {
                subfolder.append("new");
            }
            if (entityName != null && !entityName.isBlank()) {
                String slug = slugify(entityName);
                subfolder.append("_").append(slug);
            } else {
                subfolder.append("_").append(UUID.randomUUID().toString());
            }
            String key = folder + "/" + subfolder.toString() + "/" + UUID.randomUUID().toString() + ext;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Return key only - frontend will get presigned URL via /api/upload/presigned
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Không thể tải file lên S3: " + e.getMessage(), e);
        }
    }

    private String slugify(String input) {
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
