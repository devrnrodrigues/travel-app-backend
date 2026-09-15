package com.devrenanrodrigues.travelapi.airport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AirportRequestDTO(
        @NotBlank
        @Size(min = 3, max = 3)
        String iataCode,

        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Size(max = 100)
        String city,

        @NotBlank
        @Size(max = 100)
        String country,

        @NotNull
        Double latitude,

        @NotNull
        Double longitude
) {
}
