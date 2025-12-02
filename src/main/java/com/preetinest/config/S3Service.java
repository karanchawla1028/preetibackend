package com.preetinest.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Value("${app.s3.base-url}")
    private String baseUrl;

    @Value("${aws.accessKey:}")
    private String accessKey;

    @Value("${aws.secretKey:}")
    private String secretKey;

    private AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            logger.error("AWS credentials are missing! Check application.properties or environment variables");
            throw new IllegalStateException("AWS Access Key or Secret Key is empty");
        }

        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.CA_CENTRAL_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        logger.info("S3 Client initialized successfully");
        logger.info("Bucket: {}", bucketName);
        logger.info("Region: ca-central-1");
        logger.info("Public Base URL: {}", baseUrl);
    }

    // ========================================================================
    // 1. Upload Base64 Image (from frontend canvas, mobile, etc.)
    // ========================================================================
    public String uploadBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            logger.warn("Attempted to upload empty or null base64 image");
            return null;
        }

        try {
            String[] parts = base64Image.split(",");
            if (parts.length < 2) {
                logger.error("Invalid base64 format: missing comma separator");
                throw new IllegalArgumentException("Invalid base64 format");
            }

            String base64Data = parts[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Data.trim());

            // Detect extension from data URI header
            String header = parts[0];
            String extension = "jpg"; // fallback
            if (header.contains("png")) extension = "png";
            else if (header.contains("jpeg") || header.contains("jpg")) extension = "jpg";
            else if (header.contains("webp")) extension = "webp";
            else if (header.contains("avif")) extension = "avif";
            else if (header.contains("gif")) extension = "gif";

            String fileName = UUID.randomUUID().toString() + "." + extension;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType("image/" + extension);

            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    fileName,
                    new ByteArrayInputStream(imageBytes),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);

            PutObjectResult result = s3Client.putObject(request);
            String fullUrl = baseUrl + fileName;

            logger.info("BASE64 IMAGE UPLOADED SUCCESSFULLY!");
            logger.info("   File Name     : {}", fileName);
            logger.info("   Size          : {} bytes", imageBytes.length);
            logger.info("   Content Type  : image/{}", extension);
            logger.info("   Public URL    : {}", fullUrl);

            return fileName;

        } catch (Exception e) {
            logger.error("Failed to upload base64 image to S3", e);
            throw new RuntimeException("Failed to upload base64 image", e);
        }
    }

    // ========================================================================
    // 2. Upload Real File (MultipartFile) — e.g., test.png from Postman/frontend
    // ========================================================================
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or null");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("File has no name");
        }

        // Security: prevent path traversal
        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename: " + originalName);
        }

        // Clean filename
        String cleanName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Make filename unique to avoid overwrites
        String extension = "";
        int dotIndex = cleanName.lastIndexOf('.');
        String finalName;
        if (dotIndex > 0 && dotIndex < cleanName.length() - 1) {
            extension = cleanName.substring(dotIndex); // includes dot
            String nameWithoutExt = cleanName.substring(0, dotIndex);
            finalName = nameWithoutExt + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;
        } else {
            finalName = cleanName + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        // Content type
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(contentType);

        PutObjectRequest request = new PutObjectRequest(
                bucketName,
                finalName,
                file.getInputStream(),
                metadata
        ).withCannedAcl(CannedAccessControlList.PublicRead);

        PutObjectResult result = s3Client.putObject(request);
        String fullUrl = baseUrl + finalName;

        logger.info("FILE UPLOADED SUCCESSFULLY!");
        logger.info("   Original Name : {}", originalName);
        logger.info("   Saved As      : {}", finalName);
        logger.info("   Size          : {} bytes", file.getSize());
        logger.info("   Content Type  : {}", contentType);
        logger.info("   Public URL    : {}", fullUrl);

        return finalName;
    }

    // ========================================================================
    // Helper: Generate full public URL
    // ========================================================================
    public String getFullUrl(String key) {
        if (key == null || key.isEmpty()) return null;
        String url = baseUrl + key;
        logger.debug("Generated public URL: {}", url);
        return url;
    }
}