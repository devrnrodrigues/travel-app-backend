package com.devrenanrodrigues.travelapi.weather;

import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.destination.DestinationRepository;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherRequestDTO;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final DestinationWeatherRepository weatherRepository;
    private final DestinationRepository destinationRepository;

    @Transactional(readOnly = true)
    public WeatherResponseDTO findByDestinationId(UUID destinationId) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destino não encontrado com o id: " + destinationId);
        }

        DestinationWeather weather = weatherRepository.findById(destinationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Dados meteorológicos não encontrados para o destino com o id: " + destinationId
                ));

        return WeatherResponseDTO.fromEntity(weather);
    }

    @Transactional
    public WeatherResponseDTO upsert(UUID destinationId, WeatherRequestDTO dto) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destino não encontrado com o id: " + destinationId));

        DestinationWeather weather = weatherRepository.findById(destinationId)
                .orElseGet(() -> DestinationWeather.builder()
                        .destination(destination)
                        .build());

        weather.setTemperature(dto.temperature());
        weather.setWindSpeed(dto.windSpeed());
        weather.setRainProbability(dto.rainProbability());
        weather.setConditionText(dto.conditionText().trim());

        DestinationWeather saved = weatherRepository.save(weather);
        return WeatherResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID destinationId) {
        if (!weatherRepository.existsById(destinationId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Dados meteorológicos não encontrados para o destino com o id: " + destinationId
            );
        }
        weatherRepository.deleteById(destinationId);
    }
}
