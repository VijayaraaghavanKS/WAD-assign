package com.ssn.userservice;

import java.util.HashMap;
import com.ssn.userservice.model.User;
import com.ssn.userservice.repository.UserRepository;
import com.ssn.userservice.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    // One demo account per role so the login page can offer all three.
    @Bean
    public CommandLineRunner seedUsers(UserRepository repository, AuthService auth) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new User(null, "shopper", auth.hash("demo123"), "USER", null, new HashMap<>()));
                repository.save(new User(null, "admin", auth.hash("demo123"), "ADMIN", null, new HashMap<>()));
                repository.save(new User(null, "developer", auth.hash("demo123"), "DEVELOPER", null, new HashMap<>()));
            }
        };
    }
}
