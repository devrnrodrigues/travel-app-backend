package com.devrenanrodrigues.travelapi.auth.dto;

public record TokenResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
    public static TokenResponseDTO of(String accessToken, String refreshToken, Long expiresIn) {
        return new TokenResponseDTO(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
