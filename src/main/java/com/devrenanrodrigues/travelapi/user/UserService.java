package com.devrenanrodrigues.travelapi.user;

import com.devrenanrodrigues.travelapi.storage.CloudinaryService;
import com.devrenanrodrigues.travelapi.storage.dto.CloudinaryUploadResponse;
import com.devrenanrodrigues.travelapi.user.dto.UpdateProfileRequestDTO;
import com.devrenanrodrigues.travelapi.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public UserResponseDTO updateAvatar(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo de imagem obrigatório.");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        boolean isImageByContentType = contentType != null && contentType.startsWith("image/");
        boolean isImageByExtension = originalFilename != null && originalFilename.matches("(?i).*\\.(jpg|jpeg|png|webp|gif|heic|bmp)$");

        if (!isImageByContentType && !isImageByExtension) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo enviado deve ser uma imagem.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (user.getAvatarPublicId() != null) {
            cloudinaryService.delete(user.getAvatarPublicId());
        }

        CloudinaryUploadResponse uploadResponse = cloudinaryService.upload(file, "travel-app/avatars");

        user.setAvatarUrl(uploadResponse.url());
        user.setAvatarPublicId(uploadResponse.publicId());

        User savedUser = userRepository.save(user);
        return UserResponseDTO.fromEntity(savedUser);
    }

    public UserResponseDTO getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        return UserResponseDTO.fromEntity(user);
    }

    @Transactional
    public UserResponseDTO updateProfile(UUID userId, UpdateProfileRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (dto.fullName() != null && !dto.fullName().isBlank()) {
            user.setFullName(dto.fullName().trim());
        }
        if (dto.bio() != null) {
            user.setBio(dto.bio().trim());
        }
        if (dto.nationality() != null) {
            user.setNationality(dto.nationality().trim());
        }

        User savedUser = userRepository.save(user);
        return UserResponseDTO.fromEntity(savedUser);
    }
}
