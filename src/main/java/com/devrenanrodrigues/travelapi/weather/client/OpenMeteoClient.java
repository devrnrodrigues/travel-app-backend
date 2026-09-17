package com.devrenanrodrigues.travelapi.weather.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);

    private final RestClient restClient;

    public OpenMeteoClient(WeatherProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Optional<OpenMeteoResponseDTO> fetchCurrentWeather(double latitude, double longitude) {
        try {
            OpenMeteoResponseDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("current", "temperature_2m,relative_humidity_2m,precipitation_probability,wind_speed_10m,weather_code")
                            .queryParam("wind_speed_unit", "kmh")
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponseDTO.class);

            return Optional.ofNullable(response);
        } catch (Exception ex) {
            log.warn("Falha ao consultar API Open-Meteo para latitude={} longitude={}: {}", latitude, longitude, ex.getMessage());
            return Optional.empty();
        }
    }
}
