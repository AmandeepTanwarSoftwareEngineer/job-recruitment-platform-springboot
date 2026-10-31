package com.achal.jobportal.exception;

import org.springframework.http.HttpStatus;

// 409 - the request clashes with data that already exists (e.g. an email already registered).
public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
