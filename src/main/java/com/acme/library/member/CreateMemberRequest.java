package com.acme.library.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMemberRequest(
    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "name must be atmost 100 characterss")
    String name,

    @NotBlank(message = "email must not be blank")
    @Size(max = 255, message = "email must be atmost 255 characterss")
    @Email (message = "Email must not be a valid")
    String email
) {}
