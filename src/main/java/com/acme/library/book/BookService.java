package com.acme.library.book;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import com.acme.library.common.BusinessRuleException;
import com.acme.library.common.ErrorCode;
import com.acme.library.common.ResourceNotFoundException;

@Service
public class BookService {
    private BookRepository bookRepository;
    private final Clock clock;

    public BookService(BookRepository bookRepository, Clock clock) {
        this.bookRepository = bookRepository;
        this.clock = clock;
    }

    public Book create(CreateBookRequest request) {
        Book book = new Book(0L, request.isbn(), request.title(), request.author(), request.totalCopies(), request.totalCopies(), clock.instant());
        
        try {
            return bookRepository.insert(book);
        } catch (DuplicateIsbnException e) {
            throw new BusinessRuleException(ErrorCode.ISBN_ALREADY_EXISTS, e.getMessage());
        }
    }

    public Book findById(long id) {
        return bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("book", id));
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Book replace(long id, UpdateBookRequest request) {
        return bookRepository.update(id, current -> applyChanges(current, request.title().strip(),request.author().strip(),request.totalCopies())).orElseThrow(() -> new ResourceNotFoundException("book", id));
    }

    public Book patch(long id, BookPatch patch) {
        return bookRepository.update(id, current -> applyChanges(
                        current,
                        patch.title() != null ? patch.title().strip() : current.title(),
                        patch.author() != null ? patch.author().strip() : current.author(),
                        patch.totalCopies() != null ? patch.totalCopies() : current.totalCopies()))
                .orElseThrow(() -> new ResourceNotFoundException("book", id));
    }

    private Book applyChanges(Book current, String title, String author, int newTotalCopies) {
        if (newTotalCopies < current.copiesOnLoan()) {
            throw new BusinessRuleException(
                    ErrorCode.TOTAL_COPIES_BELOW_ON_LOAN,
                    "Cannot reduce total copies to " + newTotalCopies + "; " + current.copiesOnLoan() + " copies are currently on loan"
            );
        }
        int availableCopies = current.availableCopies() + (newTotalCopies - current.totalCopies());
        return new Book(
                current.id(),
                current.isbn(),
                title,
                author,
                newTotalCopies,
                availableCopies,
                current.createdAt()
        );
    }

    public void delete(long id) {
        bookRepository.deleteById(id, book -> {
                    if (book.copiesOnLoan() > 0) {
                        throw new BusinessRuleException(ErrorCode.ACTIVE_LOANS_EXIST,
                                "Book " + id + " has " + book.copiesOnLoan()
                                        + " copies on loan and cannot be deleted");
                    }
                })
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
    }
}
