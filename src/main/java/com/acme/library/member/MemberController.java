package com.acme.library.member;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/members")
public class MemberController {
    private MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> list() {
        return memberService.findAll().stream().map(MemberResponse :: from).toList();
    }

    @GetMapping("/{id}")
    public MemberResponse get(@PathVariable long id) {
        return MemberResponse.from(memberService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody CreateMemberRequest request) {
        Member created = memberService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();

        return ResponseEntity.created(location).body(MemberResponse.from(created));
    }

    @PutMapping("/{id}")
    public MemberResponse patch(@PathVariable long id, @Valid @RequestBody MemberPatch patch) {
        return MemberResponse.from(memberService.patch(id, patch));
    }

    @PatchMapping("/{id}")
    public MemberResponse replace(@PathVariable long id, @Valid @RequestBody UpdateMemberRequest request) {
        return MemberResponse.from(memberService.replace(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        memberService.delete(id);
    }

}
