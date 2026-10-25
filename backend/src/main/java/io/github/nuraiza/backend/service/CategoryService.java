package io.github.nuraiza.backend.service;

import io.github.nuraiza.backend.persistence.entity.CategoryEntity;
import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.persistence.repository.CategoryRepository;
import io.github.nuraiza.backend.persistence.repository.ClothingItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final UserService userService;

    private final ClothingItemRepository clothingItemRepository;

    @Transactional
    public CategoryEntity createCategory(Long userId, String name) {
        UserEntity user = userService.getUser(userId);

        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists.");
        }

        CategoryEntity category = new CategoryEntity();
        category.setName(name.trim());
        category.setUser(user);

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryEntity> getUserCategories(Long userId) {
        userService.getUser(userId);
        return categoryRepository.findAllByUserId(userId);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        CategoryEntity category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        List<ClothingItemEntity> linkedItems = clothingItemRepository.findAllByCategoriesId(categoryId);

        for (ClothingItemEntity item : linkedItems) {
            item.getCategories().remove(category);
        }

        categoryRepository.delete(category);
    }
}