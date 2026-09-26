package com.devrenanrodrigues.travelapi.destination.dto;

import com.devrenanrodrigues.travelapi.category.Category;
import com.devrenanrodrigues.travelapi.destination.AiStatus;
import com.devrenanrodrigues.travelapi.destination.Destination;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DestinationResponseDTO(
        UUID id,
        String iata,
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
        List<String> aiSummary,
        String aiCostEstimates,
        Long approximatePopulation,
        Integer popularity,
        Instant aiCachedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static DestinationResponseDTO fromEntity(Destination destination) {
        List<String> categoryNames = destination.getCategories() != null
                ? destination.getCategories().stream().map(Category::getName).toList()
                : List.of();

        List<DestinationImageResponseDTO> imageDTOs = destination.getImages() != null
                ? destination.getImages().stream().map(DestinationImageResponseDTO::fromEntity).toList()
                : List.of();

        return new DestinationResponseDTO(
                destination.getId(),
                destination.getIata(),
                destination.getIata(),
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
                destination.getApproximatePopulation(),
                destination.getPopularity(),
                destination.getAiCachedAt(),
                destination.getCreatedAt(),
                destination.getUpdatedAt()
        );
    }
}
