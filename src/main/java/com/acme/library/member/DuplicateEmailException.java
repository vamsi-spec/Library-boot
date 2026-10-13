package com.acme.library.member;

import com.acme.library.common.BusinessRuleException;
import com.acme.library.common.ErrorCode;

public class DuplicateEmailException extends BusinessRuleException {
    public DuplicateEmailException(String email) {
        super(ErrorCode.EMAIL_ALREADY_EXISTS, "Email " + email + " already exists");
    }
}
