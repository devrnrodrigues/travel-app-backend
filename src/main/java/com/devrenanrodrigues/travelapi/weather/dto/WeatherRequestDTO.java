package com.devrenanrodrigues.travelapi.weather.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WeatherRequestDTO(
        @NotNull
        Double temperature,

        @NotNull
        Double windSpeed,

        @NotNull
        @Min(0)
        @Max(100)
        Integer rainProbability,

        @NotBlank
        @Size(max = 50)
        String conditionText
) {
}
