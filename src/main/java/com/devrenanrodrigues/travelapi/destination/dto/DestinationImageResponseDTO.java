package com.devrenanrodrigues.travelapi.destination.dto;

import com.devrenanrodrigues.travelapi.destination.DestinationImage;

import java.util.UUID;

public record DestinationImageResponseDTO(
        UUID id,
        String url,
        String photographer,
        String photographerUrl,
        boolean isCover,
        int position
) {
    public static DestinationImageResponseDTO fromEntity(DestinationImage image) {
        if (image == null) return null;
        return new DestinationImageResponseDTO(
                image.getId(),
                image.getUrl(),
                image.getPhotographer(),
                image.getPhotographerUrl(),
                image.isCover(),
                image.getPosition()
        );
    }
}
