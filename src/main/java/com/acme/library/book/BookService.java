package com.acme.library.book;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.acme.library.common.BusinessRuleException;
import com.acme.library.common.ErrorCode;
import com.acme.library.common.PageQuery;
import com.acme.library.common.PageResponse;
import com.acme.library.common.Paging;
import com.acme.library.common.ResourceNotFoundException;

@Service
public class BookService {
    private BookRepository bookRepository;
    private final Clock clock;

    public BookService(BookRepository bookRepository, Clock clock) {
        this.bookRepository = bookRepository;
        this.clock = clock;
    }
 
    private static final Map<String, Comparator<Book>> SORTS = Map.of(
        "id", Comparator.comparingLong(Book :: id),
        "isbn", Comparator.comparing(Book :: isbn),
        "title", Comparator.comparing(Book :: title, String.CASE_INSENSITIVE_ORDER),
        "author", Comparator.comparing(Book::author, String.CASE_INSENSITIVE_ORDER),
        "totalCopies", Comparator.comparingInt(Book::totalCopies),
        "availableCopies", Comparator.comparingInt(Book::availableCopies)
    );

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

    public PageResponse<Book> search(String title, String author, Boolean available, int page, int size, List<String> sort) {
        PageQuery query = PageQuery.of(page, size, sort, SORTS.keySet(), "id");
        String titleFilter = normalize(title);
        String authorFilter = normalize(author);

        List<Book> matches = bookRepository.findAll().stream()
        .filter(b -> titleFilter == null || b.title().toLowerCase(Locale.ROOT).contains(titleFilter))
        .filter(b -> authorFilter == null || b.author().toLowerCase(Locale.ROOT).contains(authorFilter))
        .filter(b -> available == null || b.isAvailable() == available)
        .toList();

        return Paging.paginate(matches, query, SORTS);
    }

    private static String normalize(String text) {
        return (text == null || text.isBlank()) ? null : text.strip().toLowerCase(Locale.ROOT);
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

    public Book reserveCopy(long bookId) {
        return bookRepository.update(bookId, current -> {
            if(!current.isAvailable()) {
                throw new BusinessRuleException(ErrorCode.NO_COPIES_AVAILABLE, "No copies of book" + bookId + "are available");
            }

            return new Book(current.id(), current.isbn(), current.title(), current.author(), current.totalCopies(), current.availableCopies() - 1, current.createdAt());
        }).orElseThrow(() -> new ResourceNotFoundException("book", bookId));
    }

    public Book releaseCopy(long bookId) {
        return bookRepository.update(bookId, current -> new Book(current.id(), current.isbn(), current.title(), current.author(), current.totalCopies(), current.availableCopies() + 1, current.createdAt())).orElseThrow(() -> new IllegalStateException("Book " + bookId + " disappeared while one of its copies was on loan"));
    }
}
