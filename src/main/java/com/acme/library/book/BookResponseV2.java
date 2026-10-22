package com.acme.library.book;


public record BookResponseV2(
    long id,
    String isbn,
    String title,
    String author,
    Copies copies
) {
    public record Copies(int total, int available) {}

    public static BookResponseV2 from(Book book) {
        return new BookResponseV2(book.id(), book.isbn(), book.title(), book.author(), new Copies(book.totalCopies(), book.availableCopies()));
    }
}
