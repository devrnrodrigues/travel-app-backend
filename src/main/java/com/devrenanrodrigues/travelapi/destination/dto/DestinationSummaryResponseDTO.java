package com.devrenanrodrigues.travelapi.destination.dto;

import com.devrenanrodrigues.travelapi.destination.Destination;

import java.util.List;
import java.util.UUID;

public record DestinationSummaryResponseDTO(
        UUID id,
        String name,
        String city,
        String state,
        String country,
        List<String> categories,
        String coverImageUrl,
        Double rating
) {
    public static DestinationSummaryResponseDTO fromEntity(Destination destination) {
        return new DestinationSummaryResponseDTO(
                destination.getId(),
                destination.getName(),
                destination.getCity(),
                destination.getState(),
                destination.getCountry(),
                destination.getCategories(),
                destination.getCoverImageUrl(),
                destination.getRating()
        );
    }
}
