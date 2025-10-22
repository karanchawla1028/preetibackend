package com.preetinest.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogDetailResponseDTO {
    private Long id;
    private String uuid;
    private String heading;
    private String content;
    private String imageUrl;
    private Integer displayOrder;
    private Long blogId;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
}