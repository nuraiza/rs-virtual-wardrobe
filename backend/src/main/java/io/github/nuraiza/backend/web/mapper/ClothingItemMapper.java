package io.github.nuraiza.backend.web.mapper;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.web.dto.ClothingItemDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothingItemMapper {

    private final CategoryMapper categoryMapper;

    public ClothingItemDtos.Response toResponse(ClothingItemEntity clothingItemEntity){
        if (clothingItemEntity == null) return null;
        return new ClothingItemDtos.Response(
                clothingItemEntity.getId(),
                clothingItemEntity.getName(),
                clothingItemEntity.getDescription(),
                clothingItemEntity.getUser().getId(),
                clothingItemEntity.getCategories().stream().map(categoryMapper::toResponse).toList()
        );
    }
}
