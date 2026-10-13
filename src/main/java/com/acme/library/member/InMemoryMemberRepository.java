package com.acme.library.member;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryMemberRepository implements MemberRepository {
    private final Map<Long, Member> members = new ConcurrentHashMap<>();
    private final Map<String, Long> idByEmail = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong();

    public Member insert(Member member) {
        long id = nextId.getAndIncrement();
        if(idByEmail.putIfAbsent(member.email(), id) != null ) {
            throw new DuplicateEmailException(member.email());
        }

        Member saved = new Member(id, member.name(), member.email(), member.status(), member.activeLoans(), member.joinedAt());
        members.put(id, saved);
        return saved;
    }

    public Optional<Member> findById(long id) {
        return Optional.ofNullable(members.get(id));
    }

    public Optional<Member> update(long id, UnaryOperator<Member> change) {
        Member updated = members.computeIfPresent(id, (key, current) -> {
            Member next = change.apply(current);
            if(next.id() != current.id() || !next.email().equals(current.email())) {
                throw new IllegalStateException("id and email must not change");
            }
            return next;
        });
        return Optional.ofNullable(updated);
    }

    public Optional<Member> deleteById(long id, Consumer<Member> guard) {
        AtomicReference<Member> removed = new AtomicReference<>();
        members.computeIfPresent(id, (key, current) -> {
            guard.accept(current);
            removed.set(current);
            return null;                          
        });
        Member member = removed.get();
        if (member == null) {
            return Optional.empty();
        }
        idByEmail.remove(member.email());
        return Optional.of(member);
    }

}
