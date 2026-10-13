package com.acme.library.member;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface MemberRepository {
    Member insert(Member member);

    Optional<Member> findById(long id);

    List<Member> findAll();

    Optional<Member> update(long id, UnaryOperator<Member> change);

    Optional<Member> deleteById(long id, Consumer<Member> guard);

}
