package com.devrenanrodrigues.travelapi.collection.dto;

import com.devrenanrodrigues.travelapi.collection.Collection;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record CollectionResponseDTO(
        UUID id,
        String title,
        List<CollectionPhotoResponseDTO> photos,
        Instant createdAt,
        Instant updatedAt
) {
    public static CollectionResponseDTO fromEntity(Collection entity) {
        if (entity == null) {
            return null;
        }

        List<CollectionPhotoResponseDTO> photos = entity.getPhotos() == null
                ? Collections.emptyList()
                : entity.getPhotos().stream().map(CollectionPhotoResponseDTO::fromEntity).toList();

        return new CollectionResponseDTO(
                entity.getId(),
                entity.getTitle(),
                photos,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
