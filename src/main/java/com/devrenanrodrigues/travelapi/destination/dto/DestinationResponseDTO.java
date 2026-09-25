package com.devrenanrodrigues.travelapi.destination.dto;

import com.devrenanrodrigues.travelapi.category.Category;
import com.devrenanrodrigues.travelapi.destination.AiStatus;
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
        String state,
        String country,
        List<String> categories,
        Double rating,
        Integer reviewCount,
        AiStatus aiStatus,
        Double latitude,
        Double longitude,
        String photoQuery,
        String coverImageUrl,
        List<DestinationImageResponseDTO> images,
        String aiSummary,
        String aiCostEstimates,
        Instant aiCachedAt,
        Instant createdAt,
        Instant updatedAt
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

        List<String> categoryNames = destination.getCategories() != null
                ? destination.getCategories().stream().map(Category::getName).toList()
                : List.of();

        List<DestinationImageResponseDTO> imageDTOs = destination.getImages() != null
                ? destination.getImages().stream().map(DestinationImageResponseDTO::fromEntity).toList()
                : List.of();

        return new DestinationResponseDTO(
                destination.getId(),
                airportId,
                airportName,
                airportIata,
                destination.getName(),
                destination.getCity(),
                destination.getState(),
                destination.getCountry(),
                categoryNames,
                destination.getRating(),
                destination.getReviewCount(),
                destination.getAiStatus(),
                destination.getLatitude(),
                destination.getLongitude(),
                destination.getPhotoQuery(),
                destination.getCoverImageUrl(),
                imageDTOs,
                destination.getAiSummary(),
                destination.getAiCostEstimates(),
                destination.getAiCachedAt(),
                destination.getCreatedAt(),
                destination.getUpdatedAt()
        );
    }
}
