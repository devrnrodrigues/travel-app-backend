package com.devrenanrodrigues.travelapi.favorite;

import com.devrenanrodrigues.travelapi.exception.GlobalExceptionHandler;
import com.devrenanrodrigues.travelapi.favorite.dto.FavoriteResponseDTO;
import com.devrenanrodrigues.travelapi.favorite.dto.FavoriteStatusDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private FavoriteController favoriteController;

    private MockMvc mockMvc;
    private final UUID testUserId = UUID.fromString("33333333-3333-3333-3333-333333333333");
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

        mockMvc = MockMvcBuilders.standaloneSetup(favoriteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(jwtResolver, new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("POST /api/favorites/{destinationId} - Deve favoritar destino e retornar 201 Created")
    void shouldAddFavoriteAndReturn201() throws Exception {
        UUID destId = UUID.randomUUID();
        FavoriteResponseDTO dto = new FavoriteResponseDTO(
                testUserId, destId, "Barcelona", "Barcelona", "Espanha",
                List.of("Urbano"), "http://cover.jpg", 4.7, 15, 4.7, 15, Instant.now(), null
        );

        when(favoriteService.addFavorite(destId, testUserId)).thenReturn(dto);

        mockMvc.perform(post("/api/favorites/{destinationId}", destId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.destinationName").value("Barcelona"));
    }

    @Test
    @DisplayName("DELETE /api/favorites/{destinationId} - Deve desfavoritar destino e retornar 204 No Content")
    void shouldRemoveFavoriteAndReturn204() throws Exception {
        UUID destId = UUID.randomUUID();
        doNothing().when(favoriteService).removeFavorite(destId, testUserId);

        mockMvc.perform(delete("/api/favorites/{destinationId}", destId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/favorites - Deve retornar favoritos paginados com status 200 OK")
    void shouldFindFavoritesPaged() throws Exception {
        UUID destId = UUID.randomUUID();
        FavoriteResponseDTO dto = new FavoriteResponseDTO(
                testUserId, destId, "Londres", "Londres", "Reino Unido",
                List.of("Cidades"), "http://cover.jpg", 4.6, 20, 4.6, 20, Instant.now(), null
        );

        when(favoriteService.findFavorites(eq(testUserId), eq(false), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].destinationName").value("Londres"));
    }
}
