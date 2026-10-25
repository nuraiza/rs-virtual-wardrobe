package io.github.nuraiza.backend.service;

import io.github.nuraiza.backend.persistence.entity.CategoryEntity;
import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.CollectionEntity;
import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.persistence.repository.CategoryRepository;
import io.github.nuraiza.backend.persistence.repository.ClothingItemRepository;
import io.github.nuraiza.backend.persistence.specification.ClothingItemSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClothingItemService {

    private final ClothingItemRepository clothingItemRepository;

    private final UserService userService;

    private final CategoryRepository categoryRepository;

    @Transactional
    public ClothingItemEntity createClothingItem(Long userId, String name, String description, Set<Long> categoryIds) {
        UserEntity userEntity = userService.getUser(userId);

        ClothingItemEntity clothingItem = new ClothingItemEntity(name, description, userEntity);
        userEntity.addClothingItem(clothingItem);

        Set<CategoryEntity> categoryEntities = validateCategories(userId, categoryIds);

        if (categoryEntities != null) clothingItem.setCategories(categoryEntities);

        return clothingItemRepository.save(clothingItem);
    }

    @Transactional(readOnly = true)
    public ClothingItemEntity getClothingItem(Long userId, Long clothingItemId) {

        return clothingItemRepository.findByUserIdAndId(userId, clothingItemId).orElseThrow(() ->
                new EntityNotFoundException("Clothing item not found with id: " + clothingItemId));
    }

    @Transactional(readOnly = true)
    public Page<ClothingItemEntity> getAllClothingItems(Long userId, Pageable pageable, List<String> categories) {
        userService.getUser(userId);
        Specification<ClothingItemEntity> spec = Specification.where(ClothingItemSpecifications.belongsToUser(userId))
                .and(ClothingItemSpecifications.hasCategories(categories));

        return clothingItemRepository.findAll(spec, pageable);
    }

    @Transactional
    public ClothingItemEntity updateClothingItem(Long userId, Long clothingItemId, String name, String description, Set<Long> categoryIds) {
        ClothingItemEntity clothingItem = getClothingItem(userId, clothingItemId);
        clothingItem.setName(name);
        clothingItem.setDescription(description);

        Set<CategoryEntity> categoryEntities = validateCategories(userId, categoryIds);

        if (categoryEntities != null) clothingItem.setCategories(categoryEntities);

        return clothingItem;
    }

    @Transactional
    public void deleteClothingItem(Long userId, Long clothingItemId) {
        ClothingItemEntity clothingItem = getClothingItem(userId, clothingItemId);

        for (CollectionEntity collection : Set.copyOf(clothingItem.getCollections())) {
            collection.removeClothingItem(clothingItem);
        }

        clothingItem.getUser().removeClothingItem(clothingItem);
        clothingItemRepository.delete(clothingItem);
    }

    private Set<CategoryEntity> validateCategories(Long userId, Set<Long> categoryIds) {
        if (categoryIds == null) return null;

        if (categoryIds.isEmpty()) return new HashSet<>();

        List<CategoryEntity> categoryEntities = categoryRepository.findAllById(categoryIds);

        for (CategoryEntity category : categoryEntities) {
            if (!category.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Category with id " + category.getId() + " does not belong to user.");
            }
        }
        return new HashSet<>(categoryEntities);
    }
}
