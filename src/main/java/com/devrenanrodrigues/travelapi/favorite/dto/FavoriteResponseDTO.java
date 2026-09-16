package com.devrenanrodrigues.travelapi.favorite.dto;

import com.devrenanrodrigues.travelapi.favorite.Favorite;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FavoriteResponseDTO(
        UUID userId,
        UUID destinationId,
        String destinationName,
        String destinationCity,
        String destinationCountry,
        List<String> destinationCategories,
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
                favorite.getDestination().getCategories(),
                favorite.getDestination().getCoverImageUrl(),
                favorite.getCreatedAt()
        );
    }
}
