package com.devrenanrodrigues.travelapi.destination.dto;

import com.devrenanrodrigues.travelapi.destination.AiStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
public record DestinationRequestDTO(
        @Size(max = 3)
        String iata,

        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Size(max = 100)
        String city,

        @Size(max = 100)
        String state,

        @NotBlank
        @Size(max = 100)
        String country,

        @NotEmpty
        List<String> categories,

        Double rating,

        AiStatus aiStatus,

        @NotNull
        Double latitude,

        @NotNull
        Double longitude,

        @Size(max = 150)
        String photoQuery,

        String coverImageUrl,
        List<String> aiSummary,
        String aiCostEstimates,
        Long approximatePopulation,
        Integer popularity
) {
}
