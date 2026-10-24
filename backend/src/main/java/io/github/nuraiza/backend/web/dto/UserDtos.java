package io.github.nuraiza.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public class UserDtos {
    public record CreateRequest(
            @NotBlank(message = "Name cannot be blank") String name
    ) {}

    public record UpdateRequest(
            @NotBlank(message = "Name cannot be blank") String name
    ) {}

    public record Response(
            Long id,
            String name
    ){}
}
