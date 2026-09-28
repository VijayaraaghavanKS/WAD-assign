package com.ssn.productservice;

import com.ssn.productservice.model.Product;
import com.ssn.productservice.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedProducts(ProductRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				repository.save(new Product(null, "Laptop", 55000, 14));
				repository.save(new Product(null, "Headphones", 2000, 40));
				repository.save(new Product(null, "Keyboard", 1500, 25));
				repository.save(new Product(null, "Mouse", 700, 60));
				repository.save(new Product(null, "Webcam", 3199, 4));
				repository.save(new Product(null, "Smartphone", 18999, 22));
				repository.save(new Product(null, "Smartwatch", 4499, 30));
				repository.save(new Product(null, "Running Shoes", 3199, 18));
				repository.save(new Product(null, "Travel Backpack", 1799, 35));
				repository.save(new Product(null, "Bluetooth Speaker", 2599, 3));
				repository.save(new Product(null, "4K Monitor", 20999, 9));
				repository.save(new Product(null, "Tablet", 24999, 12));
			}
		};
	}

}
