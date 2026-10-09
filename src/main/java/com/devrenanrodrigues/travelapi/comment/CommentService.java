package com.devrenanrodrigues.travelapi.comment;

import com.devrenanrodrigues.travelapi.comment.dto.CommentRequestDTO;
import com.devrenanrodrigues.travelapi.comment.dto.CommentResponseDTO;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.destination.DestinationRepository;
import com.devrenanrodrigues.travelapi.storage.CloudinaryService;
import com.devrenanrodrigues.travelapi.storage.dto.CloudinaryUploadResponse;
import com.devrenanrodrigues.travelapi.user.User;
import com.devrenanrodrigues.travelapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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
    private final CloudinaryService cloudinaryService;
    private final TransactionTemplate transactionTemplate;

    public CommentResponseDTO create(UUID destinationId, UUID userId, CommentRequestDTO dto) {
        return create(destinationId, userId, dto.rating(), dto.content(), null);
    }

    public CommentResponseDTO create(
            UUID destinationId,
            UUID userId,
            Integer rating,
            String content,
            List<MultipartFile> files
    ) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A avaliação deve ser entre 1 e 5 estrelas.");
        }

        if (content == null || content.trim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O comentário não pode ser vazio.");
        }

        if (content.trim().length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O comentário deve ter no máximo 1000 caracteres.");
        }

        if (files != null && files.size() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O limite máximo é de 5 fotos por comentário.");
        }

        if (commentRepository.existsByDestinationIdAndUserId(destinationId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já avaliou este destino.");
        }

        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Destino não encontrado com o id: " + destinationId
                ));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        List<CloudinaryUploadResponse> uploadedResponses = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            String folder = "travel-app/comments/" + destinationId;
            try {
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) {
                        continue;
                    }
                    uploadedResponses.add(cloudinaryService.upload(file, folder));
                }
            } catch (Exception ex) {
                for (CloudinaryUploadResponse res : uploadedResponses) {
                    cloudinaryService.delete(res.publicId());
                }
                throw ex;
            }
        }

        Comment saved;
        try {
            saved = transactionTemplate.execute(status -> {
                Comment comment = Comment.builder()
                        .user(user)
                        .destination(destination)
                        .rating(rating)
                        .content(content.trim())
                        .helpfulCount(0)
                        .photos(new ArrayList<>())
                        .build();

                int order = 0;
                for (CloudinaryUploadResponse uploadResponse : uploadedResponses) {
                    CommentPhoto photo = CommentPhoto.builder()
                            .comment(comment)
                            .url(uploadResponse.url())
                            .publicId(uploadResponse.publicId())
                            .orderIndex(order++)
                            .build();
                    comment.getPhotos().add(photo);
                }

                Comment persisted = commentRepository.saveAndFlush(comment);
                updateDestinationRatingAndCount(destination);
                return persisted;
            });
        } catch (DataIntegrityViolationException ex) {
            for (CloudinaryUploadResponse res : uploadedResponses) {
                cloudinaryService.delete(res.publicId());
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já avaliou este destino.");
        } catch (Exception ex) {
            for (CloudinaryUploadResponse res : uploadedResponses) {
                cloudinaryService.delete(res.publicId());
            }
            throw ex;
        }

        String userName = user.getFullName();
        String userAvatarUrl = user.getAvatarUrl();
        return CommentResponseDTO.fromEntity(saved, userName, userAvatarUrl, false);
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

        Set<UUID> commentIds = commentsList.stream()
                .map(Comment::getId)
                .collect(Collectors.toSet());

        Set<UUID> votedCommentIds = (currentUserId != null && !commentIds.isEmpty())
                ? commentHelpfulVoteRepository.findCommentIdsVotedByUser(currentUserId, commentIds)
                : Set.of();

        List<CommentResponseDTO> comments = commentsList.stream()
                .map(c -> {
                    User user = c.getUser();
                    String userName = user != null ? user.getFullName() : null;
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

    public CommentResponseDTO update(UUID id, UUID userId, boolean isAdmin, CommentRequestDTO dto) {
        return update(id, userId, isAdmin, dto.rating(), dto.content(), null, false, null);
    }

    public CommentResponseDTO update(
            UUID id,
            UUID userId,
            boolean isAdmin,
            Integer rating,
            String content,
            List<UUID> keepPhotoIds,
            Boolean clearPhotos,
            List<MultipartFile> files
    ) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado com o id: " + id));

        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este comentário.");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A avaliação deve ser entre 1 e 5 estrelas.");
        }

        if (content == null || content.trim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O comentário não pode ser vazio.");
        }

        if (content.trim().length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O comentário deve ter no máximo 1000 caracteres.");
        }

        List<CommentPhoto> existingPhotos = comment.getPhotos() != null ? comment.getPhotos() : new ArrayList<>();
        List<CommentPhoto> photosToRemove = new ArrayList<>();

        if (Boolean.TRUE.equals(clearPhotos)) {
            photosToRemove.addAll(existingPhotos);
        } else if (keepPhotoIds != null) {
            for (CommentPhoto photo : existingPhotos) {
                if (!keepPhotoIds.contains(photo.getId())) {
                    photosToRemove.add(photo);
                }
            }
        }

        List<String> publicIdsToDelete = new ArrayList<>();
        for (CommentPhoto photo : photosToRemove) {
            if (photo.getPublicId() != null && !photo.getPublicId().isBlank()) {
                publicIdsToDelete.add(photo.getPublicId());
            }
        }

        int newFilesCount = files != null ? files.size() : 0;
        if (existingPhotos.size() - photosToRemove.size() + newFilesCount > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O limite máximo é de 5 fotos por comentário.");
        }

        List<CloudinaryUploadResponse> newUploadResponses = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            String folder = "travel-app/comments/" + comment.getDestination().getId();
            try {
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) {
                        continue;
                    }
                    newUploadResponses.add(cloudinaryService.upload(file, folder));
                }
            } catch (Exception ex) {
                for (CloudinaryUploadResponse resp : newUploadResponses) {
                    cloudinaryService.delete(resp.publicId());
                }
                throw ex;
            }
        }

        Comment updated;
        try {
            updated = transactionTemplate.execute(status -> {
                Comment c = commentRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado com o id: " + id));

                c.setRating(rating);
                c.setContent(content.trim());

                List<CommentPhoto> currentPhotos = c.getPhotos() != null ? c.getPhotos() : new ArrayList<>();
                List<CommentPhoto> toRemove = new ArrayList<>();
                if (Boolean.TRUE.equals(clearPhotos)) {
                    toRemove.addAll(currentPhotos);
                } else if (keepPhotoIds != null) {
                    for (CommentPhoto photo : currentPhotos) {
                        if (!keepPhotoIds.contains(photo.getId())) {
                            toRemove.add(photo);
                        }
                    }
                }
                currentPhotos.removeAll(toRemove);

                int startOrder = currentPhotos.size();
                for (CloudinaryUploadResponse resp : newUploadResponses) {
                    CommentPhoto photo = CommentPhoto.builder()
                            .comment(c)
                            .url(resp.url())
                            .publicId(resp.publicId())
                            .orderIndex(startOrder++)
                            .build();
                    currentPhotos.add(photo);
                }

                for (int i = 0; i < currentPhotos.size(); i++) {
                    currentPhotos.get(i).setOrderIndex(i);
                }

                c.setPhotos(currentPhotos);
                Comment persisted = commentRepository.save(c);

                Destination destination = persisted.getDestination();
                if (destination != null) {
                    updateDestinationRatingAndCount(destination);
                }
                return persisted;
            });
        } catch (Exception ex) {
            for (CloudinaryUploadResponse resp : newUploadResponses) {
                cloudinaryService.delete(resp.publicId());
            }
            throw ex;
        }

        for (String pubId : publicIdsToDelete) {
            cloudinaryService.delete(pubId);
        }

        User user = updated.getUser();
        String userName = user != null ? user.getFullName() : null;
        String userAvatarUrl = user != null ? user.getAvatarUrl() : null;
        boolean isHelpful = commentHelpfulVoteRepository.existsByCommentIdAndUserId(updated.getId(), userId);

        return CommentResponseDTO.fromEntity(updated, userName, userAvatarUrl, isHelpful);
    }

    public void delete(UUID id, UUID userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado com o id: " + id));

        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir este comentário.");
        }

        List<String> publicIdsToDelete = new ArrayList<>();
        if (comment.getPhotos() != null) {
            for (CommentPhoto photo : comment.getPhotos()) {
                if (photo.getPublicId() != null && !photo.getPublicId().isBlank()) {
                    publicIdsToDelete.add(photo.getPublicId());
                }
            }
        }

        transactionTemplate.executeWithoutResult(status -> {
            Comment c = commentRepository.findById(id).orElse(null);
            if (c != null) {
                commentHelpfulVoteRepository.deleteByCommentId(id);
                Destination destination = c.getDestination();
                commentRepository.delete(c);
                if (destination != null) {
                    updateDestinationRatingAndCount(destination);
                }
            }
        });

        for (String pubId : publicIdsToDelete) {
            cloudinaryService.delete(pubId);
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

        User user = saved.getUser();
        String userName = user != null ? user.getFullName() : null;
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
