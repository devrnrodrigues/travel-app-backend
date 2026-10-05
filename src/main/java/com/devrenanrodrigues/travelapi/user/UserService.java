package com.devrenanrodrigues.travelapi.user;

import com.devrenanrodrigues.travelapi.comment.CommentRepository;
import com.devrenanrodrigues.travelapi.storage.CloudinaryService;
import com.devrenanrodrigues.travelapi.storage.dto.CloudinaryUploadResponse;
import com.devrenanrodrigues.travelapi.user.dto.UpdateProfileRequestDTO;
import com.devrenanrodrigues.travelapi.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final CommentRepository commentRepository;
    private final TransactionTemplate transactionTemplate;

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

        String oldPublicId = user.getAvatarPublicId();
        CloudinaryUploadResponse uploadResponse = cloudinaryService.upload(file, "travel-app/avatars");

        User savedUser;
        try {
            savedUser = transactionTemplate.execute(status -> {
                User u = userRepository.findById(userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
                u.setAvatarUrl(uploadResponse.url());
                u.setAvatarPublicId(uploadResponse.publicId());
                return userRepository.save(u);
            });
        } catch (Exception ex) {
            cloudinaryService.delete(uploadResponse.publicId());
            throw ex;
        }

        if (oldPublicId != null) {
            cloudinaryService.delete(oldPublicId);
        }

        long commentsCount = commentRepository.countByUserId(userId);
        return UserResponseDTO.fromEntity(savedUser, commentsCount);
    }

    public UserResponseDTO getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        long commentsCount = commentRepository.countByUserId(userId);
        return UserResponseDTO.fromEntity(user, commentsCount);
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
        long commentsCount = commentRepository.countByUserId(userId);
        return UserResponseDTO.fromEntity(savedUser, commentsCount);
    }
}
