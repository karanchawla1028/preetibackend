// src/main/java/com/preetinest/controller/ImageController.java

package com.preetinest.controller;

import com.preetinest.config.S3Service;
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

@Tag(name = "Image Upload", description = "Upload real image files (png, jpg, webp, avif, etc.) to S3")
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private S3Service s3Service;

    @Operation(summary = "Upload image file", description = "Upload any image file. Keeps original name (safe version).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uploaded successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"fileName\":\"test.png\",\"url\":\"https://preetinest.s3.../test.png\"}"))),
            @ApiResponse(responseCode = "400", description = "No file or invalid file"),
            @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please select a file to upload"));
        }

        try {
            String fileName = s3Service.uploadFile(file);  // We'll create this method
            String fullUrl = s3Service.getFullUrl(fileName);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("url", fullUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}