package com.acme.library.common;

public class UnprocessableRequestException extends RuntimeException {
    private final ErrorCode code;

    public UnprocessableRequestException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
    
}
