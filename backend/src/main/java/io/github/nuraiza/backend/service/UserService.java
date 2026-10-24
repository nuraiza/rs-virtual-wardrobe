package io.github.nuraiza.backend.service;

import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.persistence.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity createUser(String name) {
        UserEntity user = new UserEntity(name);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserEntity getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("User not found with id: " + id));
    }
}
