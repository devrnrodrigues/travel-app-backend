package com.devrenanrodrigues.travelapi.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO(
        @NotBlank
        @Size(max = 60)
        String name,

        @Size(max = 60)
        String slug,

        @Size(max = 50)
        String icon,

        @Size(max = 30)
        String accentColor,

        String bgImageUrl,

        Integer sortOrder,

        Boolean active
) {
}
