package com.devrenanrodrigues.travelapi.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.devrenanrodrigues.travelapi.storage.dto.CloudinaryUploadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    @DisplayName("Deve enviar imagem WebP com sucesso para o Cloudinary")
    @SuppressWarnings("unchecked")
    void shouldUploadWebpImageSuccessfully() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.webp", "image/webp", "bytes-webp".getBytes());

        Map<String, Object> result = Map.of(
                "secure_url", "https://res.cloudinary.com/demo/image/upload/v1/avatar.webp",
                "public_id", "travel-app/avatars/123"
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(result);

        CloudinaryUploadResponse response = cloudinaryService.upload(file, "travel-app/avatars");

        assertThat(response).isNotNull();
        assertThat(response.url()).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/avatar.webp");
        assertThat(response.publicId()).isEqualTo("travel-app/avatars/123");
    }

    @Test
    @DisplayName("Deve rejeitar arquivo que não seja do formato image/webp")
    void shouldRejectNonWebpFile() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "bytes-jpg".getBytes());

        assertThatThrownBy(() -> cloudinaryService.upload(file, "folder"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("Apenas imagens WebP são permitidas");
                });
    }

    @Test
    @DisplayName("Deve rejeitar arquivo maior que 5MB")
    void shouldRejectFileExceeding5Mb() {
        byte[] bigContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile("file", "avatar.webp", "image/webp", bigContent);

        assertThatThrownBy(() -> cloudinaryService.upload(file, "folder"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).contains("máximo 5MB");
                });
    }

    @Test
    @DisplayName("Deve lançar 500 quando uploader do Cloudinary falhar")
    @SuppressWarnings("unchecked")
    void shouldThrow500WhenCloudinaryFails() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.webp", "image/webp", "bytes-webp".getBytes());

        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new RuntimeException("Cloudinary API timeout"));

        assertThatThrownBy(() -> cloudinaryService.upload(file, "folder"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Test
    @DisplayName("Deve excluir imagem por publicId")
    @SuppressWarnings("unchecked")
    void shouldDeleteImageByPublicId() throws IOException {
        when(uploader.destroy(any(), anyMap())).thenReturn(Map.of("result", "ok"));

        cloudinaryService.delete("public-id-123");

        verify(uploader).destroy(any(), anyMap());
    }
}
