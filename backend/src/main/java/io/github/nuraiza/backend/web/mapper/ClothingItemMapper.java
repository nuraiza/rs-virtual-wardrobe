package io.github.nuraiza.backend.web.mapper;

import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import io.github.nuraiza.backend.web.dto.ClothingItemDtos;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClothingItemMapper {

    public ClothingItemDtos.Response toResponse(ClothingItemEntity clothingItemEntity){
        if (clothingItemEntity == null) return null;
        return new ClothingItemDtos.Response(
                clothingItemEntity.getId(),
                clothingItemEntity.getName(),
                clothingItemEntity.getDescription(),
                clothingItemEntity.getUser().getId()
        );
    }

    public List<ClothingItemDtos.Response> toResponse(List<ClothingItemEntity> clothingItemEntities){
        return clothingItemEntities.stream()
                .map(this::toResponse)
                .toList();
    }
}
