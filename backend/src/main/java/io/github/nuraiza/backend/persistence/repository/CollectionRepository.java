package io.github.nuraiza.backend.persistence.repository;

import io.github.nuraiza.backend.persistence.entity.CollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<CollectionEntity, Long> {

    Optional<CollectionEntity> findByUserIdAndId(Long userId, Long id);

    List<CollectionEntity> findAllByUserId(Long userId);
}
