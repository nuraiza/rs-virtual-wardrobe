package io.github.nuraiza.backend.web.mapper;

import io.github.nuraiza.backend.persistence.entity.CollectionEntity;
import io.github.nuraiza.backend.web.dto.CollectionDtos;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CollectionMapper {

    public CollectionDtos.Response toResponse(CollectionEntity collectionEntity){
        if (collectionEntity == null) return null;
        return new CollectionDtos.Response(
                collectionEntity.getId(),
                collectionEntity.getName(),
                collectionEntity.getDescription(),
                collectionEntity.getUser().getId()
        );
    }

    public List<CollectionDtos.Response> toResponse(List<CollectionEntity> collectionEntities){
        return collectionEntities.stream()
                .map(this::toResponse)
                .toList();
    }
}
