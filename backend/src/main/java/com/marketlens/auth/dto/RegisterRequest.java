package com.marketlens.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$",
                message = "3 to 30 characters, letters digits dot dash underscore") String username,
        @NotBlank @Size(max = 80) String displayName,
        @NotBlank @Size(min = 8, max = 100) String password) {
}
