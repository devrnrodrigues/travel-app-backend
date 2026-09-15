package com.devrenanrodrigues.travelapi.weather;

import com.devrenanrodrigues.travelapi.weather.dto.WeatherRequestDTO;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/destinations/{destinationId}/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public WeatherResponseDTO findByDestinationId(@PathVariable UUID destinationId) {
        return weatherService.findByDestinationId(destinationId);
    }

    @PutMapping
    public WeatherResponseDTO upsert(
            @PathVariable UUID destinationId,
            @RequestBody @Valid WeatherRequestDTO dto
    ) {
        return weatherService.upsert(destinationId, dto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID destinationId) {
        weatherService.delete(destinationId);
    }
}
