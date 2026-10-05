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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionPhotoRepository collectionPhotoRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final TransactionTemplate transactionTemplate;

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

    public CollectionResponseDTO createCollection(UUID userId, String title, List<MultipartFile> files) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título da coleção é obrigatório.");
        }

        String trimmedTitle = title.trim();
        if (trimmedTitle.length() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título deve ter no máximo 30 caracteres.");
        }

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }

        List<CloudinaryUploadResponse> uploadResponses = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            String folder = "travel-app/collections/" + userId;
            try {
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) {
                        continue;
                    }
                    uploadResponses.add(cloudinaryService.upload(file, folder));
                }
            } catch (Exception ex) {
                for (CloudinaryUploadResponse resp : uploadResponses) {
                    cloudinaryService.delete(resp.publicId());
                }
                throw ex;
            }
        }

        Collection saved;
        try {
            saved = transactionTemplate.execute(status -> {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

                Collection collection = Collection.builder()
                        .user(user)
                        .title(trimmedTitle)
                        .photos(new ArrayList<>())
                        .build();

                int order = 0;
                for (CloudinaryUploadResponse uploadResponse : uploadResponses) {
                    CollectionPhoto photo = CollectionPhoto.builder()
                            .collection(collection)
                            .url(uploadResponse.url())
                            .publicId(uploadResponse.publicId())
                            .orderIndex(order++)
                            .build();
                    collection.getPhotos().add(photo);
                }

                return collectionRepository.save(collection);
            });
        } catch (Exception ex) {
            for (CloudinaryUploadResponse resp : uploadResponses) {
                cloudinaryService.delete(resp.publicId());
            }
            throw ex;
        }

        return CollectionResponseDTO.fromEntity(saved);
    }

    public CollectionResponseDTO updateCollection(UUID id, UUID userId, UpdateCollectionRequestDTO dto) {
        Collection collection = collectionRepository.findByIdAndUserIdWithPhotos(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));

        List<CollectionPhoto> photosToDelete = new ArrayList<>();
        if (dto.deletePhotoIds() != null && !dto.deletePhotoIds().isEmpty() && collection.getPhotos() != null) {
            photosToDelete = collection.getPhotos().stream()
                    .filter(p -> dto.deletePhotoIds().contains(p.getId()))
                    .toList();
        }

        List<String> publicIdsToDelete = photosToDelete.stream()
                .map(CollectionPhoto::getPublicId)
                .filter(Objects::nonNull)
                .toList();

        final List<CollectionPhoto> finalPhotosToDelete = photosToDelete;
        Collection updated = transactionTemplate.execute(status -> {
            Collection c = collectionRepository.findByIdAndUserIdWithPhotos(id, userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));

            if (dto.title() != null && !dto.title().isBlank()) {
                c.setTitle(dto.title().trim());
            }

            if (!finalPhotosToDelete.isEmpty() && c.getPhotos() != null) {
                c.getPhotos().removeIf(p -> dto.deletePhotoIds().contains(p.getId()));
            }

            return collectionRepository.save(c);
        });

        for (String pubId : publicIdsToDelete) {
            cloudinaryService.delete(pubId);
        }

        return CollectionResponseDTO.fromEntity(updated);
    }

    public void deleteCollection(UUID id, UUID userId) {
        Collection collection = collectionRepository.findByIdAndUserIdWithPhotos(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));

        List<String> publicIdsToDelete = new ArrayList<>();
        if (collection.getPhotos() != null) {
            for (CollectionPhoto photo : collection.getPhotos()) {
                if (photo.getPublicId() != null && !photo.getPublicId().isBlank()) {
                    publicIdsToDelete.add(photo.getPublicId());
                }
            }
        }

        transactionTemplate.executeWithoutResult(status -> {
            Collection c = collectionRepository.findByIdAndUserIdWithPhotos(id, userId).orElse(null);
            if (c != null) {
                collectionRepository.delete(c);
            }
        });

        for (String pubId : publicIdsToDelete) {
            cloudinaryService.delete(pubId);
        }
    }

    public CollectionResponseDTO addPhotos(UUID id, UUID userId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma foto informada para envio.");
        }

        Collection collection = collectionRepository.findByIdAndUserIdWithPhotos(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));

        String folder = "travel-app/collections/" + userId;
        List<CloudinaryUploadResponse> uploadResponses = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                uploadResponses.add(cloudinaryService.upload(file, folder));
            }
        } catch (Exception ex) {
            for (CloudinaryUploadResponse resp : uploadResponses) {
                cloudinaryService.delete(resp.publicId());
            }
            throw ex;
        }

        Collection updated;
        try {
            updated = transactionTemplate.execute(status -> {
                Collection c = collectionRepository.findByIdAndUserIdWithPhotos(id, userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));

                int nextOrder = c.getPhotos() == null ? 0 : c.getPhotos().size();
                for (CloudinaryUploadResponse uploadResponse : uploadResponses) {
                    CollectionPhoto photo = CollectionPhoto.builder()
                            .collection(c)
                            .url(uploadResponse.url())
                            .publicId(uploadResponse.publicId())
                            .orderIndex(nextOrder++)
                            .build();
                    c.getPhotos().add(photo);
                }

                return collectionRepository.save(c);
            });
        } catch (Exception ex) {
            for (CloudinaryUploadResponse resp : uploadResponses) {
                cloudinaryService.delete(resp.publicId());
            }
            throw ex;
        }

        return CollectionResponseDTO.fromEntity(updated);
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

    public void deletePhoto(UUID collectionId, UUID photoId, UUID userId) {
        if (!collectionRepository.existsByIdAndUserId(collectionId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada.");
        }

        CollectionPhoto photo = collectionPhotoRepository.findByIdAndCollectionId(photoId, collectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto não encontrada na coleção."));

        String publicIdToDelete = photo.getPublicId();

        transactionTemplate.executeWithoutResult(status -> {
            Collection collection = collectionRepository.findByIdAndUserIdWithPhotos(collectionId, userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleção não encontrada."));

            CollectionPhoto p = collectionPhotoRepository.findByIdAndCollectionId(photoId, collectionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto não encontrada na coleção."));

            collection.getPhotos().remove(p);
            collectionPhotoRepository.delete(p);
        });

        if (publicIdToDelete != null && !publicIdToDelete.isBlank()) {
            cloudinaryService.delete(publicIdToDelete);
        }
    }
}
