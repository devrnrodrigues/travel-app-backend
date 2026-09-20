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
        Instant updatedAt,
        String userName,
        String userAvatarUrl,
        Integer helpfulCount,
        Boolean isHelpful
) {
    public static CommentResponseDTO fromEntity(Comment comment, String userName, String userAvatarUrl, Boolean isHelpful) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getUserId(),
                comment.getDestination().getId(),
                comment.getRating(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                userName,
                userAvatarUrl,
                comment.getHelpfulCount() != null ? comment.getHelpfulCount() : 0,
                Boolean.TRUE.equals(isHelpful)
        );
    }

    public static CommentResponseDTO fromEntity(Comment comment, String userName, String userAvatarUrl) {
        return fromEntity(comment, userName, userAvatarUrl, false);
    }

    public static CommentResponseDTO fromEntity(Comment comment) {
        return fromEntity(comment, null, null, false);
    }
}
