package com.devrenanrodrigues.travelapi.comment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentRequestDTO(
        UUID userId,

        @NotNull(message = "A avaliação é obrigatória")
        @Min(value = 1, message = "A avaliação mínima é 1")
        @Max(value = 5, message = "A avaliação máxima é 5")
        Integer rating,

        @NotBlank(message = "O comentário é obrigatório")
        String content
) {
}
