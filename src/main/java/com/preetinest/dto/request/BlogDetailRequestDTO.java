package com.preetinest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogDetailRequestDTO {

    @NotBlank(message = "Heading is mandatory")
    @Size(max = 255, message = "Heading must not exceed 255 characters")
    private String heading;

    @NotBlank(message = "Content is mandatory")
    private String content;

    // This holds the S3 filename (e.g., "blog-details/bali-beach-2025.jpg")
    private String image;

    // Optional: keep for backward compatibility or external URLs
    private String imageUrl;

    @NotNull(message = "Display order is mandatory")
    private Integer displayOrder;

    @NotNull(message = "Blog ID is mandatory")
    private Long blogId;

    private Boolean active;
}