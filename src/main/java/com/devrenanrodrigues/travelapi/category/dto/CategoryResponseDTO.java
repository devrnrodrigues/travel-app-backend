package com.devrenanrodrigues.travelapi.category.dto;

import com.devrenanrodrigues.travelapi.category.Category;

import java.util.UUID;

public record CategoryResponseDTO(
        UUID id,
        String name,
        String slug,
        String icon,
        String accentColor,
        String bgImageUrl,
        Integer sortOrder,
        Boolean active
) {
    public static CategoryResponseDTO fromEntity(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getIcon(),
                category.getAccentColor(),
                category.getBgImageUrl(),
                category.getSortOrder(),
                category.getActive()
        );
    }
}
