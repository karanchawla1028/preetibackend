package com.preetinest.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

@Service
public class S3Service {

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
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.CA_CENTRAL_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    /**
     * Uploads image directly to root of bucket
     * Returns only filename (e.g., abc123.png)
     */
    public String uploadBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) return null;

        try {
            String base64Data = base64Image.split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            String extension = base64Image.contains("png") ? "png" :
                    base64Image.contains("jpeg") || base64Image.contains("jpg") ? "jpg" : "webp";

            // Generate clean filename — direct in root
            String fileName = UUID.randomUUID().toString() + "." + extension;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType("image/" + extension);

            s3Client.putObject(new PutObjectRequest(bucketName, fileName,
                    new ByteArrayInputStream(imageBytes), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            return fileName; // e.g., "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8.png"

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to S3", e);
        }
    }

    public String getFullUrl(String key) {
        if (key == null || key.isEmpty()) return null;
        return baseUrl + "/" + key; // → https://preetinest.s3.../abc123.png
    }
}