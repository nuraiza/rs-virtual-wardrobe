package io.github.nuraiza.backend.web.mapper;


import io.github.nuraiza.backend.persistence.entity.CategoryEntity;
import io.github.nuraiza.backend.web.dto.CategoryDtos;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDtos.Response toResponse(CategoryEntity categoryEntity){
        if (categoryEntity == null) return null;
        return new CategoryDtos.Response(
                categoryEntity.getId(),
                categoryEntity.getName()
        );
    }
}
