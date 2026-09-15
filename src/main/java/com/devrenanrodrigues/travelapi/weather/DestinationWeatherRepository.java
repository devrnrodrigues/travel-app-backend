package com.devrenanrodrigues.travelapi.weather;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DestinationWeatherRepository extends JpaRepository<DestinationWeather, UUID> {
}
