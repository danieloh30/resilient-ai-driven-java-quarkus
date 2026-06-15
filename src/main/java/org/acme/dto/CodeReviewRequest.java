package org.acme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CodeReviewRequest {

    @NotBlank(message = "Code must not be blank")
    @Size(min = 10, max = 8000, message = "Code must be between 10 and 8000 characters")
    public String code;
}
