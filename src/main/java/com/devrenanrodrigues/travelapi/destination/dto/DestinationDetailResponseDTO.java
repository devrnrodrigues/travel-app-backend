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
        String iata,
        String nearestAirportIata,
        String coverImageUrl,
        String photoQuery,
        Double rating,
        Integer reviewCount,
        List<String> description,
        String aiCostEstimates,
        Long approximatePopulation,
        Integer popularity,
        AirportResponseDTO nearestAirport,
        List<DestinationImageResponseDTO> images,
        WeatherResponseDTO weather,
        DestinationCommentsSummaryDTO comments
) {
    public static DestinationDetailResponseDTO of(
            Destination destination,
            AirportResponseDTO nearestAirport,
            WeatherResponseDTO weather,
            DestinationCommentsSummaryDTO comments
    ) {
        List<DestinationImageResponseDTO> imageDTOs = destination.getImages() != null
                ? destination.getImages().stream().map(DestinationImageResponseDTO::fromEntity).toList()
                : List.of();

        return new DestinationDetailResponseDTO(
                destination.getId(),
                destination.getName(),
                destination.getCity(),
                destination.getState(),
                destination.getCountry(),
                destination.getIata(),
                destination.getIata(),
                destination.getCoverImageUrl(),
                destination.getPhotoQuery(),
                destination.getRating(),
                destination.getReviewCount(),
                destination.getAiSummary(),
                destination.getAiCostEstimates(),
                destination.getApproximatePopulation(),
                destination.getPopularity(),
                nearestAirport,
                imageDTOs,
                weather,
                comments
        );
    }
}
