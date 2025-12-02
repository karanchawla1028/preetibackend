package com.preetinest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BlogDetailRequestDTO {

    @NotBlank(message = "Heading is mandatory")
    @Size(max = 255, message = "Heading must not exceed 255 characters")
    private String heading;

    @NotBlank(message = "Content is mandatory")
    private String content;

    // NEW: Base64 image (main way to upload images)
    private String imageBase64;  // e.g., "data:image/png;base64,iVBORw0KGgo..."

    // OLD: Keep this for backward compatibility or external URLs (optional)
    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;

    @NotNull(message = "Display order is mandatory")
    private Integer displayOrder;

    @NotNull(message = "Blog ID is mandatory")
    private Long blogId;

    @NotNull(message = "Active status is mandatory")
    private Boolean active;
}