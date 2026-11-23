package com.devrenanrodrigues.travelapi.airport;

import com.devrenanrodrigues.travelapi.airport.dto.AirportRequestDTO;
import com.devrenanrodrigues.travelapi.airport.dto.AirportResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirportService airportService;

    @Test
    @DisplayName("Deve cadastrar aeroporto com código IATA em maiúsculo")
    void shouldCreateAirportSuccessfully() {
        AirportRequestDTO dto = new AirportRequestDTO(
                "gru", "SBGR", "Guarulhos", "São Paulo", "SP", "Brasil", "SA", "large_airport", null, null, null, -23.4356, -46.4731
        );

        when(airportRepository.existsByIataCodeIgnoreCase("GRU")).thenReturn(false);
        when(airportRepository.save(any(Airport.class))).thenAnswer(invocation -> {
            Airport a = invocation.getArgument(0);
            return Airport.builder()
                    .id(UUID.randomUUID())
                    .iataCode(a.getIataCode())
                    .icaoCode(a.getIcaoCode())
                    .name(a.getName())
                    .city(a.getCity())
                    .country(a.getCountry())
                    .latitude(a.getLatitude())
                    .longitude(a.getLongitude())
                    .build();
        });

        AirportResponseDTO result = airportService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.iataCode()).isEqualTo("GRU");
        assertThat(result.city()).isEqualTo("São Paulo");
    }

    @Test
    @DisplayName("Deve lançar 409 quando código IATA já estiver cadastrado")
    void shouldThrow409WhenIataAlreadyExists() {
        AirportRequestDTO dto = new AirportRequestDTO(
                "GRU", null, "Guarulhos", "São Paulo", null, "Brasil", null, null, null, null, null, null, null
        );

        when(airportRepository.existsByIataCodeIgnoreCase("GRU")).thenReturn(true);

        assertThatThrownBy(() -> airportService.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    @DisplayName("Deve buscar aeroporto por ID com sucesso")
    void shouldFindAirportById() {
        UUID id = UUID.randomUUID();
        Airport airport = Airport.builder().id(id).iataCode("GIG").name("Galeão").city("Rio de Janeiro").country("Brasil").build();

        when(airportRepository.findById(id)).thenReturn(Optional.of(airport));

        AirportResponseDTO result = airportService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.iataCode()).isEqualTo("GIG");
    }

    @Test
    @DisplayName("Deve listar todos os aeroportos")
    void shouldFindAllAirports() {
        Airport a1 = Airport.builder().id(UUID.randomUUID()).iataCode("GRU").name("Guarulhos").city("SP").country("BR").build();
        when(airportRepository.findAll()).thenReturn(List.of(a1));

        List<AirportResponseDTO> list = airportService.findAll();

        assertThat(list).hasSize(1);
        assertThat(list.getFirst().iataCode()).isEqualTo("GRU");
    }

    @Test
    @DisplayName("Deve deletar aeroporto existente")
    void shouldDeleteAirport() {
        UUID id = UUID.randomUUID();

        when(airportRepository.existsById(id)).thenReturn(true);

        airportService.delete(id);

        verify(airportRepository).deleteById(id);
    }
}
