package com.devrenanrodrigues.travelapi.favorite;

import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.destination.DestinationRepository;
import com.devrenanrodrigues.travelapi.favorite.dto.FavoriteResponseDTO;
import com.devrenanrodrigues.travelapi.favorite.dto.FavoriteStatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final DestinationRepository destinationRepository;

    @Transactional
    public FavoriteResponseDTO addFavorite(UUID destinationId, UUID userId) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Destino não encontrado com o id: " + destinationId
                ));

        FavoriteId favoriteId = new FavoriteId(userId, destinationId);

        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseGet(() -> favoriteRepository.save(
                        Favorite.builder()
                                .id(favoriteId)
                                .destination(destination)
                                .build()
                ));

        return FavoriteResponseDTO.fromEntity(favorite);
    }

    @Transactional
    public void removeFavorite(UUID destinationId, UUID userId) {
        if (!favoriteRepository.existsByIdUserIdAndIdDestinationId(userId, destinationId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Favorito não encontrado para o usuário informado."
            );
        }
        favoriteRepository.deleteByIdUserIdAndIdDestinationId(userId, destinationId);
    }

    @Transactional(readOnly = true)
    public Page<FavoriteResponseDTO> findFavorites(
            UUID authenticatedUserId,
            boolean isAdmin,
            UUID targetUserId,
            String search,
            Pageable pageable
    ) {
        if (!isAdmin && targetUserId != null && !targetUserId.equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para visualizar favoritos de outros usuários.");
        }

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        if (cleanSearch == null) {
            if (isAdmin) {
                if (targetUserId != null) {
                    return favoriteRepository.findByUserId(targetUserId, pageable)
                            .map(FavoriteResponseDTO::fromEntity);
                }
                return favoriteRepository.findAllWithDestination(pageable)
                        .map(FavoriteResponseDTO::fromEntity);
            }
            return favoriteRepository.findByUserId(authenticatedUserId, pageable)
                    .map(FavoriteResponseDTO::fromEntity);
        }

        String pattern = "%" + cleanSearch.toLowerCase() + "%";

        if (isAdmin) {
            if (targetUserId != null) {
                return favoriteRepository.findByUserIdAndSearch(targetUserId, pattern, pageable)
                        .map(FavoriteResponseDTO::fromEntity);
            }
            return favoriteRepository.findAllWithSearch(pattern, pageable)
                    .map(FavoriteResponseDTO::fromEntity);
        }

        return favoriteRepository.findByUserIdAndSearch(authenticatedUserId, pattern, pageable)
                .map(FavoriteResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponseDTO> findByUserId(UUID userId) {
        return favoriteRepository.findByUserId(userId)
                .stream()
                .map(FavoriteResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public FavoriteStatusDTO checkFavorite(UUID destinationId, UUID userId) {
        boolean exists = favoriteRepository.existsByIdUserIdAndIdDestinationId(userId, destinationId);
        return new FavoriteStatusDTO(destinationId, userId, exists);
    }
}
