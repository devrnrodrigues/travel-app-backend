package com.devrenanrodrigues.travelapi.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDTO(
        @NotBlank(message = "O idToken é obrigatório")
        String idToken
) {}
