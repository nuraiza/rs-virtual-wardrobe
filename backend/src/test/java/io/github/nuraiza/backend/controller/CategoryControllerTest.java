package io.github.nuraiza.backend.controller;

import io.github.nuraiza.backend.persistence.entity.CategoryEntity;
import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.service.CategoryService;
import io.github.nuraiza.backend.web.controller.CategoryController;
import io.github.nuraiza.backend.web.mapper.CategoryMapper;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(CategoryMapper.class)
class CategoryControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long CATEGORY_ID = 10L;
    private static final String BASE_URL = "/api/users/{userId}/categories";
    private static final String CATEGORY_URL = BASE_URL + "/{categoryId}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    private CategoryEntity category;

    @BeforeEach
    void setUp() {
        UserEntity user = new UserEntity();
        user.setId(USER_ID);
        user.setName("Antony");

        category = new CategoryEntity();
        category.setId(CATEGORY_ID);
        category.setName("Office");
        category.setUser(user);
    }

    @Nested
    class CreateCategory {

        @Test
        void shouldReturn201WithCreatedCategory() throws Exception {
            when(categoryService.createCategory(eq(USER_ID), eq("Office"))).thenReturn(category);

            mockMvc.perform(post(BASE_URL, USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Office"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(CATEGORY_ID))
                    .andExpect(jsonPath("$.name").value("Office"));
        }
    }

    @Nested
    class GetUserCategories {

        @Test
        void shouldReturn200WithListOfCategories() throws Exception {
            when(categoryService.getUserCategories(USER_ID)).thenReturn(List.of(category));

            mockMvc.perform(get(BASE_URL, USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(CATEGORY_ID))
                    .andExpect(jsonPath("$[0].name").value("Office"));
        }

        @Test
        void shouldReturn200WithEmptyListWhenUserHasNoCategories() throws Exception {
            when(categoryService.getUserCategories(USER_ID)).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL, USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class DeleteCategory {

        @Test
        void shouldReturn204WhenCategoryDeleted() throws Exception {
            doNothing().when(categoryService).deleteCategory(USER_ID, CATEGORY_ID);

            mockMvc.perform(delete(CATEGORY_URL, USER_ID, CATEGORY_ID))
                    .andExpect(status().isNoContent());

            verify(categoryService).deleteCategory(USER_ID, CATEGORY_ID);
        }

        @Test
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Category not found with id: " + CATEGORY_ID))
                    .when(categoryService).deleteCategory(USER_ID, CATEGORY_ID);

            mockMvc.perform(delete(CATEGORY_URL, USER_ID, CATEGORY_ID))
                    .andExpect(status().isNotFound());
        }
    }
}