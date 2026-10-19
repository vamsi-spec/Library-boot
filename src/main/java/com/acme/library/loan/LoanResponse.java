package com.acme.library.loan;

import java.time.LocalDate;

public record LoanResponse(
    long id,
    long bookId,
    long memberId,
    LocalDate borrowedOn,
    LocalDate dueDate,
    LocalDate returnedOn,
    int renewals,
    LoanStatus status,
    boolean overdue
) {
    public static LoanResponse from (Loan loan, LocalDate today) {
        return new LoanResponse(
            loan.id(),
            loan.bookId(),
            loan.memberId(),
            loan.borrowedOn(),
            loan.dueDate(),
            loan.returnedOn(),
            loan.renewals(),
            loan.status(),
            loan.isOverDue(today)
        );
    }
}
