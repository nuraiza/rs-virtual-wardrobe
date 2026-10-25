package io.github.nuraiza.backend.service;

import io.github.nuraiza.backend.persistence.entity.CategoryEntity;
import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.CollectionEntity;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClothingItemServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long ITEM_ID = 100L;
    private static final Long CATEGORY_ID = 10L;
    private static final Pageable PAGEABLE = Pageable.ofSize(5);
    private static final List<String> CATEGORIES = new ArrayList<>();

    private Page mockPage;
    private Specification<ClothingItemEntity> spec;

    @Mock
    private ClothingItemRepository clothingItemRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ClothingItemService clothingItemService;

    private UserEntity user;
    private ClothingItemEntity item;
    private CategoryEntity validCategory;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(USER_ID);
        user.setName("Antony");

        item = new ClothingItemEntity("Black jacket", "Warm winter jacket", user);
        item.setId(ITEM_ID);
        user.addClothingItem(item);
        mockPage = new PageImpl(List.of(item));

        validCategory = new CategoryEntity();
        validCategory.setId(CATEGORY_ID);
        validCategory.setName("Office");
        validCategory.setUser(user);
    }

    private void itemExists() {
        when(clothingItemRepository.findByUserIdAndId(USER_ID, ITEM_ID))
                .thenReturn(Optional.of(item));
    }

    private void itemDoesNotExist() {
        when(clothingItemRepository.findByUserIdAndId(USER_ID, ITEM_ID))
                .thenReturn(Optional.empty());
    }

    private CollectionEntity collectionContainingItem(Long id, String name) {
        CollectionEntity collection = new CollectionEntity(name, null, user);
        collection.setId(id);
        user.addCollection(collection);
        collection.addClothingItem(item);
        return collection;
    }

    @Nested
    class CreateClothingItem {

        @Test
        void shouldSaveItemAndLinkItToUser() {
            UserEntity freshUser = new UserEntity();
            freshUser.setId(USER_ID);
            when(userService.getUser(USER_ID)).thenReturn(freshUser);
            when(clothingItemRepository.save(any(ClothingItemEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ClothingItemEntity result = clothingItemService.createClothingItem(USER_ID, "White shirt", "Cotton", Set.of());

            ArgumentCaptor<ClothingItemEntity> captor = ArgumentCaptor.forClass(ClothingItemEntity.class);
            verify(clothingItemRepository).save(captor.capture());
            ClothingItemEntity saved = captor.getValue();

            assertThat(saved.getName()).isEqualTo("White shirt");
            assertThat(saved.getDescription()).isEqualTo("Cotton");
            assertThat(saved.getUser()).isSameAs(freshUser);
            assertThat(freshUser.getClothes()).contains(saved);
            assertThat(result).isSameAs(saved);
        }

        @Test
        void shouldSaveItemWithCategoriesWhenValid() {
            UserEntity freshUser = new UserEntity();
            freshUser.setId(USER_ID);
            when(userService.getUser(USER_ID)).thenReturn(freshUser);
            when(categoryRepository.findAllById(Set.of(CATEGORY_ID))).thenReturn(List.of(validCategory));
            when(clothingItemRepository.save(any(ClothingItemEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ClothingItemEntity result = clothingItemService.createClothingItem(USER_ID, "White shirt", "Cotton", Set.of(CATEGORY_ID));

            assertThat(result.getCategories()).contains(validCategory);
            verify(categoryRepository).findAllById(Set.of(CATEGORY_ID));
        }

        @Test
        void shouldThrowWhenCategoryDoesNotBelongToUser() {
            UserEntity otherUser = new UserEntity();
            otherUser.setId(OTHER_USER_ID);

            CategoryEntity foreignCategory = new CategoryEntity();
            foreignCategory.setId(CATEGORY_ID);
            foreignCategory.setUser(otherUser);

            when(userService.getUser(USER_ID)).thenReturn(user);
            when(categoryRepository.findAllById(Set.of(CATEGORY_ID))).thenReturn(List.of(foreignCategory));

            assertThatThrownBy(() -> clothingItemService.createClothingItem(USER_ID, "White shirt", "Cotton", Set.of(CATEGORY_ID)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong to user");

            verify(clothingItemRepository, never()).save(any());
        }

        @Test
        void shouldThrowAndNotSaveWhenUserDoesNotExist() {
            when(userService.getUser(USER_ID))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + USER_ID));

            assertThatThrownBy(() -> clothingItemService.createClothingItem(USER_ID, "White shirt", "Cotton", Set.of()))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(clothingItemRepository, never()).save(any());
        }
    }

    @Nested
    class GetClothingItem {

        @Test
        void shouldReturnItemWhenItBelongsToUser() {
            itemExists();

            ClothingItemEntity result = clothingItemService.getClothingItem(USER_ID, ITEM_ID);

            assertThat(result).isSameAs(item);
        }

        @Test
        void shouldThrowWhenItemNotFoundOrBelongsToAnotherUser() {
            itemDoesNotExist();

            assertThatThrownBy(() -> clothingItemService.getClothingItem(USER_ID, ITEM_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Clothing item not found with id: " + ITEM_ID);
        }
    }

    @Nested
    class GetAllClothingItems {

        @Test
        void shouldReturnAllItemsOfUser() {

            when(userService.getUser(USER_ID)).thenReturn(user);
            when(clothingItemRepository.findAll(any(Specification.class), eq(PAGEABLE))).thenReturn(mockPage);

            Page<ClothingItemEntity> result = clothingItemService.getAllClothingItems(USER_ID, PAGEABLE, CATEGORIES);

            assertThat(result).containsExactly(item);
        }

        @Test
        void shouldThrowAndNotQueryItemsWhenUserDoesNotExist() {
            when(userService.getUser(USER_ID))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + USER_ID));

            assertThatThrownBy(() -> clothingItemService.getAllClothingItems(USER_ID, PAGEABLE, CATEGORIES))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(clothingItemRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Nested
    class UpdateClothingItem {

        @Test
        void shouldUpdateNameDescriptionAndCategories() {
            itemExists();
            when(categoryRepository.findAllById(Set.of(CATEGORY_ID))).thenReturn(List.of(validCategory));

            ClothingItemEntity result = clothingItemService.updateClothingItem(USER_ID, ITEM_ID, "Grey jacket", "Light", Set.of(CATEGORY_ID));

            assertThat(result).isSameAs(item);
            assertThat(result.getName()).isEqualTo("Grey jacket");
            assertThat(result.getDescription()).isEqualTo("Light");
            assertThat(result.getCategories()).contains(validCategory);
            assertThat(result.getUser()).isSameAs(user);
        }

        @Test
        void shouldThrowWhenCategoryDoesNotBelongToUserDuringUpdate() {
            itemExists();

            UserEntity otherUser = new UserEntity();
            otherUser.setId(OTHER_USER_ID);

            CategoryEntity foreignCategory = new CategoryEntity();
            foreignCategory.setId(CATEGORY_ID);
            foreignCategory.setUser(otherUser);

            when(categoryRepository.findAllById(Set.of(CATEGORY_ID))).thenReturn(List.of(foreignCategory));

            assertThatThrownBy(() -> clothingItemService.updateClothingItem(USER_ID, ITEM_ID, "Grey jacket", "Light", Set.of(CATEGORY_ID)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong to user");
        }

        @Test
        void shouldThrowWhenItemNotFound() {
            itemDoesNotExist();

            assertThatThrownBy(() -> clothingItemService.updateClothingItem(USER_ID, ITEM_ID, "Grey jacket", "Light", Set.of()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class DeleteClothingItem {

        @Test
        void shouldRemoveItemFromUser() {
            itemExists();

            clothingItemService.deleteClothingItem(USER_ID, ITEM_ID);

            verify(clothingItemRepository).delete(item);
            assertThat(user.getClothes()).doesNotContain(item);
        }

        @Test
        void shouldRemoveItemFromAllCollectionsButKeepCollections() {
            CollectionEntity winter = collectionContainingItem(10L, "Winter");
            CollectionEntity work = collectionContainingItem(11L, "Work");
            itemExists();

            clothingItemService.deleteClothingItem(USER_ID, ITEM_ID);

            // item is unlinked from every collection, on both sides
            assertThat(winter.getClothes()).doesNotContain(item);
            assertThat(work.getClothes()).doesNotContain(item);
            assertThat(item.getCollections()).isEmpty();

            // the collections themselves are untouched
            assertThat(user.getCollections()).contains(winter, work);
            assertThat(user.getClothes()).doesNotContain(item);
            verify(clothingItemRepository).delete(item);
        }

        @Test
        void shouldThrowAndChangeNothingWhenItemNotFound() {
            CollectionEntity winter = collectionContainingItem(10L, "Winter");
            itemDoesNotExist();

            assertThatThrownBy(() -> clothingItemService.deleteClothingItem(USER_ID, ITEM_ID))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(clothingItemRepository, never()).delete(item);
            assertThat(user.getClothes()).contains(item);
            assertThat(winter.getClothes()).contains(item);
        }
    }
}