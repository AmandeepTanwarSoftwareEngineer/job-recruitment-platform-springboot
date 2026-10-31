package com.achal.jobportal.exception;

import org.springframework.http.HttpStatus;

// 400 - the request is well-formed but breaks a business rule
// (e.g. "Cannot apply to a closed job"). Field validation errors are handled separately.
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
