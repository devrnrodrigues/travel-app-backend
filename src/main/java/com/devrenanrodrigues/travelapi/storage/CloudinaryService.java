package com.devrenanrodrigues.travelapi.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.devrenanrodrigues.travelapi.storage.dto.CloudinaryUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    private final Cloudinary cloudinary;

    public CloudinaryUploadResponse upload(MultipartFile file, String folder) {
        validateWebpFile(file);

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getInputStream(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            return new CloudinaryUploadResponse(secureUrl, publicId);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao enviar imagem para o Cloudinary.");
        }
    }

    private void validateWebpFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo vazio ou inválido.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A imagem deve ter no máximo 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("image/webp")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato inválido. Apenas imagens WebP são permitidas.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".webp")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato inválido. A extensão do arquivo deve ser .webp.");
        }
    }

    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException ignored) {
        }
    }
}
