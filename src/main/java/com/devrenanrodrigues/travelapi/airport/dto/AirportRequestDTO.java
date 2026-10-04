package com.devrenanrodrigues.travelapi.airport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AirportRequestDTO(
        @NotBlank
        @Size(min = 3, max = 3)
        String iataCode,

        @Size(max = 4)
        String icaoCode,

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

        @Size(max = 2)
        String continent,

        @Size(max = 30)
        String type,

        String keywords,

        @Size(max = 255)
        String homeLink,

        @Size(max = 255)
        String wikipediaLink,

        @NotNull
        Double latitude,

        @NotNull
        Double longitude
) {
}
