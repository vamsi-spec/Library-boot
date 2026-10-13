package com.acme.library.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMemberRequest(
    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "name must be atmost 100 characters")
    String name,

    @NotNull(message = "Status should be provided")
    MemberStatus status
) {}
