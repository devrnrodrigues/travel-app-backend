package com.devrenanrodrigues.travelapi.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateCollectionRequestDTO(
        @NotBlank(message = "O título não pode ser vazio.")
        @Size(max = 30, message = "O título deve ter no máximo 30 caracteres.")
        String title,
        List<UUID> deletePhotoIds
) {
}
