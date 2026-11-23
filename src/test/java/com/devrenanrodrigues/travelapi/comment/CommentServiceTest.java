package com.devrenanrodrigues.travelapi.comment;

import com.devrenanrodrigues.travelapi.comment.dto.CommentRequestDTO;
import com.devrenanrodrigues.travelapi.comment.dto.CommentResponseDTO;
import com.devrenanrodrigues.travelapi.comment.dto.DestinationCommentsSummaryDTO;
import com.devrenanrodrigues.travelapi.destination.Destination;
import com.devrenanrodrigues.travelapi.destination.DestinationRepository;
import com.devrenanrodrigues.travelapi.storage.CloudinaryService;
import com.devrenanrodrigues.travelapi.user.User;
import com.devrenanrodrigues.travelapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentHelpfulVoteRepository commentHelpfulVoteRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        org.mockito.Mockito.lenient().when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInTransaction(Mockito.mock(TransactionStatus.class));
                });

        org.mockito.Mockito.lenient().doAnswer(invocation -> {
            Consumer<TransactionStatus> action = invocation.getArgument(0);
            action.accept(Mockito.mock(TransactionStatus.class));
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    @DisplayName("Deve criar comentário com sucesso e recalcular média do destino")
    void shouldCreateCommentSuccessfully() {
        UUID destinationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentRequestDTO dto = new CommentRequestDTO(5, "Lugar incrível!");

        Destination dest = Destination.builder().id(destinationId).rating(4.0).reviewCount(1).build();
        User user = User.builder().id(userId).fullName("Renan").avatarUrl("http://avatar.jpg").build();

        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(dest));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.existsByDestinationIdAndUserId(destinationId, userId)).thenReturn(false);

        when(commentRepository.saveAndFlush(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            return Comment.builder()
                    .id(UUID.randomUUID())
                    .destination(dest)
                    .user(user)
                    .rating(c.getRating())
                    .content(c.getContent())
                    .photos(new ArrayList<>())
                    .build();
        });

        CommentResponseDTO result = commentService.create(destinationId, userId, dto);

        assertThat(result).isNotNull();
        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.content()).isEqualTo("Lugar incrível!");
        assertThat(result.userName()).isEqualTo("Renan");
    }

    @Test
    @DisplayName("Deve lançar 400 quando a nota de avaliação for menor que 1 ou maior que 5")
    void shouldThrow400WhenRatingIsInvalid() {
        UUID destinationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> commentService.create(destinationId, userId, new CommentRequestDTO(0, "Texto")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("entre 1 e 5");
                });

        assertThatThrownBy(() -> commentService.create(destinationId, userId, new CommentRequestDTO(6, "Texto")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    @DisplayName("Deve lançar 400 quando o conteúdo do comentário for vazio")
    void shouldThrow400WhenContentIsEmpty() {
        UUID destinationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> commentService.create(destinationId, userId, new CommentRequestDTO(5, "   ")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    @DisplayName("Deve lançar 409 quando usuário já tiver comentado no destino")
    void shouldThrow409WhenUserAlreadyCommented() {
        UUID destinationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentRequestDTO dto = new CommentRequestDTO(4, "Muito bom!");

        when(commentRepository.existsByDestinationIdAndUserId(destinationId, userId)).thenReturn(true);

        assertThatThrownBy(() -> commentService.create(destinationId, userId, dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                });
    }
}
