package com.devrenanrodrigues.travelapi.destination;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DestinationImageRepository extends JpaRepository<DestinationImage, UUID> {
    List<DestinationImage> findByDestinationIdOrderByPositionAsc(UUID destinationId);
}
