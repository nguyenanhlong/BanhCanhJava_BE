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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;

    @Value("${s3.region:auto}")
    private String region;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Value("${s3.endpoint:https://s3.auto.railway.app}")
    private String endpoint;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
                .build();

        setPublicReadPolicy();
    }

    private void setPublicReadPolicy() {
        try {
            String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [{
                    "Sid": "PublicReadGetObject",
                    "Effect": "Allow",
                    "Principal": "*",
                    "Action": "s3:GetObject",
                    "Resource": "arn:aws:s3:::%s/*"
                  }]
                }
                """.formatted(bucketName);

            s3Client.putBucketPolicy(b -> b.bucket(bucketName).policy(policy));
        } catch (Exception e) {
            System.err.println("Không thể đặt bucket policy: " + e.getMessage());
        }
    }

    public String uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, null, null);
    }

    public String uploadFile(MultipartFile file, String folder, String entityId, String entityName) {
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
                subfolder.append("_").append(entityName.replaceAll("[^a-zA-Z0-9_\\-\\p{L}]", "_"));
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

            return endpoint + "/" + bucketName + "/" + key;
        } catch (Exception e) {
            throw new RuntimeException("Không thể tải file lên S3: " + e.getMessage(), e);
        }
    }
}
