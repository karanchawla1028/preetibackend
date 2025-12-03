// src/main/java/com/preetinest/controller/ImageController.java
package com.preetinest.controller;

import com.preetinest.config.S3Service;
import com.preetinest.entity.Image;
import com.preetinest.impl.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Image Upload", description = "Upload image files to S3 and save metadata in database")
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private ImageService imageService;  // This saves to DB

    @Operation(summary = "Upload image file", description = "Uploads image to S3 and saves record in database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uploaded & saved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "id": 1,
                                      "fileName": "photo-8f3a1c2d.jpg",
                                      "url": "https://your-bucket.s3.amazonaws.com/photo-8f3a1c2d.jpg",
                                      "uploadedAt": "2025-12-03T10:30:45"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "No file or invalid file"),
            @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please select a file to upload"));
        }

        try {
            // Step 1: Upload to S3
            String savedFileName = s3Service.uploadFile(file);
            String fullUrl = s3Service.getFullUrl(savedFileName);

            // Step 2: Save metadata in database
            Image savedImage = imageService.saveImage(savedFileName, fullUrl);

            // Step 3: Return full DB record
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedImage.getId());
            response.put("fileName", savedImage.getFileName());
            response.put("url", savedImage.getUrl());
            response.put("uploadedAt", savedImage.getUploadedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}