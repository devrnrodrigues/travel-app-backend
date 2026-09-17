package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.auth.dto.AuthResponseDTO;
import com.devrenanrodrigues.travelapi.auth.dto.GoogleLoginRequestDTO;
import com.devrenanrodrigues.travelapi.auth.dto.GoogleUserInfo;
import com.devrenanrodrigues.travelapi.user.AuthProvider;
import com.devrenanrodrigues.travelapi.user.Role;
import com.devrenanrodrigues.travelapi.user.User;
import com.devrenanrodrigues.travelapi.user.UserRepository;
import com.devrenanrodrigues.travelapi.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request) {
        GoogleUserInfo userInfo = googleTokenVerifierService.verify(request.idToken());

        User user = userRepository.findByEmail(userInfo.email())
                .map(existingUser -> updateExistingUser(existingUser, userInfo))
                .orElseGet(() -> createNewGoogleUser(userInfo));

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
