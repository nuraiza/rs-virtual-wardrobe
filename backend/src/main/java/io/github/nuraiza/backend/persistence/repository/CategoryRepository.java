package io.github.nuraiza.backend.persistence.repository;

import io.github.nuraiza.backend.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findAllByUserId(Long userId);
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
    Optional<CategoryEntity> findByIdAndUserId(Long id, Long userId);
}
