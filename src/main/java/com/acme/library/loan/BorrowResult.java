package com.acme.library.loan;

public record BorrowResult(Loan loan, boolean replayed) {}