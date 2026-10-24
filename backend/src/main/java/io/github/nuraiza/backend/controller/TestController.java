package io.github.nuraiza.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/health")
    public String healthCheck() {
        return "Virtual Wardrobe Backend is up, running, and connected to PostgreSQL!";
    }
}