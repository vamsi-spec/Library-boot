package com.acme.library.member;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.acme.library.common.BusinessRuleException;
import com.acme.library.common.ErrorCode;
import com.acme.library.common.ResourceNotFoundException;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final Clock clock;

    public MemberService(MemberRepository memberRepository, Clock clock) {
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public Member create(CreateMemberRequest request) {
        Member member = new Member(0L, request.name().strip(), request.email().toLowerCase(Locale.ROOT), MemberStatus.ACTIVE, 0, Instant.now(clock));
        try {
            return memberRepository.insert(member);
        } catch (DuplicateEmailException e) {
            throw new BusinessRuleException(ErrorCode.EMAIL_ALREADY_EXISTS, e.getMessage());
        }
    }

    public Member findById(long id) {
        return memberRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member replace(long id, UpdateMemberRequest request) {
        return memberRepository.update(id, current -> new Member(current.id(), request.name().strip(), current.email(), request.status(), current.activeLoans(), current.joinedAt())).orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }

    public Member patch(long id, MemberPatch patch) {
        return memberRepository.update(id, current -> new Member(current.id(),patch.name() != null ? patch.name().strip() : current.name(), current.email(), patch.status() != null ? patch.status() : current.status(), current.activeLoans(), current.joinedAt())).orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }

    public void delete(long id) {
        memberRepository.deleteById(id, member -> {
            if(member.activeLoans() > 0) {
                throw new BusinessRuleException(ErrorCode.ACTIVE_LOANS_EXIST, "Member has active loans");
            }
        }).orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }

    public Member reserveLoanSlot(long memberId, int maxActiveLoans) {
        return memberRepository.update(memberId, current -> {
            if(!current.isActive()) {
                throw new BusinessRuleException(ErrorCode.MEMBER_SUSPENDED, "Member "+ memberId +" is Suspended");
            }
            if(current.activeLoans() >= maxActiveLoans) {
                throw new BusinessRuleException(ErrorCode.LOAN_LIMIT_REACHED, "Member "+ memberId +" has reached maximum loan limit");
            }

            return new Member(current.id(), current.name(), current.email(), current.status(), current.activeLoans() + 1, current.joinedAt());
        }).orElseThrow(() -> new ResourceNotFoundException("Member", memberId));
    }

    public Member releaseLoanSlot(long memberId) {
        return memberRepository.update(memberId, current -> new Member(current.id(), current.name(),current.email(), current.status(), current.activeLoans() - 1, current.joinedAt())).orElseThrow(() -> new IllegalStateException("Member " + memberId + "disappeared while one of its loans was being returned"));
    }
}
