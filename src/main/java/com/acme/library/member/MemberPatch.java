package com.acme.library.member;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberPatch(
    @Pattern(regexp = ".*\\S.*", message = "name must not be blank")
    @Size(max = 100, message = "name must be at most 100 characters")
    String name,

    MemberStatus status
) {}
