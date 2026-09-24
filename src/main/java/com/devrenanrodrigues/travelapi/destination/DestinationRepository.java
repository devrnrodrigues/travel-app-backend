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

    @Query(value = "SELECT DISTINCT d.* FROM destinations d " +
            "LEFT JOIN destination_categories dc ON d.id = dc.destination_id " +
            "LEFT JOIN categories c ON dc.category_id = c.id " +
            "WHERE (CAST(:category AS text) IS NULL OR c.slug ILIKE CAST(:category AS text) OR c.name ILIKE CAST(:category AS text)) " +
            "AND (CAST(:name AS text) IS NULL OR d.name ILIKE CONCAT('%', CAST(:name AS text), '%'))",
            countQuery = "SELECT COUNT(DISTINCT d.id) FROM destinations d " +
            "LEFT JOIN destination_categories dc ON d.id = dc.destination_id " +
            "LEFT JOIN categories c ON dc.category_id = c.id " +
            "WHERE (CAST(:category AS text) IS NULL OR c.slug ILIKE CAST(:category AS text) OR c.name ILIKE CAST(:category AS text)) " +
            "AND (CAST(:name AS text) IS NULL OR d.name ILIKE CONCAT('%', CAST(:name AS text), '%'))",
            nativeQuery = true)
    Page<Destination> search(
            @Param("category") String category,
            @Param("name") String name,
            Pageable pageable
    );

    boolean existsByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);

    boolean existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(String name, String country, UUID id);
}
