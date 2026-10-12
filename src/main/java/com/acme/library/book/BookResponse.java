package com.acme.library.book;

public record BookResponse(
        long id,
        String isbn,
        String title,
        String author,
        int totalCopies,
        int availableCopies) {

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.id(),
                book.isbn(),
                book.title(),
                book.author(),
                book.totalCopies(),
                book.availableCopies());
    }
}