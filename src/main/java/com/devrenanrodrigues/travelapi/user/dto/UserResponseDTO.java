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
        AuthProvider provider,
        Role role,
        Instant createdAt
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getProvider(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
