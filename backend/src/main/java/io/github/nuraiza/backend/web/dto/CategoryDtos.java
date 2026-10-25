package io.github.nuraiza.backend.web.dto;

public class CategoryDtos {
    public record CreateRequest(String name) {}
    public record Response(Long id, String name) {}
}