package com.acme.library.loan;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.acme.library.common.ApiPaths;
import com.acme.library.common.PageResponse;

import jakarta.validation.Valid;



@RestController
@RequestMapping(ApiPaths.v1 + "/loans")
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

    @PostMapping("/{id}/return")
    public LoanResponse returnLoan(@PathVariable long id) {
        return LoanResponse.from(loanService.returnLoan(id), LocalDate.now(clock));
    }

    @GetMapping
    public PageResponse<LoanResponse> list(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort) {
        LocalDate today = LocalDate.now(clock);
        return loanService.search(memberId, bookId, status, overdue, page, size, sort)
                .map(loan -> LoanResponse.from(loan, today));
    }

    @GetMapping 
    public List<LoanResponse> list(
        @RequestParam(required = false) Long memberId,
        @RequestParam(required = false) Long bookId,
        @RequestParam(required = false) LoanStatus status,
        @RequestParam(required = false) Boolean overdue
    ) {
        LocalDate today = LocalDate.now(clock);
        return loanService.search(memberId, bookId, status, overdue).stream().map(loan -> LoanResponse.from(loan, today)).toList();

    }

    @PostMapping("/{id}/renew")
    public LoanResponse renew(@PathVariable long id) {
        return LoanResponse.from(loanService.renew(id), LocalDate.now(clock));
    }
}
