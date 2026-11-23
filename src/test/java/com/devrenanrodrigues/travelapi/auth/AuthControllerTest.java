package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.auth.dto.AuthResponseDTO;
import com.devrenanrodrigues.travelapi.auth.dto.GoogleLoginRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.LoginRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.RefreshTokenRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.RegisterRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.TokenResponseDTO;
import com.devrenanrodrigues.travelapi.exception.GlobalExceptionHandler;
import com.devrenanrodrigues.travelapi.user.AuthProvider;
import com.devrenanrodrigues.travelapi.user.Role;
import com.devrenanrodrigues.travelapi.user.dto.UserResponseDTO;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private UserResponseDTO createTestUserDTO(String email, String fullName, AuthProvider provider) {
        return new UserResponseDTO(
                UUID.randomUUID(),
                email,
                fullName,
                null,
                null,
                null,
                provider,
                Role.USER,
                Instant.now(),
                0L
        );
    }

    @Test
    @DisplayName("POST /api/auth/register - Deve registrar usuário e retornar 201 Created")
    void shouldRegisterUserAndReturn201() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("Renan Rodrigues", "renan@teste.com", "SenhaForte@123");
        UserResponseDTO userDTO = createTestUserDTO("renan@teste.com", "Renan Rodrigues", AuthProvider.LOCAL);
        AuthResponseDTO response = AuthResponseDTO.of("token-123", "refresh-123", 3600L, userDTO);

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("renan@teste.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Deve retornar 400 Bad Request quando payload for inválido")
    void shouldReturn400WhenRegisterPayloadIsInvalid() throws Exception {
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO("", "email-invalido", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve autenticar e retornar 200 OK")
    void shouldLoginAndReturn200() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("renan@teste.com", "SenhaForte@123");
        UserResponseDTO userDTO = createTestUserDTO("renan@teste.com", "Renan", AuthProvider.LOCAL);
        AuthResponseDTO response = AuthResponseDTO.of("jwt-token", "refresh-token", 3600L, userDTO);

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("renan@teste.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve retornar 401 Unauthorized quando credenciais forem inválidas")
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("renan@teste.com", "SenhaErrada@123");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));
    }

    @Test
    @DisplayName("POST /api/auth/google - Deve autenticar com token do Google e retornar 200 OK")
    void shouldLoginWithGoogleAndReturn200() throws Exception {
        GoogleLoginRequestDTO request = new GoogleLoginRequestDTO("google-token-xyz");
        UserResponseDTO userDTO = createTestUserDTO("google@teste.com", "Google User", AuthProvider.GOOGLE);
        AuthResponseDTO response = AuthResponseDTO.of("jwt-google", "refresh-google", 3600L, userDTO);

        when(authService.loginWithGoogle(any(GoogleLoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-google"))
                .andExpect(jsonPath("$.user.email").value("google@teste.com"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Deve rotacionar token e retornar 200 OK com novo access token")
    void shouldRefreshTokenAndReturn200() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("valid-refresh-token");
        TokenResponseDTO response = TokenResponseDTO.of("new-access-token", "new-refresh-token", 3600L);

        when(authService.refreshToken(any(RefreshTokenRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - Deve revogar token e retornar 204 No Content")
    void shouldLogoutAndReturn204() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("valid-refresh-token");

        doNothing().when(authService).logout(any(RefreshTokenRequestDTO.class));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}
