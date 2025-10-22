package com.preetinest.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogResponseDTO {
    private Long id;
    private String uuid;
    private String title;
    private String excerpt;
    private String metaTitle;
    private String metaKeyword;
    private String metaDescription;
    private String slug;
    private String thumbnailUrl;
    private boolean active;
    private boolean displayStatus;
    private boolean showOnHome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long categoryId;
    private Long subCategoryId;
    private Long serviceId;
}