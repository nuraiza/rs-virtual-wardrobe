package io.github.nuraiza.backend.web.controller;

import io.github.nuraiza.backend.persistence.entity.CategoryEntity;
import io.github.nuraiza.backend.service.CategoryService;
import io.github.nuraiza.backend.web.dto.CategoryDtos;
import io.github.nuraiza.backend.web.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    private final CategoryMapper categoryMapper;

    @PostMapping
    public ResponseEntity<CategoryDtos.Response> createCategory(
            @PathVariable Long userId,
            @RequestBody CategoryDtos.CreateRequest request) {
        CategoryEntity categoryEntity = categoryService.createCategory(userId, request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toResponse(categoryEntity));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDtos.Response>> getUserCategories(@PathVariable Long userId) {
        List<CategoryEntity> categoryEntities = categoryService.getUserCategories(userId);
        return ResponseEntity.ok(categoryEntities.stream().map(categoryMapper::toResponse).toList());
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long userId, @PathVariable Long categoryId){
        categoryService.deleteCategory(userId, categoryId);
        return ResponseEntity.noContent().build();
    }
}
