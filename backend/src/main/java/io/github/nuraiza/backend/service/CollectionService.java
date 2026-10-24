package io.github.nuraiza.backend.service;


import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.CollectionEntity;
import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.persistence.repository.ClothingItemRepository;
import io.github.nuraiza.backend.persistence.repository.CollectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
    public List<ClothingItemEntity> getAllClothingItemsInCollection(Long userId, Long collectionId){
        CollectionEntity collectionEntity = getCollection(userId, collectionId);

        return clothingItemRepository.findAllByUserIdAndCollectionsId(userId, collectionId);
    }
}
