package com.scentstation.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // khai bao REST API controller
@RequestMapping("/api/v1") // Duong dan API goc cho tat ca cac endpoint trong controller nay
public class HealthController {

    @GetMapping("/health") // Xu li GET request den duong dan /api/v1/health
    public Map<String, String> healthCheck() {
        return Map.of( // Tra du lieu JSON
            "status", "UP",
            "service", "ScentStation Backend"
        );
    }
}
