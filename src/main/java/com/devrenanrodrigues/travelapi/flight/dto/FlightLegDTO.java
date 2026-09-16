package com.devrenanrodrigues.travelapi.flight.dto;

import lombok.Builder;

@Builder
public record FlightLegDTO(
    String label,
    String departureTime,
    String arrivalTime,
    String duration,
    String originAirport,
    String destinationAirport,
    String departureDate
) {}
