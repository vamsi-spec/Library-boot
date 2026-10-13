package com.acme.library.book;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Repository;

@Repository 
public class InMemoryBookRepository implements BookRepository{
    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final Map<String, Long> idByIsbn = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong();

    public Book insert(Book book) {
        long id = nextId.getAndIncrement();

        if(idByIsbn.putIfAbsent(book.isbn(), id) != null) {
            throw new DuplicateIsbnException(book.isbn());
        }

        Book saved = new Book(id, book.isbn(), book.title(), book.author(), book.totalCopies(), book.availableCopies(), book.createdAt());

        books.put(id, saved);
        return saved;
    }

    public Optional<Book> findById(long id) {
        return Optional.ofNullable(books.get(id));
    }

    public List<Book> findAll() {
        return books.values().stream().sorted(Comparator.comparingLong(Book :: id)).toList();

    }

    public Optional<Book> update(long id, UnaryOperator<Book> change) {
        Book updated = books.computeIfPresent(id, (key, current) -> {
            Book next = change.apply(current);
            if(next.id() != current.id() || !next.isbn().equals(current.isbn())) {
                throw new IllegalStateException("id and isbn must not change");
            }
            return next;           
        });

        return Optional.ofNullable(updated);
    }

    public Optional<Book> deleteById(long id, Consumer<Book> guard) {
        AtomicReference<Book> removed = new AtomicReference<>();

        books.computeIfPresent(id, (key, current) -> {
            guard.accept(current);
            removed.set(current);
            return null;
        });

        Book book = removed.get();
        if(book == null) {
            return Optional.empty();
        }

        idByIsbn.remove(book.isbn());
        return Optional.of(book);
    }

}
