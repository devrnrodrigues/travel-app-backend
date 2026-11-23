package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.auth.dto.AuthResponseDTO;
import com.devrenanrodrigues.travelapi.auth.dto.GoogleLoginRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.GoogleUserInfo;
import com.devrenanrodrigues.travelapi.auth.dto.LoginRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.RefreshTokenRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.RegisterRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.TokenResponseDTO;
import com.devrenanrodrigues.travelapi.comment.CommentRepository;
import com.devrenanrodrigues.travelapi.user.AuthProvider;
import com.devrenanrodrigues.travelapi.user.Role;
import com.devrenanrodrigues.travelapi.user.User;
import com.devrenanrodrigues.travelapi.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso e retornar tokens")
    void shouldRegisterUserSuccessfully() {
        RegisterRequestDTO request = new RegisterRequestDTO("Renan", "renan@teste.com", "SenhaForte@123");

        when(userRepository.existsByEmail("renan@teste.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("Renan")
                .email("renan@teste.com")
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("access-token-123");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        RefreshToken refreshToken = RefreshToken.builder().token("raw-refresh-token").build();
        when(refreshTokenService.createRefreshToken(savedUser)).thenReturn(refreshToken);

        AuthResponseDTO response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("raw-refresh-token");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.user().email()).isEqualTo("renan@teste.com");
    }

    @Test
    @DisplayName("Deve falhar ao registrar quando senha for igual ao nome ou email")
    void shouldFailRegisterWhenPasswordEqualsNameOrEmail() {
        RegisterRequestDTO request = new RegisterRequestDTO("Renan Rodrigues", "renan@teste.com", "renan");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("A senha não pode ser igual ao seu nome ou e-mail");
                });
    }

    @Test
    @DisplayName("Deve falhar com status 409 quando email já estiver em uso no registro")
    void shouldFailRegisterWhenEmailAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO("Renan", "renan@teste.com", "SenhaForte@123");
        when(userRepository.existsByEmail("renan@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).contains("Este e-mail já está em uso");
                });
    }

    @Test
    @DisplayName("Deve autenticar usuário local com sucesso e retornar contagem de comentários")
    void shouldLoginSuccessfully() {
        LoginRequestDTO request = new LoginRequestDTO("renan@teste.com", "SenhaForte@123");
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("renan@teste.com")
                .fullName("Renan")
                .passwordHash("hashed-password")
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("renan@teste.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SenhaForte@123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access-token-jwt");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        RefreshToken refreshToken = RefreshToken.builder().token("new-refresh-token").build();
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);
        when(commentRepository.countByUserId(userId)).thenReturn(5L);

        AuthResponseDTO response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-jwt");
        assertThat(response.user().commentsCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Deve lançar 401 quando senha estiver incorreta no login")
    void shouldThrow401WhenPasswordMismatch() {
        LoginRequestDTO request = new LoginRequestDTO("renan@teste.com", "SenhaErrada");
        User user = User.builder()
                .email("renan@teste.com")
                .passwordHash("hashed-password")
                .provider(AuthProvider.LOCAL)
                .build();

        when(userRepository.findByEmail("renan@teste.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SenhaErrada", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    @DisplayName("Deve lançar 400 quando tentar login local em conta criada via Google")
    void shouldThrow400WhenLocalLoginOnGoogleAccount() {
        LoginRequestDTO request = new LoginRequestDTO("google@teste.com", "qualquerSenha");
        User user = User.builder()
                .email("google@teste.com")
                .provider(AuthProvider.GOOGLE)
                .build();

        when(userRepository.findByEmail("google@teste.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("Esta conta foi criada com o Google");
                });
    }

    @Test
    @DisplayName("Deve autenticar com Google criando novo usuário quando não existir")
    void shouldLoginWithGoogleCreatingNewUser() {
        GoogleLoginRequestDTO request = new GoogleLoginRequestDTO("valid-google-id-token");
        GoogleUserInfo userInfo = new GoogleUserInfo("google@teste.com", "Google User", "http://pic.jpg");

        when(googleTokenVerifierService.verify("valid-google-id-token")).thenReturn(userInfo);
        when(userRepository.findByEmail("google@teste.com")).thenReturn(Optional.empty());

        UUID userId = UUID.randomUUID();
        User createdUser = User.builder()
                .id(userId)
                .email("google@teste.com")
                .fullName("Google User")
                .avatarUrl("http://pic.jpg")
                .provider(AuthProvider.GOOGLE)
                .role(Role.USER)
                .build();

        when(userRepository.saveAndFlush(any(User.class))).thenReturn(createdUser);
        when(jwtService.generateToken(createdUser)).thenReturn("jwt-google");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);
        when(refreshTokenService.createRefreshToken(createdUser)).thenReturn(RefreshToken.builder().token("ref-google").build());
        when(commentRepository.countByUserId(userId)).thenReturn(0L);

        AuthResponseDTO response = authService.loginWithGoogle(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt-google");
        assertThat(response.user().email()).isEqualTo("google@teste.com");
    }

    @Test
    @DisplayName("Deve lançar 400 no Google Login quando a conta foi criada originalmente com email/senha")
    void shouldThrow400WhenGoogleLoginOnLocalAccount() {
        GoogleLoginRequestDTO request = new GoogleLoginRequestDTO("valid-google-id-token");
        GoogleUserInfo userInfo = new GoogleUserInfo("local@teste.com", "Local User", null);

        when(googleTokenVerifierService.verify("valid-google-id-token")).thenReturn(userInfo);
        User localUser = User.builder()
                .email("local@teste.com")
                .provider(AuthProvider.LOCAL)
                .build();
        when(userRepository.findByEmail("local@teste.com")).thenReturn(Optional.of(localUser));

        assertThatThrownBy(() -> authService.loginWithGoogle(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("Esta conta foi criada com e-mail e senha");
                });
    }

    @Test
    @DisplayName("Deve atualizar refresh token com sucesso e gerar novo access token")
    void shouldRefreshTokenSuccessfully() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("raw-token");
        User user = User.builder().id(UUID.randomUUID()).email("user@teste.com").build();
        RefreshToken newRefreshToken = RefreshToken.builder().user(user).token("new-rotated-token").build();

        when(refreshTokenService.verifyAndRotate("raw-token")).thenReturn(newRefreshToken);
        when(jwtService.generateToken(user)).thenReturn("new-access-jwt");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        TokenResponseDTO response = authService.refreshToken(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new-access-jwt");
        assertThat(response.refreshToken()).isEqualTo("new-rotated-token");
    }

    @Test
    @DisplayName("Deve revogar token no logout")
    void shouldRevokeTokenOnLogout() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("token-logout");

        authService.logout(request);

        verify(refreshTokenService).revokeToken("token-logout");
    }
}
