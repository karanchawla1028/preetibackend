package com.preetinest.controller;

import com.preetinest.config.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Image Upload", description = "API for uploading images to S3 bucket")
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private S3Service s3Service;

    @Operation(summary = "Upload a base64-encoded image to S3",
            description = "Uploads a base64-encoded image directly to the S3 bucket and returns the filename and full URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid base64 image data"),
            @ApiResponse(responseCode = "500", description = "Failed to upload image")
    })
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestBody @Schema(description = "Base64-encoded image data (e.g., data:image/png;base64,...)") String base64Image) {

        if (base64Image == null || base64Image.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Base64 image data is required"));
        }

        try {
            String fileName = s3Service.uploadBase64Image(base64Image);
            if (fileName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid image data"));
            }

            String fullUrl = s3Service.getFullUrl(fileName);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("url", fullUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
}