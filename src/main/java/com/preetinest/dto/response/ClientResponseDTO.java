package com.preetinest.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientResponseDTO {
    private Long id;
    private String uuid;
    private String name;
    private String clientType;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private String logoUrl;
    private String metaTitle;
    private String metaKeyword;
    private String metaDescription;
    private String slug;
    private boolean active;
    private boolean displayStatus;
    private boolean showOnHome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
}