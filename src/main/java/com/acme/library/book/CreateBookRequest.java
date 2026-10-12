package com.acme.library.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateBookRequest(
    @NotNull(message = "isbn is required")
    @Pattern(regexp = "\\d{13}", message = "isbn must be exactly 13 digits")
    String isbn,

    @NotBlank(message = "title is required")
    @Size(max = 150,message = "title must be atmost 150 characters")
    String title,

    @NotBlank(message = "author is required")
    @Size(max = 100,message = "author must be atmost 100 characters")
    String author,

    @NotNull(message = "totalCopies is required")
    @Positive(message = "totalCopies must be at least 1")
    @Max(value = 1000, message = "totalCopies must be at most 1000")
    Integer totalCopies
) {
    
}
