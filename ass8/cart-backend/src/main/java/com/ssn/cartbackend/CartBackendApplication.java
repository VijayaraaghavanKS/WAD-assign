package com.ssn.cartbackend;

import java.util.HashMap;
import com.ssn.cartbackend.model.Product;
import com.ssn.cartbackend.model.User;
import com.ssn.cartbackend.repository.ProductRepository;
import com.ssn.cartbackend.repository.UserRepository;
import com.ssn.cartbackend.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CartBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartBackendApplication.class, args);
	}

	// Seeds demo products and one demo account per role on an empty database.
	@Bean
	public CommandLineRunner seedData(ProductRepository products, UserRepository users, AuthService auth) {
		return args -> {
			if (users.count() == 0) {
				users.save(new User(null, "shopper", auth.hash("demo123"), "USER", null, new HashMap<>()));
				users.save(new User(null, "admin", auth.hash("demo123"), "ADMIN", null, new HashMap<>()));
				users.save(new User(null, "developer", auth.hash("demo123"), "DEVELOPER", null, new HashMap<>()));
			}
			if (products.count() == 0) {
				products.save(new Product(null, "Laptop", 55000, 14, "Electronics", 15));
				products.save(new Product(null, "Headphones", 2000, 40, "Audio", 20));
				products.save(new Product(null, "Keyboard", 1500, 25, "Electronics", 0));
				products.save(new Product(null, "Mouse", 700, 60, "Electronics", 10));
				products.save(new Product(null, "Webcam", 3199, 4, "Electronics", 0));
				products.save(new Product(null, "Smartphone", 18999, 22, "Electronics", 12));
				products.save(new Product(null, "Smartwatch", 4499, 30, "Wearables", 25));
				products.save(new Product(null, "Running Shoes", 3199, 18, "Fashion", 30));
				products.save(new Product(null, "Travel Backpack", 1799, 35, "Fashion", 0));
				products.save(new Product(null, "Bluetooth Speaker", 2599, 3, "Audio", 18));
				products.save(new Product(null, "4K Monitor", 20999, 9, "Electronics", 0));
				products.save(new Product(null, "Tablet", 24999, 12, "Electronics", 10));
			}
		};
	}

}
