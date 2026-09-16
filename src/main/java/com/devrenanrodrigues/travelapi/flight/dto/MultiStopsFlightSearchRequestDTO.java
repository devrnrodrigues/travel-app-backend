package com.devrenanrodrigues.travelapi.flight.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record MultiStopsFlightSearchRequestDTO(
    @NotBlank(message = "As etapas de voo (legs) são obrigatórias")
    String legs,

    Integer pageNo,
    Integer adults,
    String children,
    String sort,
    String cabinClass,
    String currencyCode
) {
    public MultiStopsFlightSearchRequestDTO {
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (adults == null || adults < 1) {
            adults = 1;
        }
        if (sort == null || sort.isBlank()) {
            sort = "CHEAPEST";
        }
        if (cabinClass == null || cabinClass.isBlank()) {
            cabinClass = "ECONOMY";
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            currencyCode = "BRL";
        }
    }
}
