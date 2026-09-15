package com.devrenanrodrigues.travelapi.comment;

import com.devrenanrodrigues.travelapi.comment.dto.CommentRequestDTO;
import com.devrenanrodrigues.travelapi.comment.dto.CommentResponseDTO;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.destination.DestinationRepository;
import lombok.RequiredArgsConstructor;
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
    public CommentResponseDTO create(UUID destinationId, CommentRequestDTO dto) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Destino não encontrado com o id: " + destinationId
                ));

        Comment comment = Comment.builder()
                .userId(dto.userId())
                .destination(destination)
                .rating(dto.rating())
                .content(dto.content().trim())
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentResponseDTO.fromEntity(saved);
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
    public void delete(UUID id) {
        if (!commentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado com o id: " + id);
        }
        commentRepository.deleteById(id);
    }
}
