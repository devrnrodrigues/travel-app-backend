package com.devrenanrodrigues.travelapi.destination;

import com.devrenanrodrigues.travelapi.airport.Airport;
import com.devrenanrodrigues.travelapi.airport.AirportRepository;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationRequestDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationResponseDTO;
import lombok.RequiredArgsConstructor;
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
                .country(dto.country().trim())
                .category(dto.category().trim())
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

    @Transactional(readOnly = true)
    public List<DestinationResponseDTO> findAll(String category, String city, String country) {
        return destinationRepository.search(category, city, country)
                .stream()
                .map(DestinationResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DestinationResponseDTO findById(UUID id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Destino não encontrado com o id: " + id
                ));
        return DestinationResponseDTO.fromEntity(destination);
    }

    @Transactional
    public void delete(UUID id) {
        if (!destinationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destino não encontrado com o id: " + id);
        }
        destinationRepository.deleteById(id);
    }
}
