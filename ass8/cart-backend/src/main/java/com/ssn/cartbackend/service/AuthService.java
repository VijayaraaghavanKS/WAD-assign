package com.ssn.cartbackend.service;

import java.util.HashMap;
import java.util.Map;
import com.ssn.cartbackend.model.User;
import com.ssn.cartbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
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
        if (users.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        return users.save(new User(null, username, hash(password), "USER", null, new HashMap<>()));
    }

    public User login(String username, String password) {
        User user = users.findByUsername(username)
                .filter(u -> u.getPasswordHash().equals(hash(password)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username or password"));
        user.setToken(UUID.randomUUID().toString());
        return users.save(user);
    }

    // Reads the "Bearer <token>" header and returns the user it belongs to, or 401.
    public User requireUser(String authorization) {
        if (authorization == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in first");
        }
        String token = authorization.replaceFirst("^Bearer ", "");
        return users.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in first"));
    }

    public User requireAdmin(String authorization) {
        User user = requireUser(authorization);
        if (!"ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins only");
        }
        return user;
    }

    // Returns the preferences saved on the account (empty for accounts that never saved any).
    public Map<String, Object> prefsOf(User user) {
        return user.getPrefs() == null ? Map.of() : user.getPrefs();
    }

    public Map<String, Object> savePrefs(User user, Map<String, Object> prefs) {
        user.setPrefs(new HashMap<>(prefs));
        users.save(user);
        return user.getPrefs();
    }
}
