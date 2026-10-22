package com.acme.library.book;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.acme.library.common.ApiPaths;
import com.acme.library.common.PageResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPaths.v2 + "/books")

public class BookControllerV2 {
    private final BookService bookService;

    public BookControllerV2(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public PageResponse<BookResponseV2> list(
        @RequestParam(required = false) String title,
        @RequestParam(required = false) Boolean available,
        @RequestParam(required = false) String author,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size,
        @RequestParam(required = false) List<String> sort  
    ) {
        return bookService.search(title, author, available, page, size, sort).map(BookResponseV2 :: from);
    }

    @GetMapping("/{id}")
    public BookResponseV2 get(@PathVariable long id) {
        return BookResponseV2.from(bookService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BookResponseV2> create(@Valid @RequestBody CreateBookRequest request) {
        Book created = bookService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();

        return ResponseEntity.created(location).body(BookResponseV2.from(created));
    }

    @PatchMapping("/{id}")
    public BookResponseV2 patch(@PathVariable long id, @Valid @RequestBody BookPatch patch) {
        return BookResponseV2.from(bookService.patch(id, patch));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        bookService.delete(id);
    }
}
