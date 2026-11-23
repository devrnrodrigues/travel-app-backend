package com.devrenanrodrigues.travelapi.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    private Jwt createJwt(String sub, String role) {
        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", sub, "role", role != null ? role : "USER")
        );
    }

    @Test
    @DisplayName("Deve extrair UUID do usuário quando subject for válido")
    void shouldExtractUserIdFromValidJwt() {
        UUID expectedId = UUID.randomUUID();
        Jwt jwt = createJwt(expectedId.toString(), "USER");

        UUID actualId = SecurityUtils.getUserId(jwt);

        assertThat(actualId).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("Deve lançar 401 quando jwt for nulo")
    void shouldThrow401WhenJwtIsNull() {
        assertThatThrownBy(() -> SecurityUtils.getUserId(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    @DisplayName("Deve lançar 401 quando subject não for um UUID válido")
    void shouldThrow401WhenSubjectIsNotUuid() {
        Jwt jwt = createJwt("invalid-uuid-string", "USER");

        assertThatThrownBy(() -> SecurityUtils.getUserId(jwt))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }

    @Test
    @DisplayName("Deve retornar true para isAdmin quando role for ADMIN")
    void shouldReturnTrueWhenRoleIsAdmin() {
        Jwt jwt = createJwt(UUID.randomUUID().toString(), "ADMIN");
        assertThat(SecurityUtils.isAdmin(jwt)).isTrue();

        Jwt jwtLower = createJwt(UUID.randomUUID().toString(), "admin");
        assertThat(SecurityUtils.isAdmin(jwtLower)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false para isAdmin quando role for USER ou nulo")
    void shouldReturnFalseWhenRoleIsNotAdmin() {
        Jwt jwt = createJwt(UUID.randomUUID().toString(), "USER");
        assertThat(SecurityUtils.isAdmin(jwt)).isFalse();

        assertThat(SecurityUtils.isAdmin(null)).isFalse();
    }
}
