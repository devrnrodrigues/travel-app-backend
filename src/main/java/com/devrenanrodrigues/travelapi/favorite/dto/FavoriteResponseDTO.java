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
        Double destinationRating,
        Integer destinationReviewCount,
        Double rating,
        Integer reviewCount,
        Instant createdAt
) {
    public static FavoriteResponseDTO fromEntity(Favorite favorite) {
        Double rating = (favorite.getDestination() != null && favorite.getDestination().getRating() != null)
                ? favorite.getDestination().getRating()
                : 0.0;
        Integer reviewCount = (favorite.getDestination() != null && favorite.getDestination().getReviewCount() != null)
                ? favorite.getDestination().getReviewCount()
                : 0;

        return new FavoriteResponseDTO(
                favorite.getId().getUserId(),
                favorite.getDestination().getId(),
                favorite.getDestination().getName(),
                favorite.getDestination().getCity(),
                favorite.getDestination().getCountry(),
                favorite.getDestination().getCategories(),
                favorite.getDestination().getCoverImageUrl(),
                rating,
                reviewCount,
                rating,
                reviewCount,
                favorite.getCreatedAt()
        );
    }
}
