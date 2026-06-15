package org.acme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {

    @NotBlank(message = "Question must not be blank")
    @Size(min = 3, max = 2000, message = "Question must be between 3 and 2000 characters")
    public String question;
}
