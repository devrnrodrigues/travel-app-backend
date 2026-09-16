package com.devrenanrodrigues.travelapi.flight.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rapidapi.booking")
@Getter
@Setter
public class BookingProperties {
    private String baseUrl;
    private String key;
    private String host;
}
