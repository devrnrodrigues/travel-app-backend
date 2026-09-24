package com.devrenanrodrigues.travelapi.collection.dto;

import jakarta.validation.constraints.Size;

public record UpdatePhotoCaptionDTO(
        @Size(max = 30, message = "A legenda deve ter no máximo 30 caracteres.")
        String caption
) {
}
