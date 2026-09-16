package com.devrenanrodrigues.travelapi.flight.client;

import com.devrenanrodrigues.travelapi.flight.dto.FlightSearchRequestDTO;
import com.devrenanrodrigues.travelapi.flight.dto.MultiStopsFlightSearchRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class BookingFlightClient {

    private final RestClient restClient;
    private final BookingProperties properties;
    private final ObjectMapper objectMapper;

    public BookingFlightClient(BookingProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("x-rapidapi-key", properties.getKey())
                .defaultHeader("x-rapidapi-host", properties.getHost())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public JsonNode searchFlights(FlightSearchRequestDTO request) {
        try {
            String rawJson = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/v1/flights/searchFlights")
                                .queryParam("fromId", request.fromId())
                                .queryParam("toId", request.toId())
                                .queryParam("departDate", request.departDate())
                                .queryParam("pageNo", request.pageNo())
                                .queryParam("adults", request.adults())
                                .queryParam("sort", request.sort())
                                .queryParam("cabinClass", request.cabinClass())
                                .queryParam("currency_code", request.currencyCode());

                        if (request.returnDate() != null && !request.returnDate().isBlank()) {
                            uriBuilder.queryParam("returnDate", request.returnDate());
                        }
                        if (request.children() != null && !request.children().isBlank()) {
                            uriBuilder.queryParam("children", request.children());
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(String.class);

            return objectMapper.readTree(rawJson);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao consultar a API de voos externa: " + ex.getMessage(), ex);
        }
    }

    public JsonNode searchFlightsMultiStops(MultiStopsFlightSearchRequestDTO request) {
        try {
            String rawJson = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/v1/flights/searchFlightsMultiStops")
                                .queryParam("legs", request.legs())
                                .queryParam("pageNo", request.pageNo())
                                .queryParam("adults", request.adults())
                                .queryParam("sort", request.sort())
                                .queryParam("cabinClass", request.cabinClass())
                                .queryParam("currency_code", request.currencyCode());

                        if (request.children() != null && !request.children().isBlank()) {
                            uriBuilder.queryParam("children", request.children());
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(String.class);

            return objectMapper.readTree(rawJson);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao consultar a API de voos externa: " + ex.getMessage(), ex);
        }
    }
}
