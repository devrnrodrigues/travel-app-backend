package com.devrenanrodrigues.travelapi.airport;

import com.devrenanrodrigues.travelapi.airport.dto.AirportRequestDTO;
import com.devrenanrodrigues.travelapi.airport.dto.AirportResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AirportResponseDTO create(@RequestBody @Valid AirportRequestDTO dto) {
        return airportService.create(dto);
    }

    @GetMapping
    public List<AirportResponseDTO> findAll() {
        return airportService.findAll();
    }

    @GetMapping("/{id}")
    public AirportResponseDTO findById(@PathVariable UUID id) {
        return airportService.findById(id);
    }

    @GetMapping("/iata/{iataCode}")
    public AirportResponseDTO findByIataCode(@PathVariable String iataCode) {
        return airportService.findByIataCode(iataCode);
    }

    @PutMapping("/{id}")
    public AirportResponseDTO update(@PathVariable UUID id, @RequestBody @Valid AirportRequestDTO dto) {
        return airportService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        airportService.delete(id);
    }
}
