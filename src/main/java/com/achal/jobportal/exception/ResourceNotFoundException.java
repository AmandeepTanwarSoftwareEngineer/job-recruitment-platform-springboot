package com.achal.jobportal.exception;

import org.springframework.http.HttpStatus;

// 404 - the thing the client asked for does not exist.
// Example: throw new ResourceNotFoundException("User", "id", id);
//          -> "User not found with id: 5"
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        this(resource + " not found with " + field + ": " + value);
    }
}
