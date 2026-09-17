package com.devrenanrodrigues.travelapi.auth.dto;

public record GoogleUserInfo(
        String email,
        String name,
        String pictureUrl
) {}
