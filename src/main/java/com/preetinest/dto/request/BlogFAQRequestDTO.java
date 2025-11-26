// com.preetinest.dto.request.BlogFAQRequestDTO
package com.preetinest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BlogFAQRequestDTO {

    @NotBlank(message = "Question cannot be blank")
    @Size(max = 500, message = "Question too long")
    private String question;

    @NotBlank(message = "Answer cannot be blank")
    private String answer;

    // ← Use Integer (not int) — allows null → supports partial updates
    @NotNull(message = "Display order is required")
    @Min(value = 0, message = "Display order must be >= 0")
    private Integer displayOrder;

    @NotNull(message = "Blog ID is required")
    private Long blogId;

    // ← Use Boolean (not boolean) → allows null in PATCH requests
    private Boolean active;

    private Boolean displayStatus;
}