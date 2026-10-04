package com.devrenanrodrigues.travelapi.airport.dto;

import com.devrenanrodrigues.travelapi.airport.Airport;

import java.util.UUID;

public record AirportResponseDTO(
        UUID id,
        String iataCode,
        String icaoCode,
        String name,
        String city,
        String state,
        String country,
        String continent,
        String type,
        String keywords,
        String homeLink,
        String wikipediaLink,
        Double latitude,
        Double longitude
) {
    public static AirportResponseDTO fromEntity(Airport airport) {
        return new AirportResponseDTO(
                airport.getId(),
                airport.getIataCode(),
                airport.getIcaoCode(),
                airport.getName(),
                airport.getCity(),
                airport.getState(),
                airport.getCountry(),
                airport.getContinent(),
                airport.getType(),
                airport.getKeywords(),
                airport.getHomeLink(),
                airport.getWikipediaLink(),
                airport.getLatitude(),
                airport.getLongitude()
        );
    }
}
