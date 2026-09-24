package com.devrenanrodrigues.travelapi.destination;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, UUID> {

    @Query(value = "SELECT DISTINCT d FROM Destination d " +
            "LEFT JOIN d.categories c " +
            "WHERE (:category IS NULL OR LOWER(c.slug) = LOWER(:category) OR LOWER(c.name) = LOWER(:category)) " +
            "AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))",
            countQuery = "SELECT COUNT(DISTINCT d) FROM Destination d " +
            "LEFT JOIN d.categories c " +
            "WHERE (:category IS NULL OR LOWER(c.slug) = LOWER(:category) OR LOWER(c.name) = LOWER(:category)) " +
            "AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Destination> search(
            @Param("category") String category,
            @Param("name") String name,
            Pageable pageable
    );

    boolean existsByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);

    boolean existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(String name, String country, UUID id);
}
