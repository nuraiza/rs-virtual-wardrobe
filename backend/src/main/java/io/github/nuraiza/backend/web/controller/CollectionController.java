package io.github.nuraiza.backend.web.controller;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.persistence.entity.CollectionEntity;
import io.github.nuraiza.backend.service.CollectionService;
import io.github.nuraiza.backend.web.dto.ClothingItemDtos;
import io.github.nuraiza.backend.web.dto.CollectionDtos;
import io.github.nuraiza.backend.web.mapper.ClothingItemMapper;
import io.github.nuraiza.backend.web.mapper.CollectionMapper;
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
@RequestMapping("api/users/{userId}/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    private final CollectionMapper collectionMapper;

    private final ClothingItemMapper clothingItemMapper;

    @PostMapping
    public ResponseEntity<CollectionDtos.Response> createCollection(@PathVariable Long userId, @Valid @RequestBody CollectionDtos.CreateRequest body) {
        CollectionEntity collectionEntity = collectionService.createCollection(userId, body.name(), body.description());
        CollectionDtos.Response response = collectionMapper.toResponse(collectionEntity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{collectionId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<CollectionDtos.Response> getCollection(@PathVariable Long userId, @PathVariable Long collectionId) {
        CollectionEntity collectionEntity = collectionService.getCollection(userId, collectionId);
        return ResponseEntity.ok(collectionMapper.toResponse(collectionEntity));
    }

    @GetMapping
    public ResponseEntity<List<CollectionDtos.Response>> getCollections(@PathVariable Long userId) {
        List<CollectionEntity> collectionEntities = collectionService.getAllCollections(userId);
        return ResponseEntity.ok(collectionMapper.toResponse(collectionEntities));
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<CollectionDtos.Response> updateCollection(@PathVariable Long userId, @PathVariable Long collectionId, @Valid @RequestBody CollectionDtos.UpdateRequest body) {
        CollectionEntity collectionEntity = collectionService.updateCollection(userId, collectionId, body.name(), body.description());
        return ResponseEntity.ok(collectionMapper.toResponse(collectionEntity));
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long userId, @PathVariable Long collectionId) {
        collectionService.deleteCollection(userId, collectionId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{collectionId}/clothes/{clothingItemId}")
    public ResponseEntity<Void> addClothingItemToCollection(@PathVariable Long userId, @PathVariable Long collectionId, @PathVariable Long clothingItemId) {
        collectionService.addClothingItemToCollection(userId, collectionId, clothingItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{collectionId}/clothes/{clothingItemId}")
    public ResponseEntity<Void> deleteClothingItemFromCollection(@PathVariable Long userId, @PathVariable Long collectionId, @PathVariable Long clothingItemId) {
        collectionService.deleteClothingItemFromCollection(userId, collectionId, clothingItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{collectionId}/clothes")
    public ResponseEntity<Page<ClothingItemDtos.Response>> getAllClothingItemsInCollection(@PathVariable Long userId, @PathVariable Long collectionId,
                                                                                           @RequestParam(required = false) List<String> categories,
                                                                                           @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ClothingItemEntity> clothingItemEntities = collectionService.getAllClothingItemsInCollection(userId, collectionId, pageable, categories);
        return ResponseEntity.ok(clothingItemEntities.map(clothingItemMapper::toResponse));
    }
}
