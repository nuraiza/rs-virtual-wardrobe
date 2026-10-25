package io.github.nuraiza.backend.service;


import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.CollectionEntity;
import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.persistence.repository.ClothingItemRepository;
import io.github.nuraiza.backend.persistence.repository.CollectionRepository;
import io.github.nuraiza.backend.persistence.specification.ClothingItemSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;

    private final ClothingItemRepository clothingItemRepository;

    private final ClothingItemService clothingItemService;

    private final UserService userService;

    @Transactional
    public CollectionEntity createCollection(Long userId, String name, String description) {
        UserEntity userEntity = userService.getUser(userId);

        CollectionEntity collectionEntity = new CollectionEntity(name, description, userEntity);
        userEntity.addCollection(collectionEntity);
        return collectionRepository.save(collectionEntity);
    }

    @Transactional(readOnly = true)
    public CollectionEntity getCollection(Long userId, Long collectionId) {
        return collectionRepository.findByUserIdAndId(userId, collectionId).orElseThrow(() -> new EntityNotFoundException("Collection not found with id: " + collectionId));
    }

    @Transactional(readOnly = true)
    public List<CollectionEntity> getAllCollections(Long userId){
        userService.getUser(userId);
        return collectionRepository.findAllByUserId(userId);
    }

    @Transactional
    public CollectionEntity updateCollection(Long userId, Long collectionId, String name, String description) {
        CollectionEntity collectionEntity = getCollection(userId, collectionId);

        collectionEntity.setName(name);
        collectionEntity.setDescription(description);
        return collectionEntity;
    }

    @Transactional
    public void deleteCollection(Long userId, Long collectionId) {
        CollectionEntity collectionEntity = getCollection(userId, collectionId);

        collectionEntity.getUser().removeCollection(collectionEntity);
        collectionRepository.delete(collectionEntity);
    }

    @Transactional
    public void addClothingItemToCollection(Long userId, Long collectionId, Long clothingItemId) {
        CollectionEntity collectionEntity = getCollection(userId, collectionId);
        ClothingItemEntity clothingItem = clothingItemService.getClothingItem(userId, clothingItemId);

        collectionEntity.addClothingItem(clothingItem);
    }

    @Transactional
    public void deleteClothingItemFromCollection(Long userId, Long collectionId, Long clothingItemId) {
        CollectionEntity collectionEntity = getCollection(userId, collectionId);
        ClothingItemEntity clothingItem = clothingItemService.getClothingItem(userId, clothingItemId);

        collectionEntity.removeClothingItem(clothingItem);
    }

    @Transactional(readOnly = true)
    public Page<ClothingItemEntity> getAllClothingItemsInCollection(Long userId, Long collectionId, Pageable pageable, List<String> categories){
        getCollection(userId, collectionId);
        Specification<ClothingItemEntity> spec = Specification.where(ClothingItemSpecifications.belongsToUser(userId))
                .and(ClothingItemSpecifications.belongsToCollection(collectionId))
                .and(ClothingItemSpecifications.hasCategories(categories));

        return clothingItemRepository.findAll(spec, pageable);
    }
}
