package com.devrenanrodrigues.travelapi.airport;

import com.devrenanrodrigues.travelapi.airport.dto.AirportRequestDTO;
import com.devrenanrodrigues.travelapi.airport.dto.AirportResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository airportRepository;

    @Transactional
    public AirportResponseDTO create(AirportRequestDTO dto) {
        String normalizedIata = dto.iataCode().trim().toUpperCase();

        if (airportRepository.existsByIataCodeIgnoreCase(normalizedIata)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código IATA já cadastrado: " + normalizedIata);
        }

        Airport airport = Airport.builder()
                .iataCode(normalizedIata)
                .name(dto.name().trim())
                .city(dto.city().trim())
                .state(dto.state() != null ? dto.state().trim() : null)
                .country(dto.country().trim())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .build();

        Airport saved = airportRepository.save(airport);
        return AirportResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<AirportResponseDTO> findAll() {
        return airportRepository.findAll()
                .stream()
                .map(AirportResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AirportResponseDTO findById(UUID id) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aeroporto não encontrado com o id: " + id));
        return AirportResponseDTO.fromEntity(airport);
    }

    @Transactional(readOnly = true)
    public AirportResponseDTO findByIataCode(String iataCode) {
        Airport airport = airportRepository.findByIataCodeIgnoreCase(iataCode.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aeroporto não encontrado com o código IATA: " + iataCode));
        return AirportResponseDTO.fromEntity(airport);
    }

    @Transactional
    public AirportResponseDTO update(UUID id, AirportRequestDTO dto) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aeroporto não encontrado com o id: " + id));

        String normalizedIata = dto.iataCode().trim().toUpperCase();
        if (!airport.getIataCode().equalsIgnoreCase(normalizedIata)
                && airportRepository.existsByIataCodeIgnoreCase(normalizedIata)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Código IATA já cadastrado para outro aeroporto: " + normalizedIata);
        }

        airport.setIataCode(normalizedIata);
        airport.setName(dto.name().trim());
        airport.setCity(dto.city().trim());
        airport.setState(dto.state() != null ? dto.state().trim() : null);
        airport.setCountry(dto.country().trim());
        airport.setLatitude(dto.latitude());
        airport.setLongitude(dto.longitude());

        Airport saved = airportRepository.save(airport);
        return AirportResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!airportRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aeroporto não encontrado com o id: " + id);
        }
        airportRepository.deleteById(id);
    }
}
