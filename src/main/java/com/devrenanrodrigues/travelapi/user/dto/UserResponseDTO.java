package com.devrenanrodrigues.travelapi.user.dto;

import com.devrenanrodrigues.travelapi.user.AuthProvider;
import com.devrenanrodrigues.travelapi.user.Role;
import com.devrenanrodrigues.travelapi.user.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email,
        String fullName,
        String avatarUrl,
        String bio,
        String nationality,
        AuthProvider provider,
        Role role,
        Instant createdAt,
        Long commentsCount
) {
    public static UserResponseDTO fromEntity(User user) {
        return fromEntity(user, 0L);
    }

    public static UserResponseDTO fromEntity(User user, Long commentsCount) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getNationality(),
                user.getProvider(),
                user.getRole(),
                user.getCreatedAt(),
                commentsCount != null ? commentsCount : 0L
        );
    }
}
