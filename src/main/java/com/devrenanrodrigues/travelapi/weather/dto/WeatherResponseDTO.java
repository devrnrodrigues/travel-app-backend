package com.devrenanrodrigues.travelapi.weather.dto;

import com.devrenanrodrigues.travelapi.weather.DestinationWeather;

import java.time.Instant;
import java.util.UUID;

public record WeatherResponseDTO(
        UUID destinationId,
        Double temperature,
        Double windSpeed,
        Integer rainProbability,
        String conditionText,
        Instant updatedAt
) {
    public static WeatherResponseDTO fromEntity(DestinationWeather weather) {
        return new WeatherResponseDTO(
                weather.getDestinationId(),
                weather.getTemperature(),
                weather.getWindSpeed(),
                weather.getRainProbability(),
                weather.getConditionText(),
                weather.getUpdatedAt()
        );
    }
}
