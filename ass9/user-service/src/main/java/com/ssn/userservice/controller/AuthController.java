package com.ssn.userservice.controller;

import com.ssn.userservice.model.User;
import com.ssn.userservice.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Other services never read this database. They call GET /api/auth/me with the
// browser's Bearer token to find out who is logged in and what role they have.
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    public record Credentials(String username, String password) {}

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Credentials body) {
        User user = auth.register(body.username(), body.password());
        return Map.of("id", user.getId(), "username", user.getUsername(), "role", user.getRole());
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Credentials body) {
        User user = auth.login(body.username(), body.password());
        return Map.of("token", user.getToken(), "username", user.getUsername(), "role", user.getRole());
    }

    @GetMapping("/me")
    public Map<String, String> me(@RequestHeader("Authorization") String authorization) {
        User user = auth.findByToken(authorization.replaceFirst("^Bearer ", ""));
        return Map.of("id", user.getId(), "username", user.getUsername(), "role", user.getRole());
    }
}
