package com.devrenanrodrigues.travelapi.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("SELECT DISTINCT c FROM Comment c JOIN FETCH c.user LEFT JOIN FETCH c.photos WHERE c.destination.id = :destinationId ORDER BY c.createdAt DESC")
    List<Comment> findByDestinationIdOrderByCreatedAtDesc(@Param("destinationId") UUID destinationId);

    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Comment> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Comment c WHERE c.destination.id = :destinationId AND c.user.id = :userId")
    boolean existsByDestinationIdAndUserId(@Param("destinationId") UUID destinationId, @Param("userId") UUID userId);

    boolean existsByDestinationId(UUID destinationId);

    long countByDestinationId(UUID destinationId);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.destination.id = :destinationId")
    Double getAverageRatingByDestinationId(@Param("destinationId") UUID destinationId);
}
