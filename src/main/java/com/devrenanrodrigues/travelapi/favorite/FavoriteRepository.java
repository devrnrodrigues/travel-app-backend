package com.devrenanrodrigues.travelapi.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    @Query("SELECT f FROM Favorite f JOIN FETCH f.destination WHERE f.id.userId = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.destination ORDER BY f.createdAt DESC")
    List<Favorite> findAllWithDestination();

    boolean existsByIdUserIdAndIdDestinationId(UUID userId, UUID destinationId);

    boolean existsByIdDestinationId(UUID destinationId);

    void deleteByIdUserIdAndIdDestinationId(UUID userId, UUID destinationId);
}
