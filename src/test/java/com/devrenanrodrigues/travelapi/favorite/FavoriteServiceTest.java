package com.devrenanrodrigues.travelapi.favorite;

import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.destination.DestinationRepository;
import com.devrenanrodrigues.travelapi.favorite.dto.FavoriteResponseDTO;
import com.devrenanrodrigues.travelapi.favorite.dto.FavoriteStatusDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    @DisplayName("Deve favoritar um destino com sucesso")
    void shouldAddFavoriteSuccessfully() {
        UUID destinationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Destination dest = Destination.builder().id(destinationId).name("Tóquio").city("Tóquio").country("Japão").build();

        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(dest));
        when(favoriteRepository.findById(any(FavoriteId.class))).thenReturn(Optional.empty());

        Favorite savedFavorite = Favorite.builder()
                .id(new FavoriteId(userId, destinationId))
                .destination(dest)
                .build();
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(savedFavorite);

        FavoriteResponseDTO result = favoriteService.addFavorite(destinationId, userId);

        assertThat(result).isNotNull();
        assertThat(result.destinationName()).isEqualTo("Tóquio");
        assertThat(result.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Deve lançar 404 ao favoritar destino inexistente")
    void shouldThrow404WhenDestinationNotFound() {
        UUID destinationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(destinationRepository.findById(destinationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.addFavorite(destinationId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("Deve remover favorito existente com sucesso")
    void shouldRemoveFavoriteSuccessfully() {
        UUID destinationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(favoriteRepository.existsByIdUserIdAndIdDestinationId(userId, destinationId)).thenReturn(true);

        favoriteService.removeFavorite(destinationId, userId);

        verify(favoriteRepository).deleteByIdUserIdAndIdDestinationId(userId, destinationId);
    }

    @Test
    @DisplayName("Deve lançar 404 ao tentar remover favorito inexistente")
    void shouldThrow404WhenRemovingNonExistentFavorite() {
        UUID destinationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(favoriteRepository.existsByIdUserIdAndIdDestinationId(userId, destinationId)).thenReturn(false);

        assertThatThrownBy(() -> favoriteService.removeFavorite(destinationId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("Deve verificar se destino é favorito do usuário")
    void shouldCheckFavoriteStatus() {
        UUID destinationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(favoriteRepository.existsByIdUserIdAndIdDestinationId(userId, destinationId)).thenReturn(true);

        FavoriteStatusDTO status = favoriteService.checkFavorite(destinationId, userId);

        assertThat(status).isNotNull();
        assertThat(status.isFavorite()).isTrue();
    }
}
