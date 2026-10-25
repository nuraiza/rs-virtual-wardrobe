package io.github.nuraiza.backend.service;

import io.github.nuraiza.backend.persistence.entity.CategoryEntity;
import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.persistence.repository.CategoryRepository;
import io.github.nuraiza.backend.persistence.repository.ClothingItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long CATEGORY_ID = 10L;
    private static final String CATEGORY_NAME = "  Office  ";
    private static final String TRIMMED_NAME = "Office";

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @Mock
    private ClothingItemRepository clothingItemRepository;

    @InjectMocks
    private CategoryService categoryService;

    private UserEntity user;
    private CategoryEntity category;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(USER_ID);
        user.setName("Antony");

        category = new CategoryEntity();
        category.setId(CATEGORY_ID);
        category.setName(TRIMMED_NAME);
        category.setUser(user);
    }

    @Nested
    class CreateCategory {

        @Test
        void shouldCreateAndSaveCategorySuccessfully() {
            when(userService.getUser(USER_ID)).thenReturn(user);
            when(categoryRepository.existsByUserIdAndNameIgnoreCase(USER_ID, CATEGORY_NAME)).thenReturn(false);
            when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CategoryEntity result = categoryService.createCategory(USER_ID, CATEGORY_NAME);

            ArgumentCaptor<CategoryEntity> captor = ArgumentCaptor.forClass(CategoryEntity.class);
            verify(categoryRepository).save(captor.capture());
            CategoryEntity saved = captor.getValue();

            assertThat(saved.getName()).isEqualTo(TRIMMED_NAME);
            assertThat(saved.getUser()).isSameAs(user);
            assertThat(result).isSameAs(saved);
        }

        @Test
        void shouldThrowWhenCategoryNameAlreadyExistsForUser() {
            when(userService.getUser(USER_ID)).thenReturn(user);
            when(categoryRepository.existsByUserIdAndNameIgnoreCase(USER_ID, CATEGORY_NAME)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory(USER_ID, CATEGORY_NAME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");

            verify(categoryRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(userService.getUser(USER_ID))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + USER_ID));

            assertThatThrownBy(() -> categoryService.createCategory(USER_ID, CATEGORY_NAME))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(categoryRepository, never()).existsByUserIdAndNameIgnoreCase(any(), any());
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    class GetUserCategories {

        @Test
        void shouldReturnListOfCategoriesForUser() {
            when(userService.getUser(USER_ID)).thenReturn(user);
            when(categoryRepository.findAllByUserId(USER_ID)).thenReturn(List.of(category));

            List<CategoryEntity> result = categoryService.getUserCategories(USER_ID);

            assertThat(result).containsExactly(category);
            verify(categoryRepository).findAllByUserId(USER_ID);
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(userService.getUser(USER_ID))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + USER_ID));

            assertThatThrownBy(() -> categoryService.getUserCategories(USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(categoryRepository, never()).findAllByUserId(any());
        }
    }

    @Nested
    class DeleteCategory {

        @Test
        void shouldDeleteCategoryAndUnlinkFromLinkedItems() {
            ClothingItemEntity item = new ClothingItemEntity("Shirt", "Cotton", user);
            Set<CategoryEntity> itemCategories = new HashSet<>();
            itemCategories.add(category);
            item.setCategories(itemCategories);

            when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(category));
            when(clothingItemRepository.findAllByCategoriesId(CATEGORY_ID)).thenReturn(List.of(item));

            categoryService.deleteCategory(USER_ID, CATEGORY_ID);

            assertThat(item.getCategories()).doesNotContain(category);
            verify(categoryRepository).delete(category);
        }

        @Test
        void shouldThrowWhenCategoryNotFoundOrDoesNotBelongToUser() {
            when(categoryRepository.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCategory(USER_ID, CATEGORY_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category not found with id: " + CATEGORY_ID);

            verify(clothingItemRepository, never()).findAllByCategoriesId(any());
            verify(categoryRepository, never()).delete(any());
        }
    }
}