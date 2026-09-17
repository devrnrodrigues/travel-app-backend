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

    @Query(value = "SELECT * FROM destinations d " +
           "WHERE (:category IS NULL OR (d.categories IS NOT NULL AND :category ILIKE ANY(d.categories))) " +
           "AND (:name IS NULL OR d.name ILIKE CONCAT('%', :name, '%'))",
           countQuery = "SELECT count(*) FROM destinations d " +
           "WHERE (:category IS NULL OR (d.categories IS NOT NULL AND :category ILIKE ANY(d.categories))) " +
           "AND (:name IS NULL OR d.name ILIKE CONCAT('%', :name, '%'))",
           nativeQuery = true)
    Page<Destination> search(
            @Param("category") String category,
            @Param("name") String name,
            Pageable pageable
    );

    boolean existsByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);

    boolean existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(String name, String country, UUID id);
}
