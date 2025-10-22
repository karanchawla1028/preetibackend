package com.preetinest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogRequestDTO {
    @NotBlank(message = "Title is mandatory")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Excerpt is mandatory")
    private String excerpt;

    @NotBlank(message = "Meta title is mandatory")
    @Size(max = 255, message = "Meta title must not exceed 255 characters")
    private String metaTitle;

    @NotBlank(message = "Meta keyword is mandatory")
    private String metaKeyword;

    @NotBlank(message = "Meta description is mandatory")
    private String metaDescription;

    @NotBlank(message = "Slug is mandatory")
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    private String slug;

    @Size(max = 255, message = "Thumbnail URL must not exceed 255 characters")
    private String thumbnailUrl;

    @NotNull(message = "Active status is mandatory")
    private Boolean active;

    @NotNull(message = "Show on home status is mandatory")
    private Boolean showOnHome;

    @NotNull(message = "Category ID is mandatory")
    private Long categoryId;

    private Long subCategoryId;

    private Long serviceId;
}