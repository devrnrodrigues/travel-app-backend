package com.devrenanrodrigues.travelapi.collection;

import com.devrenanrodrigues.travelapi.collection.dto.CollectionPhotoResponseDTO;
import com.devrenanrodrigues.travelapi.collection.dto.CollectionResponseDTO;
import com.devrenanrodrigues.travelapi.collection.dto.UpdateCollectionRequestDTO;
import com.devrenanrodrigues.travelapi.collection.dto.UpdatePhotoCaptionDTO;
import com.devrenanrodrigues.travelapi.storage.CloudinaryService;
import com.devrenanrodrigues.travelapi.storage.dto.CloudinaryUploadResponse;
import com.devrenanrodrigues.travelapi.user.User;
import com.devrenanrodrigues.travelapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionPhotoRepository collectionPhotoRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public List<CollectionResponseDTO> getCollections(UUID userId) {
        return collectionRepository.findAllByUserIdWithPhotos(userId)
                .stream()
                .map(CollectionResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CollectionResponseDTO getCollectionById(UUID id, UUID userId) {
        Collection collection = collectionRepository.findByIdAndUserIdWithPhotos(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));
        return CollectionResponseDTO.fromEntity(collection);
    }

    @Transactional
    public CollectionResponseDTO createCollection(UUID userId, String title, List<MultipartFile> files) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título da coleção é obrigatório.");
        }

        String trimmedTitle = title.trim();
        if (trimmedTitle.length() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título deve ter no máximo 30 caracteres.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        Collection collection = Collection.builder()
                .user(user)
                .title(trimmedTitle)
                .photos(new ArrayList<>())
                .build();

        collection = collectionRepository.save(collection);

        if (files != null && !files.isEmpty()) {
            String folder = "travel-app/collections/" + userId;
            int order = 0;
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                CloudinaryUploadResponse uploadResponse = cloudinaryService.upload(file, folder);
                CollectionPhoto photo = CollectionPhoto.builder()
                        .collection(collection)
                        .url(uploadResponse.url())
                        .publicId(uploadResponse.publicId())
                        .orderIndex(order++)
                        .build();
                collection.getPhotos().add(photo);
            }
            collection = collectionRepository.save(collection);
        }

        return CollectionResponseDTO.fromEntity(collection);
    }

    @Transactional
    public CollectionResponseDTO updateCollection(UUID id, UUID userId, UpdateCollectionRequestDTO dto) {
        Collection collection = collectionRepository.findByIdAndUserIdWithPhotos(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));

        if (dto.title() != null && !dto.title().isBlank()) {
            collection.setTitle(dto.title().trim());
        }

        if (dto.deletePhotoIds() != null && !dto.deletePhotoIds().isEmpty() && collection.getPhotos() != null) {
            List<CollectionPhoto> photosToDelete = collection.getPhotos().stream()
                    .filter(p -> dto.deletePhotoIds().contains(p.getId()))
                    .toList();

            for (CollectionPhoto photo : photosToDelete) {
                cloudinaryService.delete(photo.getPublicId());
                collection.getPhotos().remove(photo);
            }
        }

        collection = collectionRepository.save(collection);

        return CollectionResponseDTO.fromEntity(collection);
    }

    @Transactional
    public void deleteCollection(UUID id, UUID userId) {
        Collection collection = collectionRepository.findByIdAndUserIdWithPhotos(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));

        if (collection.getPhotos() != null) {
            for (CollectionPhoto photo : collection.getPhotos()) {
                cloudinaryService.delete(photo.getPublicId());
            }
        }

        collectionRepository.delete(collection);
    }

    @Transactional
    public CollectionResponseDTO addPhotos(UUID id, UUID userId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma foto informada para envio.");
        }

        Collection collection = collectionRepository.findByIdAndUserIdWithPhotos(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));

        String folder = "travel-app/collections/" + userId;
        int nextOrder = collection.getPhotos() == null ? 0 : collection.getPhotos().size();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            CloudinaryUploadResponse uploadResponse = cloudinaryService.upload(file, folder);
            CollectionPhoto photo = CollectionPhoto.builder()
                    .collection(collection)
                    .url(uploadResponse.url())
                    .publicId(uploadResponse.publicId())
                    .orderIndex(nextOrder++)
                    .build();
            collection.getPhotos().add(photo);
        }

        collection = collectionRepository.save(collection);
        return CollectionResponseDTO.fromEntity(collection);
    }

    @Transactional
    public CollectionPhotoResponseDTO updatePhotoCaption(UUID collectionId, UUID photoId, UUID userId, UpdatePhotoCaptionDTO dto) {
        if (!collectionRepository.existsByIdAndUserId(collectionId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada.");
        }

        CollectionPhoto photo = collectionPhotoRepository.findByIdAndCollectionId(photoId, collectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto não encontrada na coleção."));

        String newCaption = dto.caption() == null ? null : dto.caption().trim();
        if (newCaption != null && newCaption.length() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A legenda deve ter no máximo 30 caracteres.");
        }

        photo.setCaption(newCaption);
        photo = collectionPhotoRepository.save(photo);

        return CollectionPhotoResponseDTO.fromEntity(photo);
    }

    @Transactional
    public void deletePhoto(UUID collectionId, UUID photoId, UUID userId) {
        Collection collection = collectionRepository.findByIdAndUserIdWithPhotos(collectionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));

        CollectionPhoto photo = collectionPhotoRepository.findByIdAndCollectionId(photoId, collectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto não encontrada na coleção."));

        cloudinaryService.delete(photo.getPublicId());
        collection.getPhotos().remove(photo);
        collectionPhotoRepository.delete(photo);
    }
}
