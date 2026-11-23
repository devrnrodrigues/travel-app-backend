package com.devrenanrodrigues.travelapi.collection;

import com.devrenanrodrigues.travelapi.collection.dto.CollectionResponseDTO;
import com.devrenanrodrigues.travelapi.collection.dto.UpdateCollectionRequestDTO;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CollectionPhotoRepository collectionPhotoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private CollectionService collectionService;

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
    @DisplayName("Deve listar todas as coleções de fotos do usuário")
    void shouldGetCollectionsForUser() {
        UUID userId = UUID.randomUUID();
        Collection col = Collection.builder()
                .id(UUID.randomUUID())
                .title("Férias 2026")
                .photos(new ArrayList<>())
                .build();

        when(collectionRepository.findAllByUserIdWithPhotos(userId)).thenReturn(List.of(col));

        List<CollectionResponseDTO> result = collectionService.getCollections(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("Férias 2026");
    }

    @Test
    @DisplayName("Deve buscar coleção por ID para o usuário")
    void shouldGetCollectionById() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Collection col = Collection.builder().id(id).title("Viagem Europa").photos(new ArrayList<>()).build();

        when(collectionRepository.findByIdAndUserIdWithPhotos(id, userId)).thenReturn(Optional.of(col));

        CollectionResponseDTO result = collectionService.getCollectionById(id, userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.title()).isEqualTo("Viagem Europa");
    }

    @Test
    @DisplayName("Deve lançar 404 quando coleção não for encontrada")
    void shouldThrow404WhenCollectionNotFound() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(collectionRepository.findByIdAndUserIdWithPhotos(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.getCollectionById(id, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("Deve criar coleção sem fotos com sucesso")
    void shouldCreateCollectionWithoutPhotos() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(collectionRepository.save(any(Collection.class))).thenAnswer(invocation -> {
            Collection c = invocation.getArgument(0);
            return Collection.builder()
                    .id(UUID.randomUUID())
                    .user(c.getUser())
                    .title(c.getTitle())
                    .photos(new ArrayList<>())
                    .build();
        });

        CollectionResponseDTO result = collectionService.createCollection(userId, "Mochilão", null);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Mochilão");
    }

    @Test
    @DisplayName("Deve lançar 400 ao tentar criar coleção com título vazio ou maior que 30 caracteres")
    void shouldThrow400WhenTitleIsInvalid() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> collectionService.createCollection(userId, "   ", null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        String title31 = "a".repeat(31);
        assertThatThrownBy(() -> collectionService.createCollection(userId, title31, null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    @DisplayName("Deve atualizar o título da coleção com sucesso")
    void shouldUpdateCollectionTitle() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Collection col = Collection.builder().id(id).title("Título Antigo").photos(new ArrayList<>()).build();

        when(collectionRepository.findByIdAndUserIdWithPhotos(id, userId)).thenReturn(Optional.of(col));
        when(collectionRepository.save(any(Collection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CollectionResponseDTO updated = collectionService.updateCollection(id, userId, new UpdateCollectionRequestDTO("Título Novo", null));

        assertThat(updated).isNotNull();
        assertThat(col.getTitle()).isEqualTo("Título Novo");
    }

    @Test
    @DisplayName("Deve excluir coleção e suas fotos")
    void shouldDeleteCollection() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Collection col = Collection.builder().id(id).photos(new ArrayList<>()).build();

        when(collectionRepository.findByIdAndUserIdWithPhotos(id, userId)).thenReturn(Optional.of(col));

        collectionService.deleteCollection(id, userId);

        verify(collectionRepository).delete(col);
    }
}
