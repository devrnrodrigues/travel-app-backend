package com.devrenanrodrigues.travelapi.destination.dto;

import com.devrenanrodrigues.travelapi.category.Category;
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
        String photoQuery,
        Double rating,
        Integer reviewCount
) {
    public static DestinationSummaryResponseDTO fromEntity(Destination destination) {
        List<String> categoryNames = destination.getCategories() != null
                ? destination.getCategories().stream().map(Category::getName).toList()
                : List.of();

        return new DestinationSummaryResponseDTO(
                destination.getId(),
                destination.getName(),
                destination.getCity(),
                destination.getState(),
                destination.getCountry(),
                categoryNames,
                destination.getCoverImageUrl(),
                destination.getPhotoQuery(),
                destination.getRating(),
                destination.getReviewCount()
        );
    }
}
