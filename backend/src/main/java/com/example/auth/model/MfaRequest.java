package com.example.auth.model;

import jakarta.validation.constraints.NotBlank;

public record MfaRequest(@NotBlank String username, @NotBlank String code) {
}
