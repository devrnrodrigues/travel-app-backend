package com.devrenanrodrigues.travelapi.airport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AirportRepository extends JpaRepository<Airport, UUID> {

    Optional<Airport> findByIataCodeIgnoreCase(String iataCode);

    boolean existsByIataCodeIgnoreCase(String iataCode);
}
