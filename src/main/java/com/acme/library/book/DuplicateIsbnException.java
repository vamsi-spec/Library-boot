package com.acme.library.book;

public class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException(String isbn) {
        super("Book with ISBN " + isbn + " already exists");
    }
}
