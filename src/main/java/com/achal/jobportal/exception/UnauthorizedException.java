package com.achal.jobportal.exception;

import org.springframework.http.HttpStatus;

// 401 - the caller could not be identified (e.g. wrong email or password at login).
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
