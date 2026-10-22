package com.acme.library.loan;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.acme.library.book.BookService;
import com.acme.library.common.BusinessRuleException;
import com.acme.library.common.ErrorCode;
import com.acme.library.common.PageQuery;
import com.acme.library.common.PageResponse;
import com.acme.library.common.Paging;
import com.acme.library.common.ResourceNotFoundException;
import com.acme.library.config.LibraryProperties;
import com.acme.library.member.MemberService;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final BookService bookService;
    private final MemberService memberService;
    private final LibraryProperties libraryProperties;
    private final Clock clock;

    public LoanService(LoanRepository loanRepository, BookService bookService, MemberService memberService, LibraryProperties libraryProperties, Clock clock) {
        this.loanRepository = loanRepository;
        this.bookService = bookService;
        this.memberService = memberService;
        this.libraryProperties = libraryProperties;
        this.clock = clock;
    }

    public Loan borrow(LoanRequest request) {
        long memberId = request.memberId();
        long bookId = request.bookId();
        LocalDate today = LocalDate.now(clock);

        boolean hasOverdue = loanRepository.findActvieMemberById(memberId).stream().anyMatch(loan -> loan.isOverDue(today));

        if (hasOverdue) {
            throw new BusinessRuleException(ErrorCode.MEMBER_HAS_OVERDUE_LOANS,
                    "Member " + memberId + " has overdue loans and cannot borrow");
        }

        memberService.reserveLoanSlot(memberId, libraryProperties.loan().maxActivePerMember());

        try {
            bookService.reserveCopy(bookId);
        } catch (RuntimeException e) {
            undo(e, () -> memberService.releaseLoanSlot(memberId));
            throw e;
        }

        try {
            Loan loan = new Loan(0, bookId, memberId, today, today.plusDays(libraryProperties.loan().periodDays()), null, 0, null);
            return loanRepository.insert(loan);
        } catch (RuntimeException e) {
            undo(e, () -> bookService.releaseCopy(bookId));
            undo(e, () -> memberService.releaseLoanSlot(memberId));
            throw e;
        }
    }

    public Loan returnLoan(long loanId) {
        LocalDate today = LocalDate.now(clock);

        BigDecimal feePerDay = libraryProperties.loan().lateFeePerDay();

        Loan closed = loanRepository.update(loanId, current -> {
            if(!current.isActive()) {
                throw new BusinessRuleException(ErrorCode.LOAN_ALREADY_RETURNED, "Loan " + loanId + " has already been returned");
            }
            return current.markReturned(today, lateFee(current.dueDate(), today, feePerDay));
        }).orElseThrow(() -> new ResourceNotFoundException("Loan", loanId));

        bookService.releaseCopy(closed.bookId());
        memberService.releaseLoanSlot(closed.memberId());

        return closed;
    }

    private static BigDecimal lateFee(LocalDate dueDate, LocalDate returnedOn, BigDecimal perDay) {
        long overdueDays = Math.max(0, ChronoUnit.DAYS.between(dueDate, returnedOn));
        return perDay.multiply(BigDecimal.valueOf(overdueDays)).setScale(2, RoundingMode.HALF_UP);
    }

    public Loan findById(long id) {
        return loanRepository.findByID(id).orElseThrow(() -> new ResourceNotFoundException("Loan", id));
    }

    public List<Loan> search(Long memberId, Long bookId, LoanStatus status, Boolean overdue) {
        LocalDate today = LocalDate.now(clock);
        return loanRepository.findAll().stream().filter(loan -> memberId == null || loan.memberId() == memberId)
            .filter(loan -> bookId == null || loan.bookId() == bookId)
            .filter(loan -> status == null || loan.status() == status)
            .filter(loan -> overdue == null || loan.isOverDue(today))
            .toList();
    }

        private static final Map<String, Comparator<Loan>> SORTS = Map.of(
            "id", Comparator.comparingLong(Loan::id),
            "borrowedOn", Comparator.comparing(Loan::borrowedOn),
            "dueDate", Comparator.comparing(Loan::dueDate),
            "memberId", Comparator.comparingLong(Loan::memberId),
            "bookId", Comparator.comparingLong(Loan::bookId));

    public PageResponse<Loan> search(Long memberId, Long bookId, LoanStatus status, Boolean overdue,
                                     int page, int size, List<String> sort) {
        PageQuery query = PageQuery.of(page, size, sort, SORTS.keySet(), "-id");
        LocalDate today = LocalDate.now(clock);

        List<Loan> matches = loanRepository.findAll().stream()
                .filter(loan -> memberId == null || loan.memberId() == memberId)
                .filter(loan -> bookId == null || loan.bookId() == bookId)
                .filter(loan -> status == null || loan.status() == status)
                .filter(loan -> overdue == null || loan.isOverDue(today) == overdue)
                .toList();
        return Paging.paginate(matches, query, SORTS);
    }

    private void undo(RuntimeException original, Runnable compensation) {
        try {
            compensation.run();
        } catch (RuntimeException undoFailure) {
            original.addSuppressed(undoFailure);
        }
    }

    public Loan renew(long loanId) {
        LocalDate today = LocalDate.now(clock);
        int maxRenewals = libraryProperties.loan().maxRenewals();
        int renewalDays = libraryProperties.loan().renewalDays();

        return loanRepository.update(loanId, current -> {
            if(!current.isActive()) {
                throw new BusinessRuleException(ErrorCode.RENEWAL_NOT_ALLOWED,
                                "Loan " + loanId + " was already returned and cannot be renewed");
            }
            if(current.isOverDue(today)) {
                throw new BusinessRuleException(ErrorCode.RENEWAL_NOT_ALLOWED,
                                "Loan " + loanId + " is overdue (due " + current.dueDate()
                                        + ") and cannot be renewed");
            }

            if (current.renewals() >= maxRenewals) {
                        throw new BusinessRuleException(ErrorCode.RENEWAL_NOT_ALLOWED,
                                "Loan " + loanId + " has already been renewed " + current.renewals()
                                        + " time(s) (maximum " + maxRenewals + ")");
                    }
                    return current.markRenewed(current.dueDate().plusDays(renewalDays));
        })
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId));
    }
}
