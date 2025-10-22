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
public class ClientRequestDTO {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Client type cannot be blank")
    @Size(max = 100, message = "Client type must not exceed 100 characters")
    private String clientType;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @Size(max = 255, message = "Contact email must not exceed 255 characters")
    private String contactEmail;

    @Size(max = 255, message = "Contact phone must not exceed 255 characters")
    private String contactPhone;

    @Size(max = 255, message = "Logo URL must not exceed 255 characters")
    private String logoUrl;

    @NotBlank(message = "Meta title cannot be blank")
    @Size(max = 255, message = "Meta title must not exceed 255 characters")
    private String metaTitle;

    @NotBlank(message = "Meta keyword cannot be blank")
    private String metaKeyword;

    @NotBlank(message = "Meta description cannot be blank")
    private String metaDescription;

    @NotBlank(message = "Slug cannot be blank")
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    private String slug;

    @NotNull(message = "Active status cannot be null")
    private boolean active;

    @NotNull(message = "Display status cannot be null")
    private boolean displayStatus;

    @NotNull(message = "Show on home status cannot be null")
    private boolean showOnHome;
}