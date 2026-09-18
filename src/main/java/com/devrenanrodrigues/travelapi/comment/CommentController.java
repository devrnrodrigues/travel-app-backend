package com.devrenanrodrigues.travelapi.comment;

import com.devrenanrodrigues.travelapi.comment.dto.CommentRequestDTO;
import com.devrenanrodrigues.travelapi.comment.dto.CommentResponseDTO;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
import com.devrenanrodrigues.travelapi.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/destinations/{destinationId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO create(
            @PathVariable UUID destinationId,
            @RequestBody @Valid CommentRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        return commentService.create(destinationId, userId, dto);
    }

    @GetMapping("/api/destinations/{destinationId}/comments")
    public DestinationCommentsSummaryDTO findByDestinationId(@PathVariable UUID destinationId) {
        return commentService.findByDestinationId(destinationId);
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
}
