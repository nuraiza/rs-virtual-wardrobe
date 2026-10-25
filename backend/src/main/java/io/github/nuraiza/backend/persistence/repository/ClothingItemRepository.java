package io.github.nuraiza.backend.persistence.repository;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ClothingItemRepository extends JpaRepository<ClothingItemEntity, Long>, JpaSpecificationExecutor<ClothingItemEntity> {

    Optional<ClothingItemEntity> findByUserIdAndId(Long userId, Long id);

    List<ClothingItemEntity> findAllByCategoriesId(Long categoryId);
}
