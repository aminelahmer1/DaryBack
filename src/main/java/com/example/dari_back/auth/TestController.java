package com.example.dari_back.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('VISITOR') or hasRole('DANCER') or hasRole('ADMIN') or hasRole('EVALUATOR')")
    public String userAccess() {
        return "User Content.";
    }

    @GetMapping("/mod")
    @PreAuthorize("hasRole('DANCER')")
    public String moderatorAccess() {
        return "Moderator Board.";
    }

    @GetMapping("/eva")
    @PreAuthorize("hasRole('EVALUATOR')")
    public String adminAccess() {
        return "Moderator Board.";
    }
}