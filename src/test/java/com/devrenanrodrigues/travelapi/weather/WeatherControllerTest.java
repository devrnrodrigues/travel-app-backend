package com.devrenanrodrigues.travelapi.weather;

import com.devrenanrodrigues.travelapi.exception.GlobalExceptionHandler;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherRequestDTO;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherController weatherController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(weatherController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/destinations/{id}/weather - Deve retornar clima do destino com 200 OK")
    void shouldReturnWeatherByDestinationId() throws Exception {
        UUID destId = UUID.randomUUID();
        WeatherResponseDTO dto = new WeatherResponseDTO(destId, 26.5, 14.0, 20, "Parcialmente nublado", Instant.now());

        when(weatherService.findByDestinationId(destId)).thenReturn(dto);

        mockMvc.perform(get("/api/destinations/{destinationId}/weather", destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(26.5))
                .andExpect(jsonPath("$.conditionText").value("Parcialmente nublado"));
    }

    @Test
    @DisplayName("GET /api/destinations/{id}/weather - Deve retornar 404 quando destino não for encontrado")
    void shouldReturn404WhenWeatherNotFound() throws Exception {
        UUID destId = UUID.randomUUID();
        when(weatherService.findByDestinationId(destId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Destino não encontrado."));

        mockMvc.perform(get("/api/destinations/{destinationId}/weather", destId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/destinations/{id}/weather - Deve atualizar clima com 200 OK")
    void shouldUpsertWeather() throws Exception {
        UUID destId = UUID.randomUUID();
        WeatherRequestDTO request = new WeatherRequestDTO(22.0, 8.0, 10, "Ensolarado");
        WeatherResponseDTO response = new WeatherResponseDTO(destId, 22.0, 8.0, 10, "Ensolarado", Instant.now());

        when(weatherService.upsert(eq(destId), any(WeatherRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/destinations/{destinationId}/weather", destId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(22.0));
    }

    @Test
    @DisplayName("DELETE /api/destinations/{id}/weather - Deve excluir dados de clima com 204 No Content")
    void shouldDeleteWeather() throws Exception {
        UUID destId = UUID.randomUUID();
        doNothing().when(weatherService).delete(destId);

        mockMvc.perform(delete("/api/destinations/{destinationId}/weather", destId))
                .andExpect(status().isNoContent());
    }
}
