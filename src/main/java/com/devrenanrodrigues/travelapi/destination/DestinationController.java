package com.devrenanrodrigues.travelapi.destination;

import com.devrenanrodrigues.travelapi.destination.dto.DestinationDetailResponseDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationRequestDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationResponseDTO;
import com.devrenanrodrigues.travelapi.destination.dto.DestinationSummaryResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DestinationResponseDTO create(@RequestBody @Valid DestinationRequestDTO dto) {
        return destinationService.create(dto);
    }

    @PutMapping("/{id}")
    public DestinationResponseDTO update(@PathVariable UUID id, @RequestBody @Valid DestinationRequestDTO dto) {
        return destinationService.update(id, dto);
    }

    @GetMapping
    public Page<DestinationSummaryResponseDTO> findAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, page = 0, sort = "name") Pageable pageable
    ) {
        return destinationService.findAll(category, name, pageable);
    }

    @GetMapping("/{id}")
    public DestinationDetailResponseDTO findById(@PathVariable UUID id) {
        return destinationService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        destinationService.delete(id);
    }
}
