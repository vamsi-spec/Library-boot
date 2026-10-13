package com.acme.library.member;

import java.time.Instant;

public record Member(long id,
    String name,
    String email,
    MemberStatus status,
    int activeLoans,
    Instant joinedAt) {
    public Member {
        if(activeLoans < 0) {
            throw new IllegalArgumentException("activeLoans must be non-negative");
        }
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }
}
