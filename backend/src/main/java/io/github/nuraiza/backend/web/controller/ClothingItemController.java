package io.github.nuraiza.backend.web.controller;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.service.ClothingItemService;
import io.github.nuraiza.backend.web.dto.ClothingItemDtos;
import io.github.nuraiza.backend.web.mapper.ClothingItemMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
        ClothingItemEntity clothingItemEntity = clothingItemService.createClothingItem(userId, body.name(), body.description(), body.categoryIds());
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
    public ResponseEntity<Page<ClothingItemDtos.Response>> getAllClothingItems(@PathVariable Long userId,
                                                                               @RequestParam(required = false) List<String> categories,
                                                                               @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ClothingItemEntity> clothingItemEntities = clothingItemService.getAllClothingItems(userId, pageable, categories);
        return ResponseEntity.ok(clothingItemEntities.map(clothingItemMapper::toResponse));
    }

    @PutMapping("/{clothingItemId}")
    public ResponseEntity<ClothingItemDtos.Response> updateClothingItem(@PathVariable Long userId, @PathVariable Long clothingItemId, @Valid @RequestBody ClothingItemDtos.UpdateRequest body) {
        ClothingItemEntity clothingItemEntity = clothingItemService.updateClothingItem(userId, clothingItemId, body.name(), body.description(), body.categoryIds());
        ClothingItemDtos.Response Response = clothingItemMapper.toResponse(clothingItemEntity);
        return ResponseEntity.ok(Response);
    }

    @DeleteMapping("/{clothingItemId}")
    public ResponseEntity<Void> deleteClothingItem(@PathVariable Long userId, @PathVariable Long clothingItemId) {
        clothingItemService.deleteClothingItem(userId, clothingItemId);
        return ResponseEntity.noContent().build();
    }
}
