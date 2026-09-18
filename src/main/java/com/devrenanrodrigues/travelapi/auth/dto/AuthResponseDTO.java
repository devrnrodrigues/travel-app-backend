package com.devrenanrodrigues.travelapi.auth.dto;

import com.devrenanrodrigues.travelapi.user.dto.UserResponseDTO;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponseDTO user
) {
    public static AuthResponseDTO of(String accessToken, String refreshToken, Long expiresIn, UserResponseDTO user) {
        return new AuthResponseDTO(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
