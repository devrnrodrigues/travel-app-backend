package com.devrenanrodrigues.travelapi.comment;

import com.devrenanrodrigues.travelapi.comment.dto.CommentRequestDTO;
import com.devrenanrodrigues.travelapi.comment.dto.CommentResponseDTO;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.destination.DestinationRepository;
import com.devrenanrodrigues.travelapi.user.User;
import com.devrenanrodrigues.travelapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentHelpfulVoteRepository commentHelpfulVoteRepository;
    private final DestinationRepository destinationRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponseDTO create(UUID destinationId, UUID userId, CommentRequestDTO dto) {
        if (commentRepository.existsByDestinationIdAndUserId(destinationId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já avaliou este destino.");
        }

        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Destino não encontrado com o id: " + destinationId
                ));

        Comment comment = Comment.builder()
                .userId(userId)
                .destination(destination)
                .rating(dto.rating())
                .content(dto.content().trim())
                .helpfulCount(0)
                .build();

        try {
            Comment saved = commentRepository.saveAndFlush(comment);
            updateDestinationRatingAndCount(destination);
            User user = userRepository.findById(userId).orElse(null);
            String userName = user != null ? (user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getEmail()) : null;
            String userAvatarUrl = user != null ? user.getAvatarUrl() : null;
            return CommentResponseDTO.fromEntity(saved, userName, userAvatarUrl, false);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já avaliou este destino.");
        }
    }

    @Transactional(readOnly = true)
    public DestinationCommentsSummaryDTO findByDestinationId(UUID destinationId) {
        return findByDestinationId(destinationId, null);
    }

    @Transactional(readOnly = true)
    public DestinationCommentsSummaryDTO findByDestinationId(UUID destinationId, UUID currentUserId) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Destino não encontrado com o id: " + destinationId
            );
        }

        List<Comment> commentsList = commentRepository.findByDestinationIdOrderByCreatedAtDesc(destinationId);
        Set<UUID> userIds = commentsList.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Set<UUID> commentIds = commentsList.stream()
                .map(Comment::getId)
                .collect(Collectors.toSet());

        Set<UUID> votedCommentIds = (currentUserId != null && !commentIds.isEmpty())
                ? commentHelpfulVoteRepository.findCommentIdsVotedByUser(currentUserId, commentIds)
                : Set.of();

        List<CommentResponseDTO> comments = commentsList.stream()
                .map(c -> {
                    User user = userMap.get(c.getUserId());
                    String userName = user != null ? (user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getEmail()) : null;
                    String userAvatarUrl = user != null ? user.getAvatarUrl() : null;
                    boolean isHelpful = votedCommentIds.contains(c.getId());
                    return CommentResponseDTO.fromEntity(c, userName, userAvatarUrl, isHelpful);
                })
                .toList();

        Double avg = commentRepository.getAverageRatingByDestinationId(destinationId);
        double roundedAvg = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        return new DestinationCommentsSummaryDTO(
                destinationId,
                roundedAvg,
                comments.size(),
                comments
        );
    }

    @Transactional
    public CommentResponseDTO update(UUID id, UUID userId, boolean isAdmin, CommentRequestDTO dto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado com o id: " + id));

        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este comentário.");
        }

        comment.setRating(dto.rating());
        comment.setContent(dto.content().trim());
        Comment updated = commentRepository.save(comment);

        Destination destination = comment.getDestination();
        if (destination != null) {
            updateDestinationRatingAndCount(destination);
        }

        User user = userRepository.findById(comment.getUserId()).orElse(null);
        String userName = user != null ? (user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getEmail()) : null;
        String userAvatarUrl = user != null ? user.getAvatarUrl() : null;
        boolean isHelpful = commentHelpfulVoteRepository.existsByCommentIdAndUserId(updated.getId(), userId);

        return CommentResponseDTO.fromEntity(updated, userName, userAvatarUrl, isHelpful);
    }

    @Transactional
    public void delete(UUID id, UUID userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado com o id: " + id));

        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir este comentário.");
        }

        commentHelpfulVoteRepository.deleteByCommentId(id);

        Destination destination = comment.getDestination();
        commentRepository.delete(comment);

        if (destination != null) {
            updateDestinationRatingAndCount(destination);
        }
    }

    @Transactional
    public CommentResponseDTO toggleHelpful(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comentário não encontrado com o id: " + commentId
                ));

        Optional<CommentHelpfulVote> existingVote = commentHelpfulVoteRepository.findByCommentIdAndUserId(commentId, userId);
        boolean isHelpful;
        int currentCount = comment.getHelpfulCount() != null ? comment.getHelpfulCount() : 0;

        if (existingVote.isPresent()) {
            commentHelpfulVoteRepository.delete(existingVote.get());
            comment.setHelpfulCount(Math.max(0, currentCount - 1));
            isHelpful = false;
        } else {
            CommentHelpfulVote vote = CommentHelpfulVote.builder()
                    .comment(comment)
                    .userId(userId)
                    .build();
            commentHelpfulVoteRepository.save(vote);
            comment.setHelpfulCount(currentCount + 1);
            isHelpful = true;
        }

        Comment saved = commentRepository.save(comment);

        User user = userRepository.findById(saved.getUserId()).orElse(null);
        String userName = user != null ? (user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getEmail()) : null;
        String userAvatarUrl = user != null ? user.getAvatarUrl() : null;

        return CommentResponseDTO.fromEntity(saved, userName, userAvatarUrl, isHelpful);
    }

    private void updateDestinationRatingAndCount(Destination destination) {
        Double avg = commentRepository.getAverageRatingByDestinationId(destination.getId());
        int count = (int) commentRepository.countByDestinationId(destination.getId());
        destination.setRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        destination.setReviewCount(count);
        destinationRepository.save(destination);
    }
}
