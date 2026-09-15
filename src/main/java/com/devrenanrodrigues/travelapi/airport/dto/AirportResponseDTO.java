package com.devrenanrodrigues.travelapi.airport.dto;

import com.devrenanrodrigues.travelapi.airport.Airport;

import java.util.UUID;

public record AirportResponseDTO(
        UUID id,
        String iataCode,
        String name,
        String city,
        String country,
        Double latitude,
        Double longitude
) {
    public static AirportResponseDTO fromEntity(Airport airport) {
        return new AirportResponseDTO(
                airport.getId(),
                airport.getIataCode(),
                airport.getName(),
                airport.getCity(),
                airport.getCountry(),
                airport.getLatitude(),
                airport.getLongitude()
        );
    }
}
