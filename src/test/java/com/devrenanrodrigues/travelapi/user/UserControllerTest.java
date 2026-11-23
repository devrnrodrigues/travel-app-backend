package com.devrenanrodrigues.travelapi.user;

import com.devrenanrodrigues.travelapi.exception.GlobalExceptionHandler;
import com.devrenanrodrigues.travelapi.user.dto.UpdateProfileRequestDTO;
import com.devrenanrodrigues.travelapi.user.dto.UserResponseDTO;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final UUID testUserId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private Jwt testJwt;

    @BeforeEach
    void setUp() {
        testJwt = new Jwt(
                "mock-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", testUserId.toString(), "email", "renan@teste.com", "role", "USER")
        );

        HandlerMethodArgumentResolver jwtArgumentResolver = new HandlerMethodArgumentResolver() {
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

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(jwtArgumentResolver)
                .build();
    }

    private UserResponseDTO createTestUserResponse(String fullName, String bio, String nationality, Long commentsCount) {
        return new UserResponseDTO(
                testUserId,
                "renan@teste.com",
                fullName,
                "http://avatar.jpg",
                bio,
                nationality,
                AuthProvider.LOCAL,
                Role.USER,
                Instant.now(),
                commentsCount
        );
    }

    @Test
    @DisplayName("GET /api/users/me - Deve retornar perfil do usuário autenticado com status 200 OK")
    void shouldReturnAuthenticatedUserProfile() throws Exception {
        UserResponseDTO userDTO = createTestUserResponse("Renan Rodrigues", "Bio do dev", "Brasil", 12L);

        when(userService.getProfile(testUserId)).thenReturn(userDTO);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.fullName").value("Renan Rodrigues"))
                .andExpect(jsonPath("$.email").value("renan@teste.com"))
                .andExpect(jsonPath("$.commentsCount").value(12));
    }

    @Test
    @DisplayName("GET /api/users/me - Deve retornar 404 quando usuário não for encontrado")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getProfile(testUserId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Usuário não encontrado."));
    }

    @Test
    @DisplayName("PUT /api/users/profile - Deve atualizar perfil com sucesso e retornar 200 OK")
    void shouldUpdateProfileAndReturn200() throws Exception {
        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO("Renan Atualizado", "Nova bio", "Brasil");
        UserResponseDTO userDTO = createTestUserResponse("Renan Atualizado", "Nova bio", "Brasil", 5L);

        when(userService.updateProfile(eq(testUserId), any(UpdateProfileRequestDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Renan Atualizado"))
                .andExpect(jsonPath("$.bio").value("Nova bio"));
    }

    @Test
    @DisplayName("PUT /api/users/profile - Deve retornar 400 Bad Request quando campos excederem tamanho máximo")
    void shouldReturn400WhenProfileFieldsExceedMaxSize() throws Exception {
        String longText = "a".repeat(151);
        UpdateProfileRequestDTO invalidRequest = new UpdateProfileRequestDTO(longText, "bio", "País");

        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.fullName").exists());
    }

    @Test
    @DisplayName("POST /api/users/avatar - Deve fazer upload de avatar e retornar 200 OK")
    void shouldUploadAvatarAndReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "fake-image-bytes".getBytes());
        UserResponseDTO userDTO = new UserResponseDTO(
                testUserId,
                "renan@teste.com",
                "Renan Rodrigues",
                "http://cloudinary.com/avatar.jpg",
                null,
                null,
                AuthProvider.LOCAL,
                Role.USER,
                Instant.now(),
                0L
        );

        when(userService.updateAvatar(eq(testUserId), any())).thenReturn(userDTO);

        mockMvc.perform(multipart("/api/users/avatar")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("http://cloudinary.com/avatar.jpg"));
    }
}
