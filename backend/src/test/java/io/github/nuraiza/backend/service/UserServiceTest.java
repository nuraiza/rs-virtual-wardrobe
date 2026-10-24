package io.github.nuraiza.backend.service;

import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.persistence.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "Antony";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(USER_ID);
        user.setName(USER_NAME);
    }

    @Nested
    class GetUser {

        @Test
        void shouldReturnUserWhenUserExists() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            UserEntity result = userService.getUser(USER_ID);

            assertThat(result).isSameAs(user);
            assertThat(result.getName()).isEqualTo(USER_NAME);
            verify(userRepository).findById(USER_ID);
        }

        @Test
        void shouldThrowWhenUserDoesNotExist() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUser(USER_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found with id: " + USER_ID);

            verify(userRepository).findById(USER_ID);
        }
    }

    @Nested
    class CreateUser {

        @Test
        void shouldSaveUserWithGivenNameAndReturnIt() {
            when(userRepository.save(any(UserEntity.class)))
                    .thenAnswer(invocation -> {
                        UserEntity saved = invocation.getArgument(0);
                        saved.setId(USER_ID); // simulate DB-generated id
                        return saved;
                    });

            UserEntity result = userService.createUser(USER_NAME);

            ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(captor.capture());

            assertThat(captor.getValue().getName()).isEqualTo(USER_NAME);
            assertThat(result).isSameAs(captor.getValue());
            assertThat(result.getId()).isEqualTo(USER_ID);
        }
    }
}