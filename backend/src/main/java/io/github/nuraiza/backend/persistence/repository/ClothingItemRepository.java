package io.github.nuraiza.backend.persistence.repository;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClothingItemRepository extends JpaRepository<ClothingItemEntity, Long> {

    Optional<ClothingItemEntity> findByUserIdAndId(Long userId, Long id);

    List<ClothingItemEntity> findAllByUserId(Long userId);

    List<ClothingItemEntity> findAllByUserIdAndCollectionsId(Long userId, Long collectionId);
}
