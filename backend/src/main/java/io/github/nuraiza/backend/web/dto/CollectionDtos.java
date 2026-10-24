package io.github.nuraiza.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CollectionDtos {
    public record CreateRequest(
            @NotBlank @Size(max = 255) String name,
            @Size(max = 1000) String description
    ) {}

    public record UpdateRequest(
            @NotBlank @Size(max = 255) String name,
            @Size(max = 1000) String description
    ) {}

    public record Response(
            Long id,
            String name,
            String description,
            Long userId
    ) {}
}
