package com.devrenanrodrigues.travelapi.destination;

import com.devrenanrodrigues.travelapi.airport.AirportRepository;
import com.devrenanrodrigues.travelapi.airport.dto.AirportResponseDTO;
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
import com.devrenanrodrigues.travelapi.category.Category;
import com.devrenanrodrigues.travelapi.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final AirportRepository airportRepository;
    private final WeatherService weatherService;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final DestinationWeatherRepository weatherRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public DestinationResponseDTO create(DestinationRequestDTO dto) {
        String trimmedName = dto.name().trim();
        String trimmedCountry = dto.country().trim();

        if (destinationRepository.existsByNameIgnoreCaseAndCountryIgnoreCase(trimmedName, trimmedCountry)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Destino já cadastrado para este país: " + trimmedName);
        }

        Destination destination = Destination.builder()
                .iata(dto.iata() != null ? dto.iata().trim().toUpperCase() : null)
                .name(trimmedName)
                .city(dto.city().trim())
                .state(dto.state() != null ? dto.state().trim() : null)
                .country(trimmedCountry)
                .categories(resolveCategories(dto.categories()))
                .rating(dto.rating() != null ? dto.rating() : 0.0)
                .reviewCount(0)
                .aiStatus(dto.aiStatus() != null ? dto.aiStatus() : AiStatus.PENDING)
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .photoQuery(dto.photoQuery() != null ? dto.photoQuery().trim() : null)
                .coverImageUrl(dto.coverImageUrl())
                .aiSummary(dto.aiSummary())
                .aiCostEstimates(dto.aiCostEstimates())
                .approximatePopulation(dto.approximatePopulation())
                .popularity(dto.popularity() != null ? dto.popularity() : 0)
                .build();

        Destination saved = destinationRepository.save(destination);
        return DestinationResponseDTO.fromEntity(saved);
    }

    @Transactional
    public DestinationResponseDTO update(UUID id, DestinationRequestDTO dto) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Destino não encontrado com o id: " + id
                ));

        String trimmedName = dto.name().trim();
        String trimmedCountry = dto.country().trim();

        if (destinationRepository.existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(trimmedName, trimmedCountry, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Destino já cadastrado para este país: " + trimmedName);
        }

        if (dto.iata() != null) {
            destination.setIata(dto.iata().trim().toUpperCase());
        }
        destination.setName(trimmedName);
        destination.setCity(dto.city().trim());
        destination.setState(dto.state() != null ? dto.state().trim() : null);
        destination.setCountry(trimmedCountry);
        if (dto.categories() != null) {
            destination.setCategories(resolveCategories(dto.categories()));
        }
        if (dto.rating() != null) {
            destination.setRating(dto.rating());
        }
        if (dto.aiStatus() != null) {
            destination.setAiStatus(dto.aiStatus());
        }
        destination.setLatitude(dto.latitude());
        destination.setLongitude(dto.longitude());
        destination.setPhotoQuery(dto.photoQuery() != null ? dto.photoQuery().trim() : null);
        destination.setCoverImageUrl(dto.coverImageUrl());
        destination.setAiSummary(dto.aiSummary());
        destination.setAiCostEstimates(dto.aiCostEstimates());
        destination.setApproximatePopulation(dto.approximatePopulation());
        if (dto.popularity() != null) {
            destination.setPopularity(dto.popularity());
        }

        return DestinationResponseDTO.fromEntity(destination);
    }

    @Transactional(readOnly = true)
    public Page<DestinationSummaryResponseDTO> findAll(String category, String name, Pageable pageable) {
        String cleanCategory = (category != null && !category.isBlank()) ? category.trim() : null;
        String cleanName = (name != null && !name.isBlank()) ? name.trim() : null;
        return destinationRepository.search(cleanCategory, cleanName, pageable)
                .map(DestinationSummaryResponseDTO::fromEntity);
    }

    @Transactional
    public DestinationDetailResponseDTO findById(UUID id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Destino não encontrado com o id: " + id
                ));

        WeatherResponseDTO weather = weatherService.getOrFetchWeather(destination);

        DestinationCommentsSummaryDTO comments = commentService.findByDestinationId(id);

        AirportResponseDTO nearestAirport = null;
        if (destination.getIata() != null && !destination.getIata().isBlank()) {
            nearestAirport = airportRepository.findByIataCodeIgnoreCase(destination.getIata())
                    .map(AirportResponseDTO::fromEntity)
                    .orElse(null);
        }

        return DestinationDetailResponseDTO.of(destination, nearestAirport, weather, comments);
    }

    @Transactional
    public void delete(UUID id) {
        if (!destinationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destino não encontrado com o id: " + id);
        }

        if (commentRepository.existsByDestinationId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível excluir este destino pois ele possui avaliações vinculadas.");
        }

        if (favoriteRepository.existsByIdDestinationId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível excluir este destino pois ele foi favoritado por usuários.");
        }

        if (weatherRepository.existsById(id)) {
            weatherRepository.deleteById(id);
        }

        destinationRepository.deleteById(id);
    }

    private Set<Category> resolveCategories(List<String> categoryInputs) {
        if (categoryInputs == null || categoryInputs.isEmpty()) {
            return new HashSet<>();
        }
        Set<Category> resolved = new HashSet<>();
        for (String input : categoryInputs) {
            if (input != null && !input.isBlank()) {
                String clean = input.trim();
                categoryRepository.findByNameIgnoreCase(clean)
                        .or(() -> categoryRepository.findBySlugIgnoreCase(clean))
                        .ifPresent(resolved::add);
            }
        }
        return resolved;
    }
}
