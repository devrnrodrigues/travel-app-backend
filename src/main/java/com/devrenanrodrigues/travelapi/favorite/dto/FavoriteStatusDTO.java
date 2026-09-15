package com.devrenanrodrigues.travelapi.favorite.dto;

import java.util.UUID;

public record FavoriteStatusDTO(
        UUID destinationId,
        UUID userId,
        boolean isFavorite
) {
}
