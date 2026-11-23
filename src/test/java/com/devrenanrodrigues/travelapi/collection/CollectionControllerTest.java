package com.devrenanrodrigues.travelapi.collection;

import com.devrenanrodrigues.travelapi.collection.dto.CollectionResponseDTO;
import com.devrenanrodrigues.travelapi.collection.dto.UpdateCollectionRequestDTO;
import com.devrenanrodrigues.travelapi.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CollectionControllerTest {

    @Mock
    private CollectionService collectionService;

    @InjectMocks
    private CollectionController collectionController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID testUserId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private Jwt testJwt;

    @BeforeEach
    void setUp() {
        testJwt = new Jwt(
                "mock-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", testUserId.toString(), "role", "USER")
        );

        HandlerMethodArgumentResolver jwtResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(Jwt.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testJwt;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(collectionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(jwtResolver)
                .build();
    }

    @Test
    @DisplayName("GET /api/collections - Deve retornar lista de coleções do usuário com status 200 OK")
    void shouldReturnUserCollections() throws Exception {
        UUID id = UUID.randomUUID();
        CollectionResponseDTO dto = new CollectionResponseDTO(id, "Minhas Viagens", List.of(), Instant.now(), Instant.now());

        when(collectionService.getCollections(testUserId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].title").value("Minhas Viagens"));
    }

    @Test
    @DisplayName("GET /api/collections/{id} - Deve retornar coleção por ID com status 200 OK")
    void shouldReturnCollectionById() throws Exception {
        UUID id = UUID.randomUUID();
        CollectionResponseDTO dto = new CollectionResponseDTO(id, "Férias de Verão", List.of(), Instant.now(), Instant.now());

        when(collectionService.getCollectionById(id, testUserId)).thenReturn(dto);

        mockMvc.perform(get("/api/collections/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Férias de Verão"));
    }

    @Test
    @DisplayName("POST /api/collections - Deve criar coleção via multipart e retornar 200 OK")
    void shouldCreateCollection() throws Exception {
        UUID id = UUID.randomUUID();
        CollectionResponseDTO dto = new CollectionResponseDTO(id, "Roadtrip", List.of(), Instant.now(), Instant.now());

        when(collectionService.createCollection(eq(testUserId), eq("Roadtrip"), any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/collections")
                        .param("title", "Roadtrip"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Roadtrip"));
    }

    @Test
    @DisplayName("PUT /api/collections/{id} - Deve atualizar título da coleção e retornar 200 OK")
    void shouldUpdateCollection() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateCollectionRequestDTO request = new UpdateCollectionRequestDTO("Novo Título", null);
        CollectionResponseDTO dto = new CollectionResponseDTO(id, "Novo Título", List.of(), Instant.now(), Instant.now());

        when(collectionService.updateCollection(eq(id), eq(testUserId), any(UpdateCollectionRequestDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/collections/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Novo Título"));
    }

    @Test
    @DisplayName("DELETE /api/collections/{id} - Deve remover coleção e retornar 204 No Content")
    void shouldDeleteCollection() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(collectionService).deleteCollection(id, testUserId);

        mockMvc.perform(delete("/api/collections/{id}", id))
                .andExpect(status().isNoContent());
    }
}
