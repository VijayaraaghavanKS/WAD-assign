package com.ssn.userservice.model;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;
    private String passwordHash;
    private String role; // USER, ADMIN or DEVELOPER

    // Session token issued at login. Null while logged out.
    private String token;

    // Shopper preferences: wishlist, compare, recently viewed and address. Set by PUT /api/auth/prefs.
    private Map<String, Object> prefs;
}
