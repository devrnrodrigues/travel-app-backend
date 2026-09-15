package com.devrenanrodrigues.travelapi.comment.dto;

import java.util.List;
import java.util.UUID;

public record DestinationCommentsSummaryDTO(
        UUID destinationId,
        Double averageRating,
        long totalComments,
        List<CommentResponseDTO> comments
) {
}
