package com.acme.library.book;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface BookRepository {
    
    Book insert(Book book);

    Optional<Book> findById(long id);

    List<Book> findAll();

    Optional<Book> update(long id, UnaryOperator<Book> change);

    boolean deleteById(long id);
}
