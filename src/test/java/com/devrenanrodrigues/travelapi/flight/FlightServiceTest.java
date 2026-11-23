package com.devrenanrodrigues.travelapi.flight;

import com.devrenanrodrigues.travelapi.flight.client.BookingFlightClient;
import com.devrenanrodrigues.travelapi.flight.dto.FlightCardResponseDTO;
import com.devrenanrodrigues.travelapi.flight.dto.FlightSearchRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private BookingFlightClient bookingFlightClient;

    @InjectMocks
    private FlightService flightService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FlightSearchRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = FlightSearchRequestDTO.builder()
                .fromId("GRU.AIRPORT")
                .toId("CDG.AIRPORT")
                .departDate("2026-12-01")
                .currencyCode("BRL")
                .build();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando resposta da API de voos for nula")
    void shouldReturnEmptyListWhenRootNodeIsNull() {
        when(bookingFlightClient.searchFlights(any())).thenReturn(null);

        List<FlightCardResponseDTO> result = flightService.searchFlights(sampleRequest);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver ofertas de voo na resposta")
    void shouldReturnEmptyListWhenOffersEmpty() throws Exception {
        JsonNode emptyNode = objectMapper.readTree("{\"status\": true, \"data\": {\"flightOffers\": []}}");
        when(bookingFlightClient.searchFlights(any())).thenReturn(emptyNode);

        List<FlightCardResponseDTO> result = flightService.searchFlights(sampleRequest);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar 502 Bad Gateway quando a API externa retornar mensagem de erro")
    void shouldThrow502WhenExternalApiReturnsError() throws Exception {
        JsonNode errorNode = objectMapper.readTree("{\"status\": false, \"message\": \"Rate limit exceeded on RapidAPI\"}");
        when(bookingFlightClient.searchFlights(any())).thenReturn(errorNode);

        assertThatThrownBy(() -> flightService.searchFlights(sampleRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(rse.getReason()).contains("Rate limit exceeded");
                });
    }

    @Test
    @DisplayName("Deve processar e converter ofertas de voo com sucesso")
    void shouldParseFlightOffersSuccessfully() throws Exception {
        String json = """
        {
          "status": true,
          "data": {
            "aggregation": {
              "airlines": [
                {"iataCode": "AF", "name": "Air France"}
              ]
            },
            "flightOffers": [
              {
                "token": "offer-1",
                "priceBreakdown": {
                  "total": {
                    "currency": "BRL",
                    "units": 3500,
                    "nanos": 0
                  }
                },
                "segments": [
                  {
                    "departureAirport": {"code": "GRU", "name": "Guarulhos"},
                    "arrivalAirport": {"code": "CDG", "name": "Charles de Gaulle"},
                    "departureTime": "2026-12-01T18:00:00",
                    "arrivalTime": "2026-12-02T10:00:00",
                    "legs": [
                      {
                        "departureTime": "2026-12-01T18:00:00",
                        "arrivalTime": "2026-12-02T10:00:00",
                        "origin": {"code": "GRU", "name": "Guarulhos", "cityName": "São Paulo"},
                        "destination": {"code": "CDG", "name": "Charles de Gaulle", "cityName": "Paris"},
                        "flightInfo": {
                          "carrierInfo": {
                            "operatingCarrier": "AF",
                            "operatingCarrierName": "Air France"
                          }
                        }
                      }
                    ]
                  }
                ]
              }
            ]
          }
        }
        """;

        JsonNode rootNode = objectMapper.readTree(json);
        when(bookingFlightClient.searchFlights(any())).thenReturn(rootNode);

        List<FlightCardResponseDTO> result = flightService.searchFlights(sampleRequest);

        assertThat(result).hasSize(1);
        FlightCardResponseDTO card = result.getFirst();
        assertThat(card.id()).isEqualTo("offer-1");
        assertThat(card.airlineName()).isEqualTo("Air France");
        assertThat(card.totalPrice()).isEqualByComparingTo("3500");
    }
}
