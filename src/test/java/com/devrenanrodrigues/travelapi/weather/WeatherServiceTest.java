package com.devrenanrodrigues.travelapi.weather;

import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.destination.DestinationRepository;
import com.devrenanrodrigues.travelapi.weather.client.OpenMeteoClient;
import com.devrenanrodrigues.travelapi.weather.client.OpenMeteoResponseDTO;
import com.devrenanrodrigues.travelapi.weather.client.WeatherProperties;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherRequestDTO;
import com.devrenanrodrigues.travelapi.weather.dto.WeatherResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private DestinationWeatherRepository weatherRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private OpenMeteoClient openMeteoClient;

    @Mock
    private WeatherProperties weatherProperties;

    @InjectMocks
    private WeatherService weatherService;

    private Destination destination;
    private final UUID destinationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        destination = Destination.builder()
                .id(destinationId)
                .name("Rio de Janeiro")
                .latitude(-22.9068)
                .longitude(-43.1729)
                .build();
    }

    @Test
    @DisplayName("Deve retornar dados do cache se estiver válido (< TTL)")
    void shouldReturnCachedWeatherWhenValid() {
        DestinationWeather cached = DestinationWeather.builder()
                .destination(destination)
                .temperature(28.5)
                .windSpeed(12.0)
                .rainProbability(10)
                .conditionText("Ensolarado")
                .updatedAt(Instant.now().minus(10, ChronoUnit.MINUTES))
                .build();

        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
        when(weatherRepository.findById(destinationId)).thenReturn(Optional.of(cached));
        when(weatherProperties.getCacheTtlHours()).thenReturn(1);

        WeatherResponseDTO response = weatherService.findByDestinationId(destinationId);

        assertThat(response).isNotNull();
        assertThat(response.temperature()).isEqualTo(28.5);
        assertThat(response.conditionText()).isEqualTo("Ensolarado");
    }

    @Test
    @DisplayName("Deve consultar Open-Meteo e salvar no banco quando cache estiver ausente")
    void shouldFetchFromOpenMeteoWhenNoCache() {
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
        when(weatherRepository.findById(destinationId)).thenReturn(Optional.empty());

        OpenMeteoResponseDTO.CurrentWeather current = new OpenMeteoResponseDTO.CurrentWeather(
                25.0, 15.0, 0, 65, 0
        );
        OpenMeteoResponseDTO openMeteoResponse = new OpenMeteoResponseDTO(current);

        when(openMeteoClient.fetchCurrentWeather(anyDouble(), anyDouble())).thenReturn(Optional.of(openMeteoResponse));

        when(weatherRepository.save(any(DestinationWeather.class))).thenAnswer(invocation -> {
            DestinationWeather w = invocation.getArgument(0);
            return DestinationWeather.builder()
                    .destination(destination)
                    .temperature(w.getTemperature())
                    .windSpeed(w.getWindSpeed())
                    .rainProbability(w.getRainProbability())
                    .conditionText(w.getConditionText())
                    .updatedAt(w.getUpdatedAt())
                    .build();
        });

        WeatherResponseDTO response = weatherService.findByDestinationId(destinationId);

        assertThat(response).isNotNull();
        assertThat(response.temperature()).isEqualTo(25.0);
        assertThat(response.windSpeed()).isEqualTo(15.0);
        verify(weatherRepository).save(any(DestinationWeather.class));
    }

    @Test
    @DisplayName("Deve lançar 404 quando destino não for encontrado")
    void shouldThrow404WhenDestinationNotFound() {
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> weatherService.findByDestinationId(destinationId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("Deve atualizar ou inserir dados meteorológicos via upsert")
    void shouldUpsertWeather() {
        WeatherRequestDTO dto = new WeatherRequestDTO(30.0, 10.0, 0, "Céu limpo");

        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
        when(weatherRepository.findById(destinationId)).thenReturn(Optional.empty());

        when(weatherRepository.save(any(DestinationWeather.class))).thenAnswer(invocation -> {
            DestinationWeather w = invocation.getArgument(0);
            return DestinationWeather.builder()
                    .destination(destination)
                    .temperature(w.getTemperature())
                    .windSpeed(w.getWindSpeed())
                    .rainProbability(w.getRainProbability())
                    .conditionText(w.getConditionText())
                    .updatedAt(w.getUpdatedAt())
                    .build();
        });

        WeatherResponseDTO response = weatherService.upsert(destinationId, dto);

        assertThat(response).isNotNull();
        assertThat(response.temperature()).isEqualTo(30.0);
        assertThat(response.conditionText()).isEqualTo("Céu limpo");
    }

    @Test
    @DisplayName("Deve excluir dados de clima com sucesso")
    void shouldDeleteWeather() {
        when(weatherRepository.existsById(destinationId)).thenReturn(true);

        weatherService.delete(destinationId);

        verify(weatherRepository).deleteById(destinationId);
    }
}
