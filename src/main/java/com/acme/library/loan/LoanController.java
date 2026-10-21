package com.acme.library.loan;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/loans")
public class LoanController {
    private final LoanService loanService;
    private final Clock clock;

    public LoanController(LoanService loanService, Clock clock) {
        this.loanService = loanService;
        this.clock = clock;
    }

    @PostMapping
    public ResponseEntity<LoanResponse> borrow(@Valid @RequestBody LoanRequest request) {
        Loan loan = loanService.borrow(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(loan.id()).toUri();
        return ResponseEntity.created(location).body(LoanResponse.from(loan, LocalDate.now(clock)));
    }

    @GetMapping("/{id}")
    public LoanResponse get(@PathVariable long id) {
        return LoanResponse.from(loanService.findById(id), LocalDate.now(clock));
    }
}
