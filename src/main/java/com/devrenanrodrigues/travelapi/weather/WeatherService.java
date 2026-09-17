package com.devrenanrodrigues.travelapi.weather;

import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.destination.DestinationRepository;
import com.devrenanrodrigues.travelapi.weather.client.OpenMeteoClient;
import com.devrenanrodrigues.travelapi.weather.client.OpenMeteoResponseDTO;
import com.devrenanrodrigues.travelapi.weather.client.WeatherProperties;
import com.devrenanrodrigues.travelapi.weather.client.WmoWeatherCode;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherRequestDTO;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final DestinationWeatherRepository weatherRepository;
    private final DestinationRepository destinationRepository;
    private final OpenMeteoClient openMeteoClient;
    private final WeatherProperties weatherProperties;

    @Transactional
    public WeatherResponseDTO findByDestinationId(UUID destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destino não encontrado com o id: " + destinationId));

        WeatherResponseDTO weather = getOrFetchWeather(destination);
        if (weather == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Dados meteorológicos não encontrados para o destino com o id: " + destinationId
            );
        }

        return weather;
    }

    @Transactional
    public WeatherResponseDTO getOrFetchWeather(Destination destination) {
        DestinationWeather cached = weatherRepository.findById(destination.getId()).orElse(null);
        if (cached != null && isCacheValid(cached)) {
            return WeatherResponseDTO.fromEntity(cached);
        }

        Optional<OpenMeteoResponseDTO> responseOpt = openMeteoClient.fetchCurrentWeather(
                destination.getLatitude(),
                destination.getLongitude()
        );

        if (responseOpt.isEmpty() || responseOpt.get().current() == null) {
            return cached != null ? WeatherResponseDTO.fromEntity(cached) : null;
        }

        OpenMeteoResponseDTO.CurrentWeather current = responseOpt.get().current();
        DestinationWeather weather = cached != null ? cached : DestinationWeather.builder()
                .destination(destination)
                .build();

        weather.setTemperature(current.temperature() != null ? current.temperature() : 0.0);
        weather.setWindSpeed(current.windSpeed() != null ? current.windSpeed() : 0.0);

        int rainProb = current.precipitationProbability() != null ? current.precipitationProbability() : 0;
        weather.setRainProbability(Math.max(0, Math.min(100, rainProb)));

        String desc = WmoWeatherCode.getDescription(current.weatherCode());
        String condition = current.relativeHumidity() != null
                ? String.format("%s, %d%% de umidade", desc, current.relativeHumidity())
                : desc;

        if (condition.length() > 50) {
            condition = condition.substring(0, 50);
        }
        weather.setConditionText(condition);
        weather.setUpdatedAt(Instant.now());

        DestinationWeather saved = weatherRepository.save(weather);
        return WeatherResponseDTO.fromEntity(saved);
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
        weather.setUpdatedAt(Instant.now());

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

    private boolean isCacheValid(DestinationWeather weather) {
        if (weather == null || weather.getUpdatedAt() == null) {
            return false;
        }
        Duration age = Duration.between(weather.getUpdatedAt(), Instant.now());
        return !age.isNegative() && age.toHours() < weatherProperties.getCacheTtlHours();
    }
}
