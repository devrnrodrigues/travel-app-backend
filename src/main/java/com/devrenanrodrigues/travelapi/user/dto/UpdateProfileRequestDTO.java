package com.devrenanrodrigues.travelapi.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequestDTO(
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String fullName,

        @Size(max = 150, message = "A bio deve ter no máximo 150 caracteres.")
        String bio,

        @Size(max = 20, message = "A nacionalidade deve ter no máximo 20 caracteres.")
        String nationality
) {
}
