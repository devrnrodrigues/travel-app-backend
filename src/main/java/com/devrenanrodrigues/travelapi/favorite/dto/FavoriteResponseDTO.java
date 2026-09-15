package com.devrenanrodrigues.travelapi.favorite.dto;

import com.devrenanrodrigues.travelapi.favorite.Favorite;

import java.time.Instant;
import java.util.UUID;

public record FavoriteResponseDTO(
        UUID userId,
        UUID destinationId,
        String destinationName,
        String destinationCity,
        String destinationCountry,
        String destinationCategory,
        String destinationCoverImageUrl,
        Instant createdAt
) {
    public static FavoriteResponseDTO fromEntity(Favorite favorite) {
        return new FavoriteResponseDTO(
                favorite.getId().getUserId(),
                favorite.getDestination().getId(),
                favorite.getDestination().getName(),
                favorite.getDestination().getCity(),
                favorite.getDestination().getCountry(),
                favorite.getDestination().getCategory(),
                favorite.getDestination().getCoverImageUrl(),
                favorite.getCreatedAt()
        );
    }
}
