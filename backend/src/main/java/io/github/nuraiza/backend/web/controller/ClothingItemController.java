package io.github.nuraiza.backend.web.controller;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.service.ClothingItemService;
import io.github.nuraiza.backend.web.dto.ClothingItemDtos;
import io.github.nuraiza.backend.web.mapper.ClothingItemMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/users/{userId}/clothes")
@RequiredArgsConstructor
public class ClothingItemController {

    private final ClothingItemService clothingItemService;

    private final ClothingItemMapper clothingItemMapper;

    @PostMapping
    public ResponseEntity<ClothingItemDtos.Response> createClothingItem(@PathVariable Long userId, @Valid @RequestBody ClothingItemDtos.CreateRequest body) {
        ClothingItemEntity clothingItemEntity = clothingItemService.createClothingItem(userId, body.name(), body.description());
        ClothingItemDtos.Response response = clothingItemMapper.toResponse(clothingItemEntity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{clothingItemId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{clothingItemId}")
    public ResponseEntity<ClothingItemDtos.Response> getClothingItem(@PathVariable Long userId, @PathVariable Long clothingItemId) {
        ClothingItemEntity clothingItemEntity = clothingItemService.getClothingItem(userId, clothingItemId);
        ClothingItemDtos.Response Response = clothingItemMapper.toResponse(clothingItemEntity);
        return ResponseEntity.ok(Response);
    }

    @GetMapping
    public ResponseEntity<List<ClothingItemDtos.Response>> getAllClothingItems(@PathVariable Long userId) {
        List<ClothingItemEntity> clothingItemEntities = clothingItemService.getAllClothingItems(userId);
        List<ClothingItemDtos.Response> clothingItemsResponse = clothingItemMapper.toResponse(clothingItemEntities);
        return ResponseEntity.ok(clothingItemsResponse);
    }

    @PutMapping("/{clothingItemId}")
    public ResponseEntity<ClothingItemDtos.Response> updateClothingItem(@PathVariable Long userId, @PathVariable Long clothingItemId, @Valid @RequestBody ClothingItemDtos.UpdateRequest body) {
        ClothingItemEntity clothingItemEntity = clothingItemService.updateClothingItem(userId, clothingItemId, body.name(), body.description());
        ClothingItemDtos.Response Response = clothingItemMapper.toResponse(clothingItemEntity);
        return ResponseEntity.ok(Response);
    }

    @DeleteMapping("/{clothingItemId}")
    public ResponseEntity<Void> deleteClothingItem(@PathVariable Long userId, @PathVariable Long clothingItemId) {
        clothingItemService.deleteClothingItem(userId, clothingItemId);
        return ResponseEntity.noContent().build();
    }
}
