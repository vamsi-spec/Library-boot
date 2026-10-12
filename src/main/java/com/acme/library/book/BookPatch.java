package com.acme.library.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BookPatch(                 // every field is optional: null means "leave unchanged"

        @Pattern(regexp = ".*\\S.*", message = "title must not be blank")
        @Size(max = 150, message = "title must be at most 150 characters")
        String title,

        @Pattern(regexp = ".*\\S.*", message = "author must not be blank")
        @Size(max = 100, message = "author must be at most 100 characters")
        String author,

        @Positive(message = "totalCopies must be at least 1")
        @Max(value = 1000, message = "totalCopies must be at most 1000")
        Integer totalCopies) {}