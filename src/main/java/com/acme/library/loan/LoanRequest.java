package com.acme.library.loan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoanRequest(@NotNull(message = "memberId is required")
    @Positive(message = "memberId must be positive")
    Long memberId,

    @NotNull(message = "bookId is required")
    @Positive(message = "bookId must be positive")
    Long bookId
) {
    
}
