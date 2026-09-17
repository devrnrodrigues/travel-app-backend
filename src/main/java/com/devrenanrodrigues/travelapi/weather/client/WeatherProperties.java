package com.devrenanrodrigues.travelapi.weather.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "weather.open-meteo")
@Getter
@Setter
public class WeatherProperties {
    private String baseUrl = "https://api.open-meteo.com";
    private int cacheTtlHours = 2;
}
