package com.devrenanrodrigues.travelapi.destination.dto;

import com.devrenanrodrigues.travelapi.destination.Destination;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DestinationResponseDTO(
        UUID id,
        UUID nearestAirportId,
        String nearestAirportName,
        String nearestAirportIata,
        String name,
        String city,
        String country,
        String category,
        Double latitude,
        Double longitude,
        String photoQuery,
        String coverImageUrl,
        List<String> galleryUrls,
        String aiSummary,
        String aiCostEstimates,
        Instant aiCachedAt,
        Instant createdAt
) {
    public static DestinationResponseDTO fromEntity(Destination destination) {
        UUID airportId = null;
        String airportName = null;
        String airportIata = null;

        if (destination.getNearestAirport() != null) {
            airportId = destination.getNearestAirport().getId();
            airportName = destination.getNearestAirport().getName();
            airportIata = destination.getNearestAirport().getIataCode();
        }

        return new DestinationResponseDTO(
                destination.getId(),
                airportId,
                airportName,
                airportIata,
                destination.getName(),
                destination.getCity(),
                destination.getCountry(),
                destination.getCategory(),
                destination.getLatitude(),
                destination.getLongitude(),
                destination.getPhotoQuery(),
                destination.getCoverImageUrl(),
                destination.getGalleryUrls(),
                destination.getAiSummary(),
                destination.getAiCostEstimates(),
                destination.getAiCachedAt(),
                destination.getCreatedAt()
        );
    }
}
