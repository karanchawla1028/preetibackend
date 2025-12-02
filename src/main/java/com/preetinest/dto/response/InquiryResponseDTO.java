package com.preetinest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponseDTO {

    private Long id;
    private String uuid;
    private String name;
    private String location;
    private String message;
    private String email;
    private String phone;
    private String pageName;
    private String pageType;
    private String slug;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
}