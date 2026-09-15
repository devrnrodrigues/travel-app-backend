package com.devrenanrodrigues.travelapi.comment.dto;

import com.devrenanrodrigues.travelapi.comment.Comment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponseDTO(
        UUID id,
        UUID userId,
        UUID destinationId,
        Integer rating,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponseDTO fromEntity(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getUserId(),
                comment.getDestination().getId(),
                comment.getRating(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
