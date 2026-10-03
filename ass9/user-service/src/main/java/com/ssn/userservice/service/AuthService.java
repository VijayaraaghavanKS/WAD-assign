package com.ssn.userservice.service;

import com.ssn.userservice.model.User;
import com.ssn.userservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    // Demo-grade hashing (SHA-256). A real system would use bcrypt or argon2.
    public String hash(String password) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public User register(String username, String password) {
        if (repository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        return repository.save(new User(null, username, hash(password), "USER", null));
    }

    // Returns a fresh random token. Every service checks it by calling GET /api/auth/me.
    public User login(String username, String password) {
        User user = repository.findByUsername(username)
                .filter(u -> u.getPasswordHash().equals(hash(password)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username or password"));
        user.setToken(UUID.randomUUID().toString());
        return repository.save(user);
    }

    public User findByToken(String token) {
        return repository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in"));
    }
}
