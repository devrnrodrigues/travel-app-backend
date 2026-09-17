package com.devrenanrodrigues.travelapi.destination.dto;

import com.devrenanrodrigues.travelapi.airport.dto.AirportResponseDTO;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherResponseDTO;

import java.util.List;
import java.util.UUID;

public record DestinationDetailResponseDTO(
        UUID id,
        String name,
        String city,
        String state,
        String country,
        String coverImageUrl,
        Double rating,
        Integer reviewCount,
        String description,
        String aiCostEstimates,
        AirportResponseDTO nearestAirport,
        List<String> galleryUrls,
        WeatherResponseDTO weather,
        DestinationCommentsSummaryDTO comments
) {
    public static DestinationDetailResponseDTO of(
            Destination destination,
            WeatherResponseDTO weather,
            DestinationCommentsSummaryDTO comments
    ) {
        return new DestinationDetailResponseDTO(
                destination.getId(),
                destination.getName(),
                destination.getCity(),
                destination.getState(),
                destination.getCountry(),
                destination.getCoverImageUrl(),
                destination.getRating(),
                destination.getReviewCount(),
                destination.getAiSummary(),
                destination.getAiCostEstimates(),
                destination.getNearestAirport() != null ? AirportResponseDTO.fromEntity(destination.getNearestAirport()) : null,
                destination.getGalleryUrls(),
                weather,
                comments
        );
    }
}
