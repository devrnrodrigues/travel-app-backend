package com.devrenanrodrigues.travelapi.auth.dto;

import com.devrenanrodrigues.travelapi.user.dto.UserResponseDTO;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        Long expiresIn,
        UserResponseDTO user
) {
    public static AuthResponseDTO of(String accessToken, Long expiresIn, UserResponseDTO user) {
        return new AuthResponseDTO(accessToken, "Bearer", expiresIn, user);
    }
}
