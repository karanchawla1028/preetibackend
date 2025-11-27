// src/main/java/com/preetinest/dto/request/ClientRequestDTO.java

package com.preetinest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClientRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Client type is required")
    @Size(max = 100)
    private String clientType;

    @NotBlank(message = "Description is required")
    private String description;

    @Size(max = 255)
    private String contactEmail;

    @Size(max = 255)
    private String contactPhone;

    @NotBlank(message = "Meta title is required")
    @Size(max = 255)
    private String metaTitle;

    @NotBlank(message = "Meta keyword is required")
    private String metaKeyword;

    @NotBlank(message = "Meta description is required")
    private String metaDescription;

    @NotBlank(message = "Slug is required")
    @Size(max = 100)
    private String slug;

    @NotNull
    private Boolean active;

    @NotNull
    private Boolean displayStatus;

    @NotNull
    private Boolean showOnHome;

    private String logo; // filename only (e.g. "client-logo-123.png")
}