package com.devrenanrodrigues.travelapi.destination;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, UUID> {

    @Query(value = "SELECT * FROM destinations d " +
           "WHERE (:category IS NULL OR (d.categories IS NOT NULL AND :category ILIKE ANY(d.categories)))",
           nativeQuery = true)
    List<Destination> search(@Param("category") String category);
}
