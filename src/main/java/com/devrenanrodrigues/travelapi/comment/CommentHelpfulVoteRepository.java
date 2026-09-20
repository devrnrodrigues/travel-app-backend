package com.devrenanrodrigues.travelapi.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CommentHelpfulVoteRepository extends JpaRepository<CommentHelpfulVote, UUID> {

    boolean existsByCommentIdAndUserId(UUID commentId, UUID userId);

    Optional<CommentHelpfulVote> findByCommentIdAndUserId(UUID commentId, UUID userId);

    void deleteByCommentIdAndUserId(UUID commentId, UUID userId);

    void deleteByCommentId(UUID commentId);

    @Query("SELECT v.comment.id FROM CommentHelpfulVote v WHERE v.userId = :userId AND v.comment.id IN :commentIds")
    Set<UUID> findCommentIdsVotedByUser(@Param("userId") UUID userId, @Param("commentIds") Collection<UUID> commentIds);
}
