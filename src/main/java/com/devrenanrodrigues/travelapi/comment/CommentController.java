package com.devrenanrodrigues.travelapi.comment;

import com.devrenanrodrigues.travelapi.comment.dto.CommentRequestDTO;
import com.devrenanrodrigues.travelapi.comment.dto.CommentResponseDTO;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
import com.devrenanrodrigues.travelapi.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping(value = "/api/destinations/{destinationId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO create(
            @PathVariable UUID destinationId,
            @RequestBody @Valid CommentRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        return commentService.create(destinationId, userId, dto);
    }

    @PostMapping(value = "/api/destinations/{destinationId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO createWithPhotos(
            @PathVariable UUID destinationId,
            @RequestParam("rating") Integer rating,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        return commentService.create(destinationId, userId, rating, content, files);
    }

    @GetMapping("/api/destinations/{destinationId}/comments")
    public DestinationCommentsSummaryDTO findByDestinationId(
            @PathVariable UUID destinationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID currentUserId = null;
        if (jwt != null) {
            try {
                currentUserId = SecurityUtils.getUserId(jwt);
            } catch (Exception ignored) {
            }
        }
        return commentService.findByDestinationId(destinationId, currentUserId);
    }

    @PutMapping("/api/comments/{id}")
    public CommentResponseDTO update(
            @PathVariable UUID id,
            @RequestBody @Valid CommentRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        boolean isAdmin = SecurityUtils.isAdmin(jwt);
        return commentService.update(id, userId, isAdmin, dto);
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        boolean isAdmin = SecurityUtils.isAdmin(jwt);
        commentService.delete(id, userId, isAdmin);
    }

    @PostMapping("/api/comments/{id}/helpful")
    public CommentResponseDTO toggleHelpful(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        return commentService.toggleHelpful(id, userId);
    }
}
