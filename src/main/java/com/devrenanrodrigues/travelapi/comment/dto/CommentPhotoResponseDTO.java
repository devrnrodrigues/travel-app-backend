package com.devrenanrodrigues.travelapi.comment.dto;

import com.devrenanrodrigues.travelapi.comment.CommentPhoto;

import java.util.UUID;

public record CommentPhotoResponseDTO(
        UUID id,
        String url,
        Integer orderIndex
) {
    public static CommentPhotoResponseDTO fromEntity(CommentPhoto photo) {
        return new CommentPhotoResponseDTO(
                photo.getId(),
                photo.getUrl(),
                photo.getOrderIndex()
        );
    }
}
