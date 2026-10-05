package com.devrenanrodrigues.travelapi.destination;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, UUID> {

    @Query(value = "SELECT d.* FROM destinations d " +
            "WHERE d.id IN (" +
            "  SELECT DISTINCT d2.id FROM destinations d2 " +
            "  LEFT JOIN destination_categories dc ON d2.id = dc.destination_id " +
            "  LEFT JOIN categories c ON dc.category_id = c.id " +
            "  LEFT JOIN categories cp ON d2.primary_category_id = cp.id " +
            "  WHERE (CAST(:category AS text) IS NULL OR c.slug ILIKE CAST(:category AS text) OR c.name ILIKE CAST(:category AS text) OR cp.slug ILIKE CAST(:category AS text) OR cp.name ILIKE CAST(:category AS text)) " +
            "  AND (CAST(:name AS text) IS NULL OR " +
            "  d2.name ILIKE CONCAT('%', CAST(:name AS text), '%') OR " +
            "  d2.city ILIKE CONCAT('%', CAST(:name AS text), '%') OR " +
            "  d2.state ILIKE CONCAT('%', CAST(:name AS text), '%') OR " +
            "  d2.country ILIKE CONCAT('%', CAST(:name AS text), '%'))" +
            ") " +
            "ORDER BY " +
            "CASE WHEN CAST(:sortBy AS text) = 'name_asc' THEN d.name END ASC, " +
            "CASE WHEN CAST(:sortBy AS text) = 'name_desc' THEN d.name END DESC, " +
            "CASE WHEN CAST(:sortBy AS text) = 'rating_desc' THEN d.rating END DESC NULLS LAST, " +
            "CASE WHEN CAST(:sortBy AS text) = 'rating_desc' THEN d.review_count END DESC NULLS LAST, " +
            "CASE WHEN CAST(:sortBy AS text) = 'rating_asc' THEN d.rating END ASC NULLS LAST, " +
            "CASE WHEN CAST(:sortBy AS text) = 'price_asc' THEN CAST(NULLIF(SUBSTRING(CAST(d.ai_cost_estimates AS text) FROM '\"min\":\\s*([0-9.]+)'), '') AS numeric) END ASC NULLS LAST, " +
            "CASE WHEN CAST(:sortBy AS text) = 'price_desc' THEN CAST(NULLIF(SUBSTRING(CAST(d.ai_cost_estimates AS text) FROM '\"max\":\\s*([0-9.]+)'), '') AS numeric) END DESC NULLS LAST, " +
            "d.popularity DESC NULLS LAST, d.approximate_population DESC NULLS LAST, d.name ASC",
            countQuery = "SELECT COUNT(DISTINCT d.id) FROM destinations d " +
            "LEFT JOIN destination_categories dc ON d.id = dc.destination_id " +
            "LEFT JOIN categories c ON dc.category_id = c.id " +
            "LEFT JOIN categories cp ON d.primary_category_id = cp.id " +
            "WHERE (CAST(:category AS text) IS NULL OR c.slug ILIKE CAST(:category AS text) OR c.name ILIKE CAST(:category AS text) OR cp.slug ILIKE CAST(:category AS text) OR cp.name ILIKE CAST(:category AS text)) " +
            "AND (CAST(:name AS text) IS NULL OR " +
            "d.name ILIKE CONCAT('%', CAST(:name AS text), '%') OR " +
            "d.city ILIKE CONCAT('%', CAST(:name AS text), '%') OR " +
            "d.state ILIKE CONCAT('%', CAST(:name AS text), '%') OR " +
            "d.country ILIKE CONCAT('%', CAST(:name AS text), '%'))",
            nativeQuery = true)
    Page<Destination> search(
            @Param("category") String category,
            @Param("name") String name,
            @Param("sortBy") String sortBy,
            Pageable pageable
    );

    @Query("SELECT d FROM Destination d LEFT JOIN FETCH d.images WHERE d.id = :id")
    Optional<Destination> findByIdWithImages(@Param("id") UUID id);

    @Query("SELECT DISTINCT d FROM Destination d LEFT JOIN FETCH d.primaryCategory WHERE d.id IN :ids")
    java.util.List<Destination> findAllByIdInWithPrimaryCategory(@Param("ids") java.util.Collection<UUID> ids);

    boolean existsByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);

    boolean existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(String name, String country, UUID id);
}
