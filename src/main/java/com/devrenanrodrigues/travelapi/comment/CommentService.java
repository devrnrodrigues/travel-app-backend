package com.devrenanrodrigues.travelapi.comment;

import com.devrenanrodrigues.travelapi.comment.dto.CommentRequestDTO;
import com.devrenanrodrigues.travelapi.comment.dto.CommentResponseDTO;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.destination.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final DestinationRepository destinationRepository;

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
                .build();

        try {
            Comment saved = commentRepository.saveAndFlush(comment);
            updateDestinationRatingAndCount(destination);
            return CommentResponseDTO.fromEntity(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já avaliou este destino.");
        }
    }

    @Transactional(readOnly = true)
    public DestinationCommentsSummaryDTO findByDestinationId(UUID destinationId) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Destino não encontrado com o id: " + destinationId
            );
        }

        List<CommentResponseDTO> comments = commentRepository.findByDestinationIdOrderByCreatedAtDesc(destinationId)
                .stream()
                .map(CommentResponseDTO::fromEntity)
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

        return CommentResponseDTO.fromEntity(updated);
    }

    @Transactional
    public void delete(UUID id, UUID userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado com o id: " + id));

        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir este comentário.");
        }

        Destination destination = comment.getDestination();
        commentRepository.delete(comment);

        if (destination != null) {
            updateDestinationRatingAndCount(destination);
        }
    }

    private void updateDestinationRatingAndCount(Destination destination) {
        Double avg = commentRepository.getAverageRatingByDestinationId(destination.getId());
        int count = (int) commentRepository.countByDestinationId(destination.getId());
        destination.setRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        destination.setReviewCount(count);
        destinationRepository.save(destination);
    }
}
