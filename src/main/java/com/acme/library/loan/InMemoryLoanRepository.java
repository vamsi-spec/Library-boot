package com.acme.library.loan;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

class InMemortLoanRepository implements LoanRepository {
    private final Map<Long, Loan> loans = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public Loan insert(Loan loan) {
        long id = nextId.getAndIncrement();
        Loan newLoan = new Loan(id, loan.bookId(), loan.memberId(), loan.borrowedOn(), loan.dueDate(), loan.returnedOn(), loan.renewals());
        loans.put(id, newLoan);
        return newLoan;
    }

    public Optional<Loan> findByID(long id) {
        return Optional.ofNullable(loans.get(id));
    }

    public List<Loan> findAll() {
        return loans.values().stream().sorted(Comparator.comparingLong(Loan :: id)).toList();
    }

    public List<Loan> findActvieMemberById(long memberId) {
        return loans.values().stream().filter(loan -> loan.memberId() == memberId && loan.isActive()).sorted(Comparator.comparingLong(Loan :: id)).toList();
    }


}
