package com.devrenanrodrigues.travelapi.destination;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, UUID> {

    @Query("SELECT d FROM Destination d LEFT JOIN FETCH d.nearestAirport " +
           "WHERE (:category IS NULL OR LOWER(d.category) = LOWER(:category)) " +
           "AND (:city IS NULL OR LOWER(d.city) = LOWER(:city)) " +
           "AND (:country IS NULL OR LOWER(d.country) = LOWER(:country))")
    List<Destination> search(
            @Param("category") String category,
            @Param("city") String city,
            @Param("country") String country
    );
}
