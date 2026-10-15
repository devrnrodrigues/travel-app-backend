package com.devrenanrodrigues.travelapi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleLoginRequestDTO(
        @NotBlank(message = "O idToken é obrigatório")
        @Size(max = 4096, message = "O idToken deve ter no máximo 4096 caracteres.")
        String idToken
) {}
