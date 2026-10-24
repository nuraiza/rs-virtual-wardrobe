package io.github.nuraiza.backend.service;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.CollectionEntity;
import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.persistence.repository.ClothingItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClothingItemService {

    private final ClothingItemRepository clothingItemRepository;

    private final UserService userService;

    @Transactional
    public ClothingItemEntity createClothingItem(Long userId, String name, String description) {
        UserEntity userEntity = userService.getUser(userId);

        ClothingItemEntity clothingItem = new ClothingItemEntity(name, description, userEntity);
        userEntity.addClothingItem(clothingItem);
        return clothingItemRepository.save(clothingItem);
    }

    @Transactional(readOnly = true)
    public ClothingItemEntity getClothingItem(Long userId, Long clothingItemId) {

        return clothingItemRepository.findByUserIdAndId(userId, clothingItemId).orElseThrow(() ->
                new EntityNotFoundException("Clothing item not found with id: " + clothingItemId));
    }

    @Transactional(readOnly = true)
    public List<ClothingItemEntity> getAllClothingItems(Long userId){
        userService.getUser(userId);

        return clothingItemRepository.findAllByUserId(userId);
    }

    @Transactional
    public ClothingItemEntity updateClothingItem(Long userId, Long clothingItemId, String name, String description) {
        ClothingItemEntity clothingItem = getClothingItem(userId, clothingItemId);

        clothingItem.setName(name);
        clothingItem.setDescription(description);
        return clothingItem;
    }

    @Transactional
    public void deleteClothingItem(Long userId, Long clothingItemId) {
        ClothingItemEntity clothingItem = getClothingItem(userId, clothingItemId);

        for (CollectionEntity collection : Set.copyOf(clothingItem.getCollections())) {
            collection.removeClothingItem(clothingItem);
        }

        clothingItem.getUser().removeClothingItem(clothingItem);
    }
}
