package com.devrenanrodrigues.travelapi.favorite;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    @Query("SELECT f FROM Favorite f JOIN FETCH f.destination d LEFT JOIN FETCH d.primaryCategory WHERE f.id.userId = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.destination d LEFT JOIN FETCH d.primaryCategory ORDER BY f.createdAt DESC")
    List<Favorite> findAllWithDestination();

    @Query(
        value = "SELECT f FROM Favorite f JOIN FETCH f.destination d LEFT JOIN FETCH d.primaryCategory WHERE f.id.userId = :userId ORDER BY f.createdAt DESC",
        countQuery = "SELECT COUNT(f) FROM Favorite f WHERE f.id.userId = :userId"
    )
    Page<Favorite> findByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query(
        value = "SELECT f FROM Favorite f JOIN FETCH f.destination d LEFT JOIN FETCH d.primaryCategory ORDER BY f.createdAt DESC",
        countQuery = "SELECT COUNT(f) FROM Favorite f"
    )
    Page<Favorite> findAllWithDestination(Pageable pageable);

    @Query(
        value = "SELECT f FROM Favorite f JOIN FETCH f.destination d LEFT JOIN FETCH d.primaryCategory " +
                "WHERE f.id.userId = :userId " +
                "AND (" +
                "LOWER(d.name) LIKE :pattern OR " +
                "LOWER(d.city) LIKE :pattern OR " +
                "LOWER(d.state) LIKE :pattern OR " +
                "LOWER(d.country) LIKE :pattern) " +
                "ORDER BY f.createdAt DESC",
        countQuery = "SELECT COUNT(f) FROM Favorite f JOIN f.destination d " +
                "WHERE f.id.userId = :userId " +
                "AND (" +
                "LOWER(d.name) LIKE :pattern OR " +
                "LOWER(d.city) LIKE :pattern OR " +
                "LOWER(d.state) LIKE :pattern OR " +
                "LOWER(d.country) LIKE :pattern)"
    )
    Page<Favorite> findByUserIdAndSearch(
            @Param("userId") UUID userId,
            @Param("pattern") String pattern,
            Pageable pageable
    );

    @Query(
        value = "SELECT f FROM Favorite f JOIN FETCH f.destination d LEFT JOIN FETCH d.primaryCategory " +
                "WHERE (" +
                "LOWER(d.name) LIKE :pattern OR " +
                "LOWER(d.city) LIKE :pattern OR " +
                "LOWER(d.state) LIKE :pattern OR " +
                "LOWER(d.country) LIKE :pattern) " +
                "ORDER BY f.createdAt DESC",
        countQuery = "SELECT COUNT(f) FROM Favorite f JOIN f.destination d " +
                "WHERE (" +
                "LOWER(d.name) LIKE :pattern OR " +
                "LOWER(d.city) LIKE :pattern OR " +
                "LOWER(d.state) LIKE :pattern OR " +
                "LOWER(d.country) LIKE :pattern)"
    )
    Page<Favorite> findAllWithSearch(
            @Param("pattern") String pattern,
            Pageable pageable
    );

    boolean existsByIdUserIdAndIdDestinationId(UUID userId, UUID destinationId);

    boolean existsByIdDestinationId(UUID destinationId);

    void deleteByIdUserIdAndIdDestinationId(UUID userId, UUID destinationId);
}
