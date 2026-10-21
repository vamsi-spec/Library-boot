package com.acme.library.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Loan(
    long id,
    long bookId,
    long memberId,
    LocalDate borrowedOn,
    LocalDate dueDate,
    LocalDate returnedOn,
    int renewals,
    BigDecimal lateFee
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

        if ((returnedOn == null) != (lateFee == null)) {
            throw new IllegalArgumentException("a loan has a lateFee exactly when it has been returned");
        }
        if (lateFee != null && lateFee.signum() < 0) {
            throw new IllegalArgumentException("lateFee must not be negative");
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

    public Loan markReturned(LocalDate returnDate, BigDecimal fee) {
        return new Loan(id, bookId, memberId, borrowedOn, dueDate, returnDate, renewals, fee);
    }

    public Loan markRenewed(LocalDate newDueDate) {
        return new Loan(id, bookId, memberId, borrowedOn, newDueDate, returnedOn, renewals + 1, lateFee);
    }

    
}
