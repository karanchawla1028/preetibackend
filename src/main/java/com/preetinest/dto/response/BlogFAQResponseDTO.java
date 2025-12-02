package com.preetinest.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogFAQResponseDTO {
    private Long id;
    private String uuid;
    private String question;
    private String answer;
    private int displayOrder;
    private Long blogId;
    private boolean active;
    private boolean displayStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
}