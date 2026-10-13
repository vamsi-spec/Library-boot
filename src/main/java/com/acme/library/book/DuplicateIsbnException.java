package com.acme.library.book;

import com.acme.library.common.BusinessRuleException;
import com.acme.library.common.ErrorCode;

public class DuplicateIsbnException extends BusinessRuleException {

    public DuplicateIsbnException(String isbn) {
        super(ErrorCode.ISBN_ALREADY_EXISTS, "Book with ISBN " + isbn + " already exists");
    }
}
