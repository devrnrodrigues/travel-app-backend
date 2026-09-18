package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.auth.dto.AuthResponseDTO;
import com.devrenanrodrigues.travelapi.auth.dto.GoogleLoginRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.LoginRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.RefreshTokenRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.RegisterRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.TokenResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> loginWithGoogle(@Valid @RequestBody GoogleLoginRequestDTO request) {
        AuthResponseDTO response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        TokenResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
