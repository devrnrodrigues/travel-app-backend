package com.devrenanrodrigues.travelapi.collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID> {

    @Query("SELECT DISTINCT c FROM Collection c LEFT JOIN FETCH c.photos WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Collection> findAllByUserIdWithPhotos(@Param("userId") UUID userId);

    @Query("SELECT c FROM Collection c LEFT JOIN FETCH c.photos WHERE c.id = :id AND c.user.id = :userId")
    Optional<Collection> findByIdAndUserIdWithPhotos(@Param("id") UUID id, @Param("userId") UUID userId);

    Optional<Collection> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
