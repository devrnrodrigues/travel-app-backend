package com.devrenanrodrigues.travelapi.collection;

import com.devrenanrodrigues.travelapi.collection.dto.CollectionPhotoResponseDTO;
import com.devrenanrodrigues.travelapi.collection.dto.CollectionResponseDTO;
import com.devrenanrodrigues.travelapi.collection.dto.UpdateCollectionRequestDTO;
import com.devrenanrodrigues.travelapi.collection.dto.UpdatePhotoCaptionDTO;
import com.devrenanrodrigues.travelapi.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping
    public ResponseEntity<List<CollectionResponseDTO>> getCollections(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = SecurityUtils.getUserId(jwt);
        List<CollectionResponseDTO> response = collectionService.getCollections(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponseDTO> getCollectionById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        CollectionResponseDTO response = collectionService.getCollectionById(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CollectionResponseDTO> createCollection(
            @RequestParam("title") String title,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        CollectionResponseDTO response = collectionService.createCollection(userId, title, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionResponseDTO> updateCollection(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCollectionRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        CollectionResponseDTO response = collectionService.updateCollection(id, userId, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        collectionService.deleteCollection(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CollectionResponseDTO> addPhotos(
            @PathVariable UUID id,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        CollectionResponseDTO response = collectionService.addPhotos(id, userId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/photos/{photoId}")
    public ResponseEntity<CollectionPhotoResponseDTO> updatePhotoCaption(
            @PathVariable UUID id,
            @PathVariable UUID photoId,
            @Valid @RequestBody UpdatePhotoCaptionDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        CollectionPhotoResponseDTO response = collectionService.updatePhotoCaption(id, photoId, userId, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable UUID id,
            @PathVariable UUID photoId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = SecurityUtils.getUserId(jwt);
        collectionService.deletePhoto(id, photoId, userId);
        return ResponseEntity.noContent().build();
    }
}
