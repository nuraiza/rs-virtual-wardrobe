package io.github.nuraiza.backend.controller;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.CollectionEntity;
import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.service.CollectionService;
import io.github.nuraiza.backend.web.controller.CollectionController;
import io.github.nuraiza.backend.web.mapper.ClothingItemMapper;
import io.github.nuraiza.backend.web.mapper.CollectionMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollectionController.class)
@Import({CollectionMapper.class, ClothingItemMapper.class})
class CollectionControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long COLLECTION_ID = 10L;
    private static final Long ITEM_ID = 100L;
    private static final String BASE_URL = "/api/users/{userId}/collections";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
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

        item = new ClothingItemEntity("Black jacket", "Warm winter jacket", user);
        item.setId(ITEM_ID);
    }

    private EntityNotFoundException collectionNotFound() {
        return new EntityNotFoundException("Collection not found with id: " + COLLECTION_ID);
    }

    @Nested
    class CreateCollection {

        @Test
        void shouldReturn201WithCreatedCollection() throws Exception {
            when(collectionService.createCollection(USER_ID, "Winter collection", "The weather is cold"))
                    .thenReturn(collection);

            mockMvc.perform(post(BASE_URL, USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "Winter collection", "description": "The weather is cold"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(COLLECTION_ID))
                    .andExpect(jsonPath("$.name").value("Winter collection"))
                    .andExpect(jsonPath("$.description").value("The weather is cold"))
                    .andExpect(jsonPath("$.userId").value(USER_ID));
        }

        @Test
        void shouldReturn400AndNotCallServiceWhenNameIsBlank() throws Exception {
            mockMvc.perform(post(BASE_URL, USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "", "description": "x"}
                                    """))
                    .andExpect(status().isBadRequest());

            verify(collectionService, never()).createCollection(anyLong(), anyString(), anyString());
        }

        @Test
        void shouldReturn400WhenJsonIsMalformed() throws Exception {
            mockMvc.perform(post(BASE_URL, USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": "))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(collectionService);
        }

        @Test
        void shouldReturn404WhenUserDoesNotExist() throws Exception {
            when(collectionService.createCollection(anyLong(), anyString(), anyString()))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + USER_ID));

            mockMvc.perform(post(BASE_URL, USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "Winter collection", "description": "x"}
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetCollection {

        @Test
        void shouldReturn200WithCollection() throws Exception {
            when(collectionService.getCollection(USER_ID, COLLECTION_ID)).thenReturn(collection);

            mockMvc.perform(get(BASE_URL + "/{collectionId}", USER_ID, COLLECTION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(COLLECTION_ID))
                    .andExpect(jsonPath("$.name").value("Winter collection"))
                    .andExpect(jsonPath("$.userId").value(USER_ID));
        }

        @Test
        void shouldReturn404ProblemDetailWhenCollectionNotFound() throws Exception {
            when(collectionService.getCollection(USER_ID, COLLECTION_ID)).thenThrow(collectionNotFound());

            mockMvc.perform(get(BASE_URL + "/{collectionId}", USER_ID, COLLECTION_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.detail").value(containsString(COLLECTION_ID.toString())));
        }

        @Test
        void shouldReturn400WhenIdIsNotANumber() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{collectionId}", USER_ID, "abc"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(collectionService);
        }
    }

    @Nested
    class GetCollections {

        @Test
        void shouldReturn200WithAllCollections() throws Exception {
            when(collectionService.getAllCollections(USER_ID)).thenReturn(List.of(collection));

            mockMvc.perform(get(BASE_URL, USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(COLLECTION_ID));
        }

        @Test
        void shouldReturn200WithEmptyListWhenUserHasNoCollections() throws Exception {
            when(collectionService.getAllCollections(USER_ID)).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL, USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void shouldReturn404WhenUserDoesNotExist() throws Exception {
            when(collectionService.getAllCollections(USER_ID))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + USER_ID));

            mockMvc.perform(get(BASE_URL, USER_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateCollection {

        @Test
        void shouldReturn200WithUpdatedCollection() throws Exception {
            collection.setName("New name");
            collection.setDescription("New description");
            when(collectionService.updateCollection(USER_ID, COLLECTION_ID, "New name", "New description"))
                    .thenReturn(collection);

            mockMvc.perform(put(BASE_URL + "/{collectionId}", USER_ID, COLLECTION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "New name", "description": "New description"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New name"))
                    .andExpect(jsonPath("$.description").value("New description"));
        }

        @Test
        void shouldReturn404WhenCollectionNotFound() throws Exception {
            when(collectionService.updateCollection(anyLong(), anyLong(), any(), any()))
                    .thenThrow(collectionNotFound());

            mockMvc.perform(put(BASE_URL + "/{collectionId}", USER_ID, COLLECTION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "New name", "description": "New description"}
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeleteCollection {

        @Test
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{collectionId}", USER_ID, COLLECTION_ID))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(collectionService).deleteCollection(USER_ID, COLLECTION_ID);
        }

        @Test
        void shouldReturn404WhenCollectionNotFound() throws Exception {
            doThrow(collectionNotFound()).when(collectionService).deleteCollection(USER_ID, COLLECTION_ID);

            mockMvc.perform(delete(BASE_URL + "/{collectionId}", USER_ID, COLLECTION_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class AddClothingItemToCollection {

        @Test
        void shouldReturn204() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{collectionId}/clothes/{itemId}", USER_ID, COLLECTION_ID, ITEM_ID))
                    .andExpect(status().isNoContent());

            verify(collectionService).addClothingItemToCollection(USER_ID, COLLECTION_ID, ITEM_ID);
        }

        @Test
        void shouldReturn404WhenItemNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Clothing item not found with id: " + ITEM_ID))
                    .when(collectionService).addClothingItemToCollection(USER_ID, COLLECTION_ID, ITEM_ID);

            mockMvc.perform(put(BASE_URL + "/{collectionId}/clothes/{itemId}", USER_ID, COLLECTION_ID, ITEM_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeleteClothingItemFromCollection {

        @Test
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{collectionId}/clothes/{itemId}", USER_ID, COLLECTION_ID, ITEM_ID))
                    .andExpect(status().isNoContent());

            verify(collectionService).deleteClothingItemFromCollection(USER_ID, COLLECTION_ID, ITEM_ID);
        }

        @Test
        void shouldReturn404WhenCollectionNotFound() throws Exception {
            doThrow(collectionNotFound())
                    .when(collectionService).deleteClothingItemFromCollection(USER_ID, COLLECTION_ID, ITEM_ID);

            mockMvc.perform(delete(BASE_URL + "/{collectionId}/clothes/{itemId}", USER_ID, COLLECTION_ID, ITEM_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetAllClothingItemsInCollection {

        @Test
        void shouldReturn200WithItems() throws Exception {
            when(collectionService.getAllClothingItemsInCollection(USER_ID, COLLECTION_ID))
                    .thenReturn(List.of(item));

            mockMvc.perform(get(BASE_URL + "/{collectionId}/clothes", USER_ID, COLLECTION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(ITEM_ID))
                    .andExpect(jsonPath("$[0].name").value("Black jacket"));
        }

        @Test
        void shouldReturn404WhenCollectionNotFound() throws Exception {
            when(collectionService.getAllClothingItemsInCollection(USER_ID, COLLECTION_ID))
                    .thenThrow(collectionNotFound());

            mockMvc.perform(get(BASE_URL + "/{collectionId}/clothes", USER_ID, COLLECTION_ID))
                    .andExpect(status().isNotFound());
        }
    }
}