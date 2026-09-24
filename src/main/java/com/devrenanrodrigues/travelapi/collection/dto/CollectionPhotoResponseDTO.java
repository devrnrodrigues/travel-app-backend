package com.devrenanrodrigues.travelapi.collection.dto;

import com.devrenanrodrigues.travelapi.collection.CollectionPhoto;

import java.time.Instant;
import java.util.UUID;

public record CollectionPhotoResponseDTO(
        UUID id,
        String url,
        String caption,
        Integer orderIndex,
        Instant createdAt
) {
    public static CollectionPhotoResponseDTO fromEntity(CollectionPhoto entity) {
        if (entity == null) {
            return null;
        }
        return new CollectionPhotoResponseDTO(
                entity.getId(),
                entity.getUrl(),
                entity.getCaption(),
                entity.getOrderIndex(),
                entity.getCreatedAt()
        );
    }
}
