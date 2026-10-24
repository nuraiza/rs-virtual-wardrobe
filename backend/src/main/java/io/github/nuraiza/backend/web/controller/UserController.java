package io.github.nuraiza.backend.web.controller;

import io.github.nuraiza.backend.persistence.entity.UserEntity;
import io.github.nuraiza.backend.service.UserService;
import io.github.nuraiza.backend.web.dto.UserDtos;
import io.github.nuraiza.backend.web.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserDtos.Response> createUser(@Valid @RequestBody UserDtos.CreateRequest body) {
        UserEntity userEntity = userService.createUser(body.name());
        UserDtos.Response response = userMapper.toResponse(userEntity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDtos.Response> getUser(@PathVariable Long id) {
        UserEntity userEntity = userService.getUser(id);
        UserDtos.Response Response = userMapper.toResponse(userEntity);
        return ResponseEntity.ok(Response);
    }
}
