package com.achal.jobportal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.util.Map;

// The JSON body every error response uses, so clients can handle all errors the same way:
// {
//   "timestamp": "2026-11-01T10:15:30",
//   "status": 400,
//   "error": "Bad Request",
//   "message": "Validation failed",
//   "path": "/api/users/create",
//   "fieldErrors": { "email": "Email must be valid" }   <- only present for validation errors
// }
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {

    public static ApiErrorResponse of(HttpStatusCode status, String message, String path,
                                      Map<String, String> fieldErrors) {
        HttpStatus known = HttpStatus.resolve(status.value());
        String error = known != null ? known.getReasonPhrase() : "Error";
        return new ApiErrorResponse(LocalDateTime.now(), status.value(), error, message, path, fieldErrors);
    }
}
