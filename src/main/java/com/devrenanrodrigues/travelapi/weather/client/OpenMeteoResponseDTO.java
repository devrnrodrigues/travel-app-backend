package com.devrenanrodrigues.travelapi.weather.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponseDTO(
        @JsonProperty("current") CurrentWeather current
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentWeather(
            @JsonProperty("temperature_2m") Double temperature,
            @JsonProperty("wind_speed_10m") Double windSpeed,
            @JsonProperty("precipitation_probability") Integer precipitationProbability,
            @JsonProperty("relative_humidity_2m") Integer relativeHumidity,
            @JsonProperty("weather_code") Integer weatherCode
    ) {
    }
}
