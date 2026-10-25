package io.github.nuraiza.backend.service;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.CollectionEntity;
import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.persistence.repository.ClothingItemRepository;
import io.github.nuraiza.backend.persistence.repository.CollectionRepository;
import io.github.nuraiza.backend.persistence.specification.ClothingItemSpecifications;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long COLLECTION_ID = 10L;
    private static final Long ITEM_ID = 100L;
    private static final Pageable PAGEABLE = Pageable.ofSize(5);
    private static final List<String> CATEGORIES = new ArrayList<>();
    private Page mockPage;
    private Specification<ClothingItemEntity> spec;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private ClothingItemRepository clothingItemRepository;

    @Mock
    private ClothingItemService clothingItemService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CollectionService collectionService;

    private UserEntity user;
    private CollectionEntity collection;
    private ClothingItemEntity item;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(USER_ID);
        user.setName("Antony");

        collection = new CollectionEntity("Winter collection", "The weather is cold", user);
        collection.setId(COLLECTION_ID);
        user.addCollection(collection);

        item = new ClothingItemEntity();
        item.setId(ITEM_ID);
        item.setName("Black jacket");
        user.addClothingItem(item);
    }

    private void collectionExists() {
        when(collectionRepository.findByUserIdAndId(USER_ID, COLLECTION_ID))
                .thenReturn(Optional.of(collection));
    }

    private void collectionDoesNotExist() {
        when(collectionRepository.findByUserIdAndId(USER_ID, COLLECTION_ID))
                .thenReturn(Optional.empty());
    }

    @Nested
    class CreateCollection {

        @Test
        void shouldSaveCollectionAndLinkItToUser() {
            UserEntity freshUser = new UserEntity();
            freshUser.setId(USER_ID);
            when(userService.getUser(USER_ID)).thenReturn(freshUser);
            when(collectionRepository.save(any(CollectionEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            CollectionEntity result = collectionService.createCollection(USER_ID, "Summer", "Light clothes");

            ArgumentCaptor<CollectionEntity> captor = ArgumentCaptor.forClass(CollectionEntity.class);
            verify(collectionRepository).save(captor.capture());
            CollectionEntity saved = captor.getValue();

            assertThat(saved.getName()).isEqualTo("Summer");
            assertThat(saved.getDescription()).isEqualTo("Light clothes");
            assertThat(saved.getUser()).isSameAs(freshUser);
            assertThat(freshUser.getCollections()).contains(saved);
            assertThat(result).isSameAs(saved);
        }

        @Test
        void shouldThrowAndNotSaveWhenUserDoesNotExist() {
            when(userService.getUser(USER_ID))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + USER_ID));

            assertThatThrownBy(() -> collectionService.createCollection(USER_ID, "Summer", "Light clothes"))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(collectionRepository, never()).save(any());
        }
    }

    @Nested
    class GetCollection {

        @Test
        void shouldReturnCollectionWhenItBelongsToUser() {
            collectionExists();

            CollectionEntity result = collectionService.getCollection(USER_ID, COLLECTION_ID);

            assertThat(result).isSameAs(collection);
        }

        @Test
        void shouldThrowWhenCollectionNotFoundOrBelongsToAnotherUser() {
            collectionDoesNotExist();

            assertThatThrownBy(() -> collectionService.getCollection(USER_ID, COLLECTION_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(COLLECTION_ID.toString());
        }
    }

    @Nested
    class GetAllCollections {

        @Test
        void shouldReturnAllCollectionsOfUser() {
            when(userService.getUser(USER_ID)).thenReturn(user);
            when(collectionRepository.findAllByUserId(USER_ID)).thenReturn(List.of(collection));

            List<CollectionEntity> result = collectionService.getAllCollections(USER_ID);

            assertThat(result).containsExactly(collection);
        }

        @Test
        void shouldThrowAndNotQueryCollectionsWhenUserDoesNotExist() {
            when(userService.getUser(USER_ID))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + USER_ID));

            assertThatThrownBy(() -> collectionService.getAllCollections(USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(collectionRepository, never()).findAllByUserId(anyLong());
        }
    }

    @Nested
    class UpdateCollection {

        @Test
        void shouldUpdateNameAndDescription() {
            collectionExists();

            CollectionEntity result = collectionService.updateCollection(USER_ID, COLLECTION_ID, "New name", "New description");

            assertThat(result).isSameAs(collection);
            assertThat(result.getName()).isEqualTo("New name");
            assertThat(result.getDescription()).isEqualTo("New description");
        }

        @Test
        void shouldThrowWhenCollectionNotFound() {
            collectionDoesNotExist();

            assertThatThrownBy(() -> collectionService.updateCollection(USER_ID, COLLECTION_ID, "New name", "New description"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class DeleteCollection {

        @Test
        void shouldRemoveCollectionFromUser() {
            collectionExists();

            collectionService.deleteCollection(USER_ID, COLLECTION_ID);

            assertThat(user.getCollections()).doesNotContain(collection);
        }

        @Test
        void shouldThrowWhenCollectionNotFound() {
            collectionDoesNotExist();

            assertThatThrownBy(() -> collectionService.deleteCollection(USER_ID, COLLECTION_ID))
                    .isInstanceOf(EntityNotFoundException.class);

            assertThat(user.getCollections()).contains(collection);
        }
    }

    @Nested
    class AddClothingItemToCollection {

        @Test
        void shouldAddItemToCollection() {
            collectionExists();
            when(clothingItemService.getClothingItem(USER_ID, ITEM_ID)).thenReturn(item);

            collectionService.addClothingItemToCollection(USER_ID, COLLECTION_ID, ITEM_ID);

            assertThat(collection.getClothes()).contains(item);
        }

        @Test
        void shouldNotDuplicateItemWhenAddedTwice() {
            collectionExists();
            when(clothingItemService.getClothingItem(USER_ID, ITEM_ID)).thenReturn(item);

            collectionService.addClothingItemToCollection(USER_ID, COLLECTION_ID, ITEM_ID);
            collectionService.addClothingItemToCollection(USER_ID, COLLECTION_ID, ITEM_ID);

            assertThat(collection.getClothes()).containsOnlyOnce(item);
        }

        @Test
        void shouldThrowWhenCollectionNotFound() {
            collectionDoesNotExist();

            assertThatThrownBy(() -> collectionService.addClothingItemToCollection(USER_ID, COLLECTION_ID, ITEM_ID))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(clothingItemService, never()).getClothingItem(anyLong(), anyLong());
        }

        @Test
        void shouldThrowAndLeaveCollectionUnchangedWhenItemNotFoundOrBelongsToAnotherUser() {
            collectionExists();
            when(clothingItemService.getClothingItem(USER_ID, ITEM_ID))
                    .thenThrow(new EntityNotFoundException("Clothing item not found with id: " + ITEM_ID));

            assertThatThrownBy(() -> collectionService.addClothingItemToCollection(USER_ID, COLLECTION_ID, ITEM_ID))
                    .isInstanceOf(EntityNotFoundException.class);

            assertThat(collection.getClothes()).isEmpty();
        }
    }

    @Nested
    class DeleteClothingItemFromCollection {

        @Test
        void shouldRemoveItemFromCollection() {
            collection.addClothingItem(item);
            collectionExists();
            when(clothingItemService.getClothingItem(USER_ID, ITEM_ID)).thenReturn(item);

            collectionService.deleteClothingItemFromCollection(USER_ID, COLLECTION_ID, ITEM_ID);

            // removed from the collection (both sides of the relationship)
            assertThat(collection.getClothes()).doesNotContain(item);
            assertThat(item.getCollections()).doesNotContain(collection);

            // but still owned by the user and not deleted
            assertThat(item.getUser()).isSameAs(user);
            assertThat(user.getClothes()).contains(item);
            verify(clothingItemRepository, never()).delete((ClothingItemEntity) any());
            verify(clothingItemRepository, never()).deleteById(anyLong());
        }

        @Test
        void shouldDoNothingWhenItemIsNotInCollection() {
            collectionExists();
            when(clothingItemService.getClothingItem(USER_ID, ITEM_ID)).thenReturn(item);

            collectionService.deleteClothingItemFromCollection(USER_ID, COLLECTION_ID, ITEM_ID);

            assertThat(collection.getClothes()).isEmpty();
        }

        @Test
        void shouldThrowWhenCollectionNotFound() {
            collectionDoesNotExist();

            assertThatThrownBy(() -> collectionService.deleteClothingItemFromCollection(USER_ID, COLLECTION_ID, ITEM_ID))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(clothingItemService, never()).getClothingItem(anyLong(), anyLong());
        }

        @Test
        void shouldThrowWhenItemNotFound() {
            collection.addClothingItem(item);
            collectionExists();
            when(clothingItemService.getClothingItem(USER_ID, ITEM_ID))
                    .thenThrow(new EntityNotFoundException("Clothing item not found with id: " + ITEM_ID));

            assertThatThrownBy(() -> collectionService.deleteClothingItemFromCollection(USER_ID, COLLECTION_ID, ITEM_ID))
                    .isInstanceOf(EntityNotFoundException.class);

            assertThat(collection.getClothes()).contains(item);
        }
    }

    @Nested
    class GetAllClothingItemsInCollection {

        @Test
        void shouldReturnItemsInCollection() {
            collectionExists();
            spec = Specification.where(ClothingItemSpecifications.belongsToUser(USER_ID))
                    .and(ClothingItemSpecifications.hasCategories(CATEGORIES));
            when(clothingItemRepository.findAll(spec, PAGEABLE))
                    .thenReturn(mockPage);

            Page<ClothingItemEntity> result = collectionService.getAllClothingItemsInCollection(USER_ID, COLLECTION_ID, PAGEABLE, CATEGORIES);

            assertThat(result).containsExactly(item);
        }

        @Test
        void shouldThrowAndNotQueryItemsWhenCollectionNotFound() {
            collectionDoesNotExist();

            assertThatThrownBy(() -> collectionService.getAllClothingItemsInCollection(USER_ID, COLLECTION_ID, PAGEABLE, CATEGORIES))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(clothingItemRepository, never()).findAll();
        }
    }
}