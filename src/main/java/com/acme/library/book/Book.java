package com.acme.library.book;

import java.time.Instant;

public record Book(long id, String isbn, String title, String author, int totalCopies, int availableCopies, Instant createdAt) {
    public Book {
        if(totalCopies < 0) {
            throw new IllegalArgumentException("totalCopies must be non-negative");
        }
        if(availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalArgumentException("availableCopies must be between 0 and totalCopies");
        }
    }

    public int copiesOnLoan() {
        return totalCopies - availableCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }
}
