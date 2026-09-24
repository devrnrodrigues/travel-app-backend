package com.devrenanrodrigues.travelapi.collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionPhotoRepository extends JpaRepository<CollectionPhoto, UUID> {

    Optional<CollectionPhoto> findByIdAndCollectionId(UUID id, UUID collectionId);

    Optional<CollectionPhoto> findByIdAndCollectionUserId(UUID id, UUID userId);

    List<CollectionPhoto> findByCollectionIdOrderByOrderIndexAscCreatedAtAsc(UUID collectionId);

    int countByCollectionId(UUID collectionId);
}
