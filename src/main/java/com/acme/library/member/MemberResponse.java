package com.acme.library.member;

public record MemberResponse(long id, String name, String email, MemberStatus status, int activeLoans) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.id(), member.name(), member.email(), member.status(), member.activeLoans());
    }
}
