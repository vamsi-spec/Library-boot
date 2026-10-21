package com.acme.library.loan;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface LoanRepository {
    
    Loan insert(Loan loan);

    Optional<Loan> findByID(long id);

    List<Loan> findAll();

    List<Loan> findActvieMemberById(long memberId);

    Optional<Loan> update(long id, UnaryOperator<Loan> change);
}
