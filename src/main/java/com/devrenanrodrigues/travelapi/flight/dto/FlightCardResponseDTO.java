package com.devrenanrodrigues.travelapi.flight.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record FlightCardResponseDTO(
    String id,
    String airlineName,
    String airlineLogoUrl,
    int stopsCount,
    String stopsLabel,
    FlightLegDTO outbound,
    FlightLegDTO inbound,
    List<FlightLegDTO> allLegs,
    String baggageInfo,
    String currency,
    BigDecimal totalPrice,
    String formattedPrice,
    String bookingUrl
) {}
