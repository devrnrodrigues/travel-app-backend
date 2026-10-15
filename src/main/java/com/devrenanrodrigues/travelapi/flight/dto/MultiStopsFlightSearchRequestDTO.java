package com.devrenanrodrigues.travelapi.flight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MultiStopsFlightSearchRequestDTO(
    @NotBlank(message = "As etapas de voo (legs) são obrigatórias")
    @Size(max = 500, message = "As etapas de voo devem ter no máximo 500 caracteres.")
    String legs,

    Integer pageNo,
    Integer adults,

    @Size(max = 50, message = "O campo de crianças deve ter no máximo 50 caracteres.")
    String children,

    @Size(max = 30, message = "A ordenação deve ter no máximo 30 caracteres.")
    String sort,

    @Size(max = 30, message = "A classe da cabine deve ter no máximo 30 caracteres.")
    String cabinClass,

    @Size(max = 10, message = "A moeda deve ter no máximo 10 caracteres.")
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
