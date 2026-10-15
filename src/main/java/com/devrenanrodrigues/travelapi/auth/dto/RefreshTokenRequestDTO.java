package com.devrenanrodrigues.travelapi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "O refresh token é obrigatório.")
        @Size(max = 500, message = "O refresh token deve ter no máximo 500 caracteres.")
        String refreshToken
) {
}
