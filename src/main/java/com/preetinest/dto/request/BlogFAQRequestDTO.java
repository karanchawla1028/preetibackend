package com.preetinest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogFAQRequestDTO {

    @NotBlank(message = "Question cannot be blank")
    @Size(max = 255, message = "Question must not exceed 255 characters")
    private String question;

    @NotBlank(message = "Answer cannot be blank")
    private String answer;

    @NotNull(message = "Display order cannot be null")
    @Min(value = 0, message = "Display order must be a positive number")
    private int displayOrder;

    @NotNull(message = "Blog ID cannot be null")
    private Long blogId;

    @NotNull(message = "Active status cannot be null")
    private boolean active;

    @NotNull(message = "Display status cannot be null")
    private boolean displayStatus;
}
