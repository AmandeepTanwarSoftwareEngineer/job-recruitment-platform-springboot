package com.achal.jobportal.exception;

import org.springframework.http.HttpStatus;

// Base class for errors we throw on purpose. The GlobalExceptionHandler turns any
// ApiException into a JSON error with this status, so a new error type only needs
// a subclass (or `throw new ApiException(HttpStatus.FORBIDDEN, "...")`).
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
