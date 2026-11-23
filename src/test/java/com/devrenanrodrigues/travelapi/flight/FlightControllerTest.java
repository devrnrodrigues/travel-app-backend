package com.devrenanrodrigues.travelapi.flight;

import com.devrenanrodrigues.travelapi.exception.GlobalExceptionHandler;
import com.devrenanrodrigues.travelapi.flight.dto.FlightCardResponseDTO;
import com.devrenanrodrigues.travelapi.flight.dto.FlightSearchRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FlightControllerTest {

    @Mock
    private FlightService flightService;

    @InjectMocks
    private FlightController flightController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(flightController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/flights/search - Deve retornar ofertas de voos com 200 OK")
    void shouldSearchFlightsSuccessfully() throws Exception {
        FlightCardResponseDTO card = FlightCardResponseDTO.builder()
                .id("card-1")
                .airlineName("LATAM Airlines")
                .totalPrice(new BigDecimal("1250.00"))
                .currency("BRL")
                .formattedPrice("R$ 1.250,00")
                .build();

        when(flightService.searchFlights(any(FlightSearchRequestDTO.class))).thenReturn(List.of(card));

        mockMvc.perform(get("/api/flights/search")
                        .param("fromId", "GRU.AIRPORT")
                        .param("toId", "GIG.AIRPORT")
                        .param("departDate", "2026-12-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("card-1"))
                .andExpect(jsonPath("$[0].airlineName").value("LATAM Airlines"))
                .andExpect(jsonPath("$[0].totalPrice").value(1250.00));
    }

    @Test
    @DisplayName("GET /api/flights/search - Deve retornar 400 Bad Request quando campos obrigatórios estiverem ausentes")
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        mockMvc.perform(get("/api/flights/search")
                        .param("fromId", "")
                        .param("toId", "GIG.AIRPORT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.fromId").exists())
                .andExpect(jsonPath("$.fieldErrors.departDate").exists());
    }
}
