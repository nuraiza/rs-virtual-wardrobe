package io.github.nuraiza.backend.persistence.specification;

import io.github.nuraiza.backend.persistence.entity.CategoryEntity;
import io.github.nuraiza.backend.persistence.entity.ClothingItemEntity;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ClothingItemSpecifications {

    public static Specification<ClothingItemEntity> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<ClothingItemEntity> belongsToCollection(Long collectionId){
        return (root, query, cb) -> cb.equal(root.get("collections").get("id"), collectionId);
    }

    public static Specification<ClothingItemEntity> hasCategories(List<String> categoryNames) {
        return (root, query, cb) -> {

            if(categoryNames.isEmpty()) return null;

            List<String> readyForQuery = categoryNames.stream().
                    filter(name -> name != null && !name.isBlank())
                    .map(String::toLowerCase)
                    .toList();

            if(readyForQuery.isEmpty()) return null;

            Join<ClothingItemEntity, CategoryEntity> categoryJoin = root.join("categories");
            query.distinct(true);

            return cb.lower(categoryJoin.get("name")).in(readyForQuery);
        };
    }
}