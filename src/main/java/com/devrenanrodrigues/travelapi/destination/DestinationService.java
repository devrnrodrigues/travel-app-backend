package com.devrenanrodrigues.travelapi.destination;

import com.devrenanrodrigues.travelapi.airport.Airport;
import com.devrenanrodrigues.travelapi.airport.AirportRepository;
import com.devrenanrodrigues.travelapi.comment.CommentService;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationDetailResponseDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationRequestDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationResponseDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationSummaryResponseDTO;
import com.devrenanrodrigues.travelapi.weather.WeatherService;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherResponseDTO;
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
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final AirportRepository airportRepository;
    private final WeatherService weatherService;
    private final CommentService commentService;

    @Transactional
    public DestinationResponseDTO create(DestinationRequestDTO dto) {
        Airport nearestAirport = null;
        if (dto.nearestAirportId() != null) {
            nearestAirport = airportRepository.findById(dto.nearestAirportId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Aeroporto mais próximo não encontrado com o id: " + dto.nearestAirportId()
                    ));
        }

        Destination destination = Destination.builder()
                .nearestAirport(nearestAirport)
                .name(dto.name().trim())
                .city(dto.city().trim())
                .state(dto.state() != null ? dto.state().trim() : null)
                .country(dto.country().trim())
                .categories(dto.categories() != null ? dto.categories().stream().map(String::trim).toList() : List.of())
                .rating(dto.rating() != null ? dto.rating() : 0.0)
                .reviewCount(0)
                .aiStatus(dto.aiStatus() != null ? dto.aiStatus() : AiStatus.PENDING)
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .photoQuery(dto.photoQuery().trim())
                .coverImageUrl(dto.coverImageUrl())
                .galleryUrls(dto.galleryUrls())
                .aiSummary(dto.aiSummary())
                .aiCostEstimates(dto.aiCostEstimates())
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

        Airport nearestAirport = destination.getNearestAirport();
        if (dto.nearestAirportId() != null) {
            nearestAirport = airportRepository.findById(dto.nearestAirportId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Aeroporto mais próximo não encontrado com o id: " + dto.nearestAirportId()
                    ));
        }

        destination.setNearestAirport(nearestAirport);
        destination.setName(dto.name().trim());
        destination.setCity(dto.city().trim());
        destination.setState(dto.state() != null ? dto.state().trim() : null);
        destination.setCountry(dto.country().trim());
        destination.setCategories(dto.categories() != null ? dto.categories().stream().map(String::trim).toList() : List.of());
        if (dto.rating() != null) {
            destination.setRating(dto.rating());
        }
        if (dto.aiStatus() != null) {
            destination.setAiStatus(dto.aiStatus());
        }
        destination.setLatitude(dto.latitude());
        destination.setLongitude(dto.longitude());
        destination.setPhotoQuery(dto.photoQuery().trim());
        destination.setCoverImageUrl(dto.coverImageUrl());
        destination.setGalleryUrls(dto.galleryUrls());
        destination.setAiSummary(dto.aiSummary());
        destination.setAiCostEstimates(dto.aiCostEstimates());

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

        return DestinationDetailResponseDTO.of(destination, weather, comments);
    }

    @Transactional
    public void delete(UUID id) {
        if (!destinationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destino não encontrado com o id: " + id);
        }
        destinationRepository.deleteById(id);
    }
}
