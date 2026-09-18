package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.auth.dto.AuthResponseDTO;
import com.devrenanrodrigues.travelapi.auth.dto.GoogleLoginRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.GoogleUserInfo;
import com.devrenanrodrigues.travelapi.auth.dto.LoginRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.RegisterRequestDTO;
import com.devrenanrodrigues.travelapi.user.AuthProvider;
import com.devrenanrodrigues.travelapi.user.Role;
import com.devrenanrodrigues.travelapi.user.User;
import com.devrenanrodrigues.travelapi.user.UserRepository;
import com.devrenanrodrigues.travelapi.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request) {
        GoogleUserInfo userInfo = googleTokenVerifierService.verify(request.idToken());

        User user = userRepository.findByEmail(userInfo.email())
                .map(existingUser -> updateExistingUser(existingUser, userInfo))
                .orElseGet(() -> createNewGoogleUser(userInfo));

        String token = jwtService.generateToken(user);
        return AuthResponseDTO.of(token, jwtService.getExpirationSeconds(), UserResponseDTO.fromEntity(user));
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está em uso.");
        }

        User newUser = User.builder()
                .email(email)
                .fullName(request.fullName().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        newUser = userRepository.saveAndFlush(newUser);
        String token = jwtService.generateToken(newUser);
        return AuthResponseDTO.of(token, jwtService.getExpirationSeconds(), UserResponseDTO.fromEntity(newUser));
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));

        if (user.getPasswordHash() == null || user.getProvider() == AuthProvider.GOOGLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta conta foi criada com o Google. Por favor, faça login utilizando o botão do Google.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }

        String token = jwtService.generateToken(user);
        return AuthResponseDTO.of(token, jwtService.getExpirationSeconds(), UserResponseDTO.fromEntity(user));
    }

    private User updateExistingUser(User existingUser, GoogleUserInfo userInfo) {
        existingUser.setFullName(userInfo.name());
        if (userInfo.pictureUrl() != null) {
            existingUser.setAvatarUrl(userInfo.pictureUrl());
        }
        return userRepository.saveAndFlush(existingUser);
    }

    private User createNewGoogleUser(GoogleUserInfo userInfo) {
        User newUser = User.builder()
                .email(userInfo.email())
                .fullName(userInfo.name())
                .avatarUrl(userInfo.pictureUrl())
                .provider(AuthProvider.GOOGLE)
                .role(Role.USER)
                .build();
        return userRepository.saveAndFlush(newUser);
    }
}
