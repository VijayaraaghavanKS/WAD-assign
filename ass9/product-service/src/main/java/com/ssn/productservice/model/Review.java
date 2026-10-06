package com.ssn.productservice.model;

import java.time.Instant;

// A shopper's review, stored inside its product document. verified is true when the shopper has bought the product.
// Reviews saved before this field existed read back as null, so it is nullable (null means not verified).
public record Review(String username, int rating, String title, String body, Instant postedAt, Boolean verified) {}
