package com.devrenanrodrigues.travelapi.flight;

import com.devrenanrodrigues.travelapi.flight.dto.FlightCardResponseDTO;
import com.devrenanrodrigues.travelapi.flight.dto.FlightSearchRequestDTO;
import com.devrenanrodrigues.travelapi.flight.dto.MultiStopsFlightSearchRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FlightController {

    private final FlightService flightService;

    @GetMapping("/search")
    public List<FlightCardResponseDTO> searchFlights(@Valid @ModelAttribute FlightSearchRequestDTO request) {
        return flightService.searchFlights(request);
    }

    @GetMapping("/search-multi-stops")
    public List<FlightCardResponseDTO> searchFlightsMultiStops(@Valid @ModelAttribute MultiStopsFlightSearchRequestDTO request) {
        return flightService.searchFlightsMultiStops(request);
    }
}
