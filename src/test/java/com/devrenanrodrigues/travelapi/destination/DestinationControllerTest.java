package com.devrenanrodrigues.travelapi.destination;

import com.devrenanrodrigues.travelapi.destination.dto.DestinationDetailResponseDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationRequestDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationResponseDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationSummaryResponseDTO;
import com.devrenanrodrigues.travelapi.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DestinationControllerTest {

    @Mock
    private DestinationService destinationService;

    @InjectMocks
    private DestinationController destinationController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(destinationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private DestinationResponseDTO createResponseDTO(UUID id, String name, String country) {
        return new DestinationResponseDTO(
                id, "OPO", "OPO", name, "Porto", null, country,
                List.of("Urbano"), 4.9, 20, AiStatus.COMPLETED, 41.1579, -8.6291,
                "porto view", "http://porto.jpg", List.of(), List.of("Vinho do Porto"),
                null, 230000L, 88, Instant.now(), Instant.now(), Instant.now()
        );
    }

    @Test
    @DisplayName("GET /api/destinations - Deve retornar destinos paginados com status 200 OK")
    void shouldFindAllDestinationsPaged() throws Exception {
        UUID id = UUID.randomUUID();
        DestinationSummaryResponseDTO summary = new DestinationSummaryResponseDTO(
                id, "Porto", "Porto", null, "Portugal", List.of("Urbano"), "http://cover.jpg", "porto", 4.9, 20, null
        );

        when(destinationService.findAll(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/destinations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Porto"))
                .andExpect(jsonPath("$.content[0].country").value("Portugal"));
    }

    @Test
    @DisplayName("GET /api/destinations/{id} - Deve retornar detalhes do destino com status 200 OK")
    void shouldFindDestinationById() throws Exception {
        UUID id = UUID.randomUUID();
        Destination dest = Destination.builder()
                .id(id)
                .name("Lisboa")
                .city("Lisboa")
                .country("Portugal")
                .latitude(38.7223)
                .longitude(-9.1393)
                .build();

        DestinationDetailResponseDTO detail = DestinationDetailResponseDTO.of(dest, null, null, null);

        when(destinationService.findById(id)).thenReturn(detail);

        mockMvc.perform(get("/api/destinations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lisboa"));
    }

    @Test
    @DisplayName("GET /api/destinations/{id} - Deve retornar 404 quando destino não existir")
    void shouldReturn404WhenDestinationNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(destinationService.findById(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Destino não encontrado."));

        mockMvc.perform(get("/api/destinations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/destinations - Deve criar destino e retornar 201 Created")
    void shouldCreateDestinationAndReturn201() throws Exception {
        DestinationRequestDTO request = new DestinationRequestDTO(
                "OPO", "Porto", "Porto", null, "Portugal", "Urbano", List.of("Cultura"),
                4.9, AiStatus.COMPLETED, 41.1579, -8.6291, "porto query", null, null, null, null, 80
        );
        UUID id = UUID.randomUUID();
        DestinationResponseDTO response = createResponseDTO(id, "Porto", "Portugal");

        when(destinationService.create(any(DestinationRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Porto"));
    }

    @Test
    @DisplayName("POST /api/destinations - Deve retornar 400 Bad Request quando campos obrigatórios estiverem ausentes")
    void shouldReturn400WhenDestinationRequestIsInvalid() throws Exception {
        DestinationRequestDTO invalid = new DestinationRequestDTO(
                null, "", "", null, "", null, null, null, null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.latitude").exists());
    }

    @Test
    @DisplayName("DELETE /api/destinations/{id} - Deve remover destino e retornar 204 No Content")
    void shouldDeleteDestinationAndReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(destinationService).delete(id);

        mockMvc.perform(delete("/api/destinations/{id}", id))
                .andExpect(status().isNoContent());
    }
}
