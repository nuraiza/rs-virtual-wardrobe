package io.github.nuraiza.backend.controller;

import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.service.UserService;
import io.github.nuraiza.backend.web.controller.UserController;
import io.github.nuraiza.backend.web.mapper.UserMapper;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserMapper.class)
class UserControllerTest {

    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "Antony";
    private static final String BASE_URL = "/api/users";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(USER_ID);
        user.setName(USER_NAME);
    }

    @Nested
    class CreateUser {

        @Test
        void shouldReturn201WithLocationAndCreatedUser() throws Exception {
            when(userService.createUser(USER_NAME)).thenReturn(user);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "Antony"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith(BASE_URL + "/" + USER_ID)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.name").value(USER_NAME));

            verify(userService).createUser(USER_NAME);
        }

        @Test
        void shouldReturn400AndNotCallServiceWhenNameIsBlank() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": ""}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    class GetUser {

        @Test
        void shouldReturn200WithUser() throws Exception {
            when(userService.getUser(USER_ID)).thenReturn(user);

            mockMvc.perform(get(BASE_URL + "/{id}", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.name").value(USER_NAME));
        }

        @Test
        void shouldReturn404ProblemDetailWhenUserNotFound() throws Exception {
            when(userService.getUser(USER_ID))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + USER_ID));

            mockMvc.perform(get(BASE_URL + "/{id}", USER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.detail").value(containsString("User not found with id: " + USER_ID)));
        }
    }
}