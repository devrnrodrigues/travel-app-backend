package com.devrenanrodrigues.travelapi.flight.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record FlightSearchRequestDTO(
    @NotBlank(message = "O aeroporto de origem (fromId) é obrigatório")
    String fromId,

    @NotBlank(message = "O aeroporto de destino (toId) é obrigatório")
    String toId,

    @NotBlank(message = "A data de ida (departDate) é obrigatória")
    String departDate,

    String returnDate,
    Integer pageNo,
    Integer adults,
    String children,
    String sort,
    String cabinClass,
    String currencyCode
) {
    public FlightSearchRequestDTO {
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
