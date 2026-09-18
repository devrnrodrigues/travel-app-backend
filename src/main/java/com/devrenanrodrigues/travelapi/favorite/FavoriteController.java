package com.devrenanrodrigues.travelapi.favorite;

import com.devrenanrodrigues.travelapi.favorite.dto.FavoriteResponseDTO;
import com.devrenanrodrigues.travelapi.favorite.dto.FavoriteStatusDTO;
import com.devrenanrodrigues.travelapi.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{destinationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteResponseDTO addFavorite(
            @PathVariable UUID destinationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        return favoriteService.addFavorite(destinationId, userId);
    }

    @DeleteMapping("/{destinationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
            @PathVariable UUID destinationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        favoriteService.removeFavorite(destinationId, userId);
    }

    @GetMapping
    public List<FavoriteResponseDTO> findFavorites(
            @RequestParam(required = false) UUID userId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authenticatedUserId = SecurityUtils.getUserId(jwt);
        boolean isAdmin = SecurityUtils.isAdmin(jwt);
        return favoriteService.findFavorites(authenticatedUserId, isAdmin, userId);
    }

    @GetMapping("/{destinationId}/check")
    public FavoriteStatusDTO checkFavorite(
            @PathVariable UUID destinationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        return favoriteService.checkFavorite(destinationId, userId);
    }
}
