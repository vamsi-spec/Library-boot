package com.acme.library.loan;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    
    Loan insert(Loan loan);

    Optional<Loan> findByID(long id);

    List<Loan> findAll();

    List<Loan> findActvieMemberById(long memberId);
}
