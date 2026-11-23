package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.user.AuthProvider;
import com.devrenanrodrigues.travelapi.user.Role;
import com.devrenanrodrigues.travelapi.user.User;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "1234567890123456789012345678901234567890123456789012345678901234";
    private static final long EXPIRATION_SECONDS = 3600;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_SECONDS);
    }

    @Test
    @DisplayName("Deve gerar token JWT com subject, email, fullName e role corretos")
    void shouldGenerateValidJwtToken() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("renan@teste.com")
                .fullName("Renan Rodrigues")
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .build();

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();

        SignedJWT signedJWT = SignedJWT.parse(token);
        assertThat(signedJWT.getJWTClaimsSet().getSubject()).isEqualTo(userId.toString());
        assertThat(signedJWT.getJWTClaimsSet().getStringClaim("email")).isEqualTo("renan@teste.com");
        assertThat(signedJWT.getJWTClaimsSet().getStringClaim("fullName")).isEqualTo("Renan Rodrigues");
        assertThat(signedJWT.getJWTClaimsSet().getStringClaim("role")).isEqualTo("USER");
        assertThat(signedJWT.getJWTClaimsSet().getExpirationTime()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar tempo de expiração configurado")
    void shouldReturnConfiguredExpirationSeconds() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(EXPIRATION_SECONDS);
    }
}
