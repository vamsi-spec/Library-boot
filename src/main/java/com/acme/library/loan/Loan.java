package com.acme.library.loan;

import java.time.LocalDate;
import java.util.Objects;

public record Loan(
    long id,
    long bookId,
    long memberId,
    LocalDate borrowedOn,
    LocalDate dueDate,
    LocalDate returnedOn,
    int renewals 
) {
    public Loan {
        Objects.requireNonNull(borrowedOn,"borrowedOn");
        Objects.requireNonNull(dueDate,"dueDate");
        if(dueDate.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("dueDate must not be before borrowedOn");
        }

        if(returnedOn != null && returnedOn.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("returnedOn must not be before borrowedOn");
        }

        if(renewals < 0) {
            throw new IllegalArgumentException("renewals must be non-negative");
        }
    }

    public boolean isActive() {
        return returnedOn == null;
    }

    public boolean isOverDue(LocalDate today) {
        return isActive() && today.isAfter(dueDate);
    }

    public LoanStatus status() {
        return isActive() ? LoanStatus.ACTIVE : LoanStatus.RETURNED;
    }

    
}
