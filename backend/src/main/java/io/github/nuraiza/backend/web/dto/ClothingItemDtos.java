package io.github.nuraiza.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public class ClothingItemDtos {
    public record CreateRequest(
            @NotBlank @Size(max = 255) String name,
            @Size(max=1000) String description,
            Set<Long> categoryIds
    ) {}

    public record UpdateRequest(
            @NotBlank @Size(max = 255) String name,
            @Size(max=1000) String description,
            Set<Long> categoryIds
    ) {}

    public record Response(
            Long id,
            String name,
            String description,
            Long userId,
            List<CategoryDtos.Response> categories
    ) {}
}
