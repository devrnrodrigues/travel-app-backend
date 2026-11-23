package com.devrenanrodrigues.travelapi.user;

import com.devrenanrodrigues.travelapi.comment.CommentRepository;
import com.devrenanrodrigues.travelapi.storage.CloudinaryService;
import com.devrenanrodrigues.travelapi.storage.dto.CloudinaryUploadResponse;
import com.devrenanrodrigues.travelapi.user.dto.UpdateProfileRequestDTO;
import com.devrenanrodrigues.travelapi.user.dto.UserResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Deve retornar perfil do usuário e contagem de comentários")
    void shouldGetProfileSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .fullName("Renan")
                .email("renan@teste.com")
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.countByUserId(userId)).thenReturn(7L);

        UserResponseDTO response = userService.getProfile(userId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.fullName()).isEqualTo("Renan");
        assertThat(response.commentsCount()).isEqualTo(7L);
    }

    @Test
    @DisplayName("Deve lançar 404 quando perfil do usuário não for encontrado")
    void shouldThrow404WhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("Deve atualizar os campos de perfil do usuário")
    void shouldUpdateProfileSuccessfully() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .fullName("Nome Antigo")
                .bio("Bio Antiga")
                .nationality("Brasil")
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .build();

        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO("Novo Nome", "Nova Bio", "Portugal");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commentRepository.countByUserId(userId)).thenReturn(3L);

        UserResponseDTO response = userService.updateProfile(userId, dto);

        assertThat(response).isNotNull();
        assertThat(response.fullName()).isEqualTo("Novo Nome");
        assertThat(response.bio()).isEqualTo("Nova Bio");
        assertThat(response.nationality()).isEqualTo("Portugal");
    }

    @Test
    @DisplayName("Deve atualizar avatar com sucesso e deletar imagem anterior do Cloudinary")
    @SuppressWarnings("unchecked")
    void shouldUpdateAvatarAndDeleteOldAvatar() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .avatarUrl("http://old-avatar.jpg")
                .avatarPublicId("old-public-id")
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .build();

        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "imagem-fake".getBytes());
        CloudinaryUploadResponse uploadResponse = new CloudinaryUploadResponse("http://new-avatar.jpg", "new-public-id");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cloudinaryService.upload(eq(file), anyString())).thenReturn(uploadResponse);

        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<User> callback = invocation.getArgument(0);
            return callback.doInTransaction(org.mockito.Mockito.mock(TransactionStatus.class));
        });
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commentRepository.countByUserId(userId)).thenReturn(2L);

        UserResponseDTO response = userService.updateAvatar(userId, file);

        assertThat(response).isNotNull();
        assertThat(response.avatarUrl()).isEqualTo("http://new-avatar.jpg");
        verify(cloudinaryService).delete("old-public-id");
    }

    @Test
    @DisplayName("Deve deletar imagem nova no Cloudinary caso a transação de salvar usuário falhe")
    @SuppressWarnings("unchecked")
    void shouldRollbackCloudinaryUploadWhenTransactionFails() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .build();

        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "imagem-fake".getBytes());
        CloudinaryUploadResponse uploadResponse = new CloudinaryUploadResponse("http://new-avatar.jpg", "new-public-id");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cloudinaryService.upload(eq(file), anyString())).thenReturn(uploadResponse);
        when(transactionTemplate.execute(any())).thenThrow(new RuntimeException("Falha no banco"));

        assertThatThrownBy(() -> userService.updateAvatar(userId, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Falha no banco");

        verify(cloudinaryService).delete("new-public-id");
    }

    @Test
    @DisplayName("Deve lançar 400 ao tentar atualizar avatar com arquivo nulo ou vazio")
    void shouldThrow400WhenFileIsEmpty() {
        UUID userId = UUID.randomUUID();
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> userService.updateAvatar(userId, emptyFile))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        assertThatThrownBy(() -> userService.updateAvatar(userId, null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }
}
