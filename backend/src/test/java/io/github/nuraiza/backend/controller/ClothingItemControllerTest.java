package io.github.nuraiza.backend.controller;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.service.ClothingItemService;
import io.github.nuraiza.backend.web.controller.ClothingItemController;
import io.github.nuraiza.backend.web.mapper.ClothingItemMapper;
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
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClothingItemController.class)
@Import(ClothingItemMapper.class)
class ClothingItemControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 100L;
    private static final String BASE_URL = "/api/users/{userId}/clothes";
    private static final String ITEM_URL = BASE_URL + "/{clothingItemId}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClothingItemService clothingItemService;

    private ClothingItemEntity item;

    @BeforeEach
    void setUp() {
        UserEntity user = new UserEntity();
        user.setId(USER_ID);
        user.setName("Antony");

        item = new ClothingItemEntity("Black jacket", "Warm winter jacket", user);
        item.setId(ITEM_ID);
    }

    private EntityNotFoundException itemNotFound() {
        return new EntityNotFoundException("Clothing item not found with id: " + ITEM_ID);
    }

    @Nested
    class CreateClothingItem {

        @Test
        void shouldReturn201WithLocationAndCreatedItem() throws Exception {
            when(clothingItemService.createClothingItem(USER_ID, "Black jacket", "Warm winter jacket"))
                    .thenReturn(item);

            mockMvc.perform(post(BASE_URL, USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "Black jacket", "description": "Warm winter jacket"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/api/users/" + USER_ID + "/clothes/" + ITEM_ID)))
                    .andExpect(jsonPath("$.id").value(ITEM_ID))
                    .andExpect(jsonPath("$.name").value("Black jacket"))
                    .andExpect(jsonPath("$.description").value("Warm winter jacket"));
        }

        @Test
        void shouldReturn400AndNotCallServiceWhenNameIsBlank() throws Exception {
            mockMvc.perform(post(BASE_URL, USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "", "description": "x"}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(clothingItemService);
        }
    }

    @Nested
    class GetClothingItem {

        @Test
        void shouldReturn200WithItem() throws Exception {
            when(clothingItemService.getClothingItem(USER_ID, ITEM_ID)).thenReturn(item);

            mockMvc.perform(get(ITEM_URL, USER_ID, ITEM_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ITEM_ID))
                    .andExpect(jsonPath("$.name").value("Black jacket"));
        }

        @Test
        void shouldReturn404ProblemDetailWhenItemNotFound() throws Exception {
            when(clothingItemService.getClothingItem(USER_ID, ITEM_ID)).thenThrow(itemNotFound());

            mockMvc.perform(get(ITEM_URL, USER_ID, ITEM_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value(containsString(ITEM_ID.toString())));
        }
    }

    @Nested
    class GetAllClothingItems {

        @Test
        void shouldReturn200WithAllItems() throws Exception {
            when(clothingItemService.getAllClothingItems(USER_ID)).thenReturn(List.of(item));

            mockMvc.perform(get(BASE_URL, USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(ITEM_ID));
        }
    }

    @Nested
    class UpdateClothingItem {

        @Test
        void shouldReturn200WithUpdatedItem() throws Exception {
            item.setName("Grey jacket");
            item.setDescription("Light");
            when(clothingItemService.updateClothingItem(USER_ID, ITEM_ID, "Grey jacket", "Light"))
                    .thenReturn(item);

            mockMvc.perform(put(ITEM_URL, USER_ID, ITEM_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "Grey jacket", "description": "Light"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Grey jacket"))
                    .andExpect(jsonPath("$.description").value("Light"));
        }

        @Test
        void shouldReturn404WhenItemNotFound() throws Exception {
            when(clothingItemService.updateClothingItem(USER_ID, ITEM_ID, "Grey jacket", "Light"))
                    .thenThrow(itemNotFound());

            mockMvc.perform(put(ITEM_URL, USER_ID, ITEM_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "Grey jacket", "description": "Light"}
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeleteClothingItem {

        @Test
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete(ITEM_URL, USER_ID, ITEM_ID))
                    .andExpect(status().isNoContent());

            verify(clothingItemService).deleteClothingItem(USER_ID, ITEM_ID);
        }

        @Test
        void shouldReturn404WhenItemNotFound() throws Exception {
            doThrow(itemNotFound()).when(clothingItemService).deleteClothingItem(USER_ID, ITEM_ID);

            mockMvc.perform(delete(ITEM_URL, USER_ID, ITEM_ID))
                    .andExpect(status().isNotFound());
        }
    }
}