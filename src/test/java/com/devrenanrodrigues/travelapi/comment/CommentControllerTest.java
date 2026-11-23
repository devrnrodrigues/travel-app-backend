package com.devrenanrodrigues.travelapi.comment;

import com.devrenanrodrigues.travelapi.comment.dto.CommentRequestDTO;
import com.devrenanrodrigues.travelapi.comment.dto.CommentResponseDTO;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
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
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID testUserId = UUID.fromString("44444444-4444-4444-4444-444444444444");
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

        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(jwtResolver)
                .build();
    }

    @Test
    @DisplayName("POST /api/destinations/{id}/comments - Deve criar comentário e retornar 201 Created")
    void shouldCreateCommentAndReturn201() throws Exception {
        UUID destId = UUID.randomUUID();
        CommentRequestDTO request = new CommentRequestDTO(5, "Excelente destino!");
        CommentResponseDTO response = new CommentResponseDTO(
                UUID.randomUUID(), testUserId, destId, 5, "Excelente destino!",
                Instant.now(), Instant.now(), "Renan", null, 0, false, List.of()
        );

        when(commentService.create(eq(destId), eq(testUserId), any(CommentRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/destinations/{destinationId}/comments", destId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("Excelente destino!"));
    }

    @Test
    @DisplayName("GET /api/destinations/{id}/comments - Deve retornar sumário de avaliações com 200 OK")
    void shouldReturnCommentsSummary() throws Exception {
        UUID destId = UUID.randomUUID();
        DestinationCommentsSummaryDTO summary = new DestinationCommentsSummaryDTO(destId, 4.8, 5L, List.of());

        when(commentService.findByDestinationId(eq(destId), any())).thenReturn(summary);

        mockMvc.perform(get("/api/destinations/{destinationId}/comments", destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinationId").value(destId.toString()))
                .andExpect(jsonPath("$.averageRating").value(4.8))
                .andExpect(jsonPath("$.totalComments").value(5));
    }
}
