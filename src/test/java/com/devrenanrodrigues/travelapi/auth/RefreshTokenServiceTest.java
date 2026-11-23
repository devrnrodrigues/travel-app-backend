package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, transactionManager);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationDays", 30L);
    }

    @Test
    @DisplayName("Deve criar um novo refresh token associado ao usuário")
    void shouldCreateRefreshTokenSuccessfully() {
        User user = User.builder().id(UUID.randomUUID()).email("usuario@teste.com").build();

        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken created = refreshTokenService.createRefreshToken(user);

        assertThat(created).isNotNull();
        assertThat(created.getUser()).isEqualTo(user);
        assertThat(created.getToken()).isNotBlank();
        assertThat(created.getRawToken()).isNotBlank();
        assertThat(created.isRevoked()).isFalse();
        assertThat(created.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Deve rotacionar token com sucesso quando token for válido")
    void shouldVerifyAndRotateValidToken() {
        User user = User.builder().id(UUID.randomUUID()).email("usuario@teste.com").build();
        RefreshToken existingToken = RefreshToken.builder()
                .user(user)
                .token("hashed-token")
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken rotated = refreshTokenService.verifyAndRotate("raw-token-string");

        assertThat(rotated).isNotNull();
        assertThat(existingToken.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar 401 quando refresh token não for encontrado")
    void shouldThrow401WhenTokenNotFound() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.verifyAndRotate("inexistente"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).isEqualTo("Refresh token inválido.");
                });
    }

    @Test
    @DisplayName("Deve revogar todos os tokens do usuário e lançar 401 em tentativa de reúso de token revogado")
    void shouldRevokeAllUserTokensWhenTokenAlreadyRevoked() {
        User user = User.builder().id(UUID.randomUUID()).email("usuario@teste.com").build();
        RefreshToken revokedToken = RefreshToken.builder()
                .user(user)
                .token("hashed-token")
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> refreshTokenService.verifyAndRotate("token-revogado"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Refresh token revogado");
                });
    }

    @Test
    @DisplayName("Deve lançar 401 quando o refresh token estiver expirado")
    void shouldThrow401WhenTokenExpired() {
        User user = User.builder().id(UUID.randomUUID()).email("usuario@teste.com").build();
        RefreshToken expiredToken = RefreshToken.builder()
                .user(user)
                .token("hashed-token")
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.verifyAndRotate("token-expirado"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).contains("Refresh token expirado");
                });
    }

    @Test
    @DisplayName("Deve revogar token existente")
    void shouldRevokeExistingToken() {
        RefreshToken token = RefreshToken.builder()
                .token("hashed-token")
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(token));

        refreshTokenService.revokeToken("token-a-revogar");

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).saveAndFlush(token);
    }
}
