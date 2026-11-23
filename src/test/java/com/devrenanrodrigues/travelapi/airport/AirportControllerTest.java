package com.devrenanrodrigues.travelapi.airport;

import com.devrenanrodrigues.travelapi.airport.dto.AirportRequestDTO;
import com.devrenanrodrigues.travelapi.airport.dto.AirportResponseDTO;
import com.devrenanrodrigues.travelapi.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AirportControllerTest {

    @Mock
    private AirportService airportService;

    @InjectMocks
    private AirportController airportController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(airportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private AirportResponseDTO createTestAirport(UUID id, String iata, String name, String city) {
        return new AirportResponseDTO(
                id, iata, null, name, city, "SP", "Brasil", "SA", "large_airport", null, null, null, -23.0, -46.0
        );
    }

    @Test
    @DisplayName("GET /api/airports - Deve retornar lista de aeroportos com 200 OK")
    void shouldReturnAllAirports() throws Exception {
        UUID id = UUID.randomUUID();
        when(airportService.findAll()).thenReturn(List.of(createTestAirport(id, "GRU", "Guarulhos", "São Paulo")));

        mockMvc.perform(get("/api/airports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iataCode").value("GRU"))
                .andExpect(jsonPath("$[0].city").value("São Paulo"));
    }

    @Test
    @DisplayName("GET /api/airports/{id} - Deve retornar aeroporto por ID com 200 OK")
    void shouldReturnAirportById() throws Exception {
        UUID id = UUID.randomUUID();
        when(airportService.findById(id)).thenReturn(createTestAirport(id, "CGH", "Congonhas", "São Paulo"));

        mockMvc.perform(get("/api/airports/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("CGH"));
    }

    @Test
    @DisplayName("POST /api/airports - Deve criar aeroporto com status 201 Created")
    void shouldCreateAirportAndReturn201() throws Exception {
        AirportRequestDTO request = new AirportRequestDTO(
                "BSB", "SBBR", "Brasília", "Brasília", "DF", "Brasil", "SA", "large_airport", null, null, null, -15.8697, -47.9172
        );
        UUID id = UUID.randomUUID();
        when(airportService.create(any(AirportRequestDTO.class))).thenReturn(createTestAirport(id, "BSB", "Brasília", "Brasília"));

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iataCode").value("BSB"));
    }

    @Test
    @DisplayName("DELETE /api/airports/{id} - Deve excluir aeroporto com status 204 No Content")
    void shouldDeleteAirportAndReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(airportService).delete(id);

        mockMvc.perform(delete("/api/airports/{id}", id))
                .andExpect(status().isNoContent());
    }
}
