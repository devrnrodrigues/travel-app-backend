package com.devrenanrodrigues.travelapi.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByDestinationIdOrderByCreatedAtDesc(UUID destinationId);

    List<Comment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByDestinationIdAndUserId(UUID destinationId, UUID userId);

    boolean existsByDestinationId(UUID destinationId);

    long countByDestinationId(UUID destinationId);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.destination.id = :destinationId")
    Double getAverageRatingByDestinationId(@Param("destinationId") UUID destinationId);
}
