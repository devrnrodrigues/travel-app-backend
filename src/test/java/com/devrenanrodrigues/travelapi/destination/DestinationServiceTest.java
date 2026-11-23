package com.devrenanrodrigues.travelapi.destination;

import com.devrenanrodrigues.travelapi.airport.AirportRepository;
import com.devrenanrodrigues.travelapi.category.Category;
import com.devrenanrodrigues.travelapi.category.CategoryRepository;
import com.devrenanrodrigues.travelapi.comment.CommentRepository;
import com.devrenanrodrigues.travelapi.comment.CommentService;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationDetailResponseDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationRequestDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationResponseDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationSummaryResponseDTO;
import com.devrenanrodrigues.travelapi.favorite.FavoriteRepository;
import com.devrenanrodrigues.travelapi.weather.DestinationWeatherRepository;
import com.devrenanrodrigues.travelapi.weather.WeatherService;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherResponseDTO;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DestinationServiceTest {

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private WeatherService weatherService;

    @Mock
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private DestinationWeatherRepository weatherRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DestinationService destinationService;

    private DestinationRequestDTO createSampleRequest(String name, String country) {
        return new DestinationRequestDTO(
                "CDG", name, "Paris", "Île-de-France", country,
                "Cultural", List.of("Histórico"),
                4.8, AiStatus.COMPLETED, 48.8566, 2.3522, "paris landscape",
                "http://cover.jpg", List.of("Cidade Luz"), null, 2161000L, 95
        );
    }

    private Destination createSampleDestination(UUID id, String name, String country) {
        return Destination.builder()
                .id(id)
                .iata("CDG")
                .name(name)
                .city("Paris")
                .country(country)
                .latitude(48.8566)
                .longitude(2.3522)
                .rating(4.8)
                .reviewCount(10)
                .aiStatus(AiStatus.COMPLETED)
                .build();
    }

    @Test
    @DisplayName("Deve criar um destino com sucesso")
    void shouldCreateDestinationSuccessfully() {
        DestinationRequestDTO dto = createSampleRequest("Torre Eiffel", "França");

        when(destinationRepository.existsByNameIgnoreCaseAndCountryIgnoreCase("Torre Eiffel", "França")).thenReturn(false);
        Category primaryCat = Category.builder().id(UUID.randomUUID()).name("Cultural").isPrimary(true).build();
        when(categoryRepository.findByNameIgnoreCase("Cultural")).thenReturn(Optional.of(primaryCat));
        when(categoryRepository.findByNameIgnoreCase("Histórico")).thenReturn(Optional.empty());

        Destination saved = createSampleDestination(UUID.randomUUID(), "Torre Eiffel", "França");
        when(destinationRepository.save(any(Destination.class))).thenReturn(saved);

        DestinationResponseDTO response = destinationService.create(dto);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Torre Eiffel");
        assertThat(response.country()).isEqualTo("França");
    }

    @Test
    @DisplayName("Deve lançar 409 quando destino já existir para o país")
    void shouldThrow409WhenDestinationAlreadyExistsInCountry() {
        DestinationRequestDTO dto = createSampleRequest("Coliseu", "Itália");
        when(destinationRepository.existsByNameIgnoreCaseAndCountryIgnoreCase("Coliseu", "Itália")).thenReturn(true);

        assertThatThrownBy(() -> destinationService.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    @DisplayName("Deve atualizar destino existente")
    void shouldUpdateDestinationSuccessfully() {
        UUID id = UUID.randomUUID();
        Destination existing = createSampleDestination(id, "Nome Antigo", "França");
        DestinationRequestDTO dto = createSampleRequest("Nome Novo", "França");

        when(destinationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(destinationRepository.existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot("Nome Novo", "França", id)).thenReturn(false);
        Category primaryCat = Category.builder().id(UUID.randomUUID()).name("Cultural").isPrimary(true).build();
        when(categoryRepository.findByNameIgnoreCase("Cultural")).thenReturn(Optional.of(primaryCat));

        DestinationResponseDTO updated = destinationService.update(id, dto);

        assertThat(updated).isNotNull();
        assertThat(existing.getName()).isEqualTo("Nome Novo");
    }

    @Test
    @DisplayName("Deve buscar detalhes do destino incluindo clima e resumo de comentários")
    void shouldFindDestinationDetailById() {
        UUID id = UUID.randomUUID();
        Destination dest = createSampleDestination(id, "Roma", "Itália");

        when(destinationRepository.findByIdWithImages(id)).thenReturn(Optional.of(dest));
        when(weatherService.getOrFetchWeather(dest)).thenReturn(null);
        when(commentService.findByDestinationId(id)).thenReturn(new DestinationCommentsSummaryDTO(id, 4.5, 10L, List.of()));

        DestinationDetailResponseDTO detail = destinationService.findById(id);

        assertThat(detail).isNotNull();
        assertThat(detail.name()).isEqualTo("Roma");
        assertThat(detail.comments()).isNotNull();
        assertThat(detail.comments().totalComments()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve lançar 404 quando destino não for encontrado por ID")
    void shouldThrow404WhenDestinationNotFound() {
        UUID id = UUID.randomUUID();
        when(destinationRepository.findByIdWithImages(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> destinationService.findById(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("Deve listar destinos paginados através de busca")
    void shouldFindAllDestinationsPaged() {
        Destination dest = createSampleDestination(UUID.randomUUID(), "Madrid", "Espanha");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Destination> page = new PageImpl<>(List.of(dest), pageable, 1);

        when(destinationRepository.search(eq("Cultural"), eq("Madrid"), eq("popularity"), eq(pageable))).thenReturn(page);

        Page<DestinationSummaryResponseDTO> result = destinationService.findAll("Cultural", "Madrid", "popularity", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Madrid");
    }

    @Test
    @DisplayName("Deve excluir destino quando não houver avaliações ou favoritos vinculados")
    void shouldDeleteDestinationSuccessfully() {
        UUID id = UUID.randomUUID();

        when(destinationRepository.existsById(id)).thenReturn(true);
        when(commentRepository.existsByDestinationId(id)).thenReturn(false);
        when(favoriteRepository.existsByIdDestinationId(id)).thenReturn(false);
        when(weatherRepository.existsById(id)).thenReturn(true);

        destinationService.delete(id);

        verify(weatherRepository).deleteById(id);
        verify(destinationRepository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar 409 ao tentar excluir destino com avaliações vinculadas")
    void shouldThrow409WhenDeletingDestinationWithComments() {
        UUID id = UUID.randomUUID();

        when(destinationRepository.existsById(id)).thenReturn(true);
        when(commentRepository.existsByDestinationId(id)).thenReturn(true);

        assertThatThrownBy(() -> destinationService.delete(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).contains("possui avaliações");
                });

        verify(destinationRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar 409 ao tentar excluir destino favoritado por usuários")
    void shouldThrow409WhenDeletingFavoritedDestination() {
        UUID id = UUID.randomUUID();

        when(destinationRepository.existsById(id)).thenReturn(true);
        when(commentRepository.existsByDestinationId(id)).thenReturn(false);
        when(favoriteRepository.existsByIdDestinationId(id)).thenReturn(true);

        assertThatThrownBy(() -> destinationService.delete(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).contains("favoritado por usuários");
                });

        verify(destinationRepository, never()).deleteById(any());
    }
}
