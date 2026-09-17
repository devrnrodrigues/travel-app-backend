package com.devrenanrodrigues.travelapi.destination.dto;

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
        String description,
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
                destination.getAiSummary(),
                destination.getGalleryUrls(),
                weather,
                comments
        );
    }
}
