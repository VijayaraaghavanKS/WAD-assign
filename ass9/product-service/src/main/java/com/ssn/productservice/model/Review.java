package com.ssn.productservice.model;

import java.time.Instant;

// A shopper's review, stored inside its product document.
public record Review(String username, int rating, String title, String body, Instant postedAt) {}
