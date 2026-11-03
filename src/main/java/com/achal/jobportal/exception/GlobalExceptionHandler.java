package com.achal.jobportal.exception;

import com.achal.jobportal.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

// Catches exceptions thrown from any controller (or the services they call) and turns them
// into a consistent ApiErrorResponse JSON body with the right HTTP status.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Our own exceptions: ResourceNotFoundException (404), DuplicateResourceException (409),
    // BadRequestException (400), or any ApiException with a custom status.
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getMessage(), request, null);
    }

    // @Valid failed on a @RequestBody -> 400 with one message per invalid field.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // A field can fail more than one rule; keep all the messages.
            fieldErrors.merge(error.getField(), error.getDefaultMessage(), (a, b) -> a + "; " + b);
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors);
    }

    // Body is missing, is not valid JSON, or has a wrong value type (e.g. "role": "ADMIN123").
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", request, null);
    }

    // A path variable or query param has the wrong type (e.g. /api/users/abc when an id is expected).
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest request) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "a valid value";
        String message = "Parameter '" + ex.getName() + "' must be of type " + expected;
        return build(HttpStatus.BAD_REQUEST, message, request, null);
    }

    // A database constraint was hit, e.g. two requests registering the same email at the same time
    // both pass the existsByEmail check. The DB message is logged, not sent, since it exposes schema details.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        log.warn("Data integrity violation on {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Request conflicts with existing data", request, null);
    }

    // @PreAuthorize said no (e.g. a JOB_SEEKER calling a RECRUITER-only endpoint) -> 403.
    // Without this, the catch-all below would turn it into a 500.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to do this", request, null);
    }

    // Everything else.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAny(Exception ex, HttpServletRequest request) {
        // Spring's own web exceptions (wrong HTTP method -> 405, unknown URL -> 404,
        // unsupported Content-Type -> 415, ...) already know their status; keep it.
        if (ex instanceof ErrorResponse springError && springError.getStatusCode().is4xxClientError()) {
            return build(springError.getStatusCode(), springError.getBody().getDetail(), request, null);
        }

        // A real bug. Log the full stack trace, but don't leak internals to the client.
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.", request, null);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatusCode status, String message,
                                                   HttpServletRequest request, Map<String, String> fieldErrors) {
        ApiErrorResponse body = ApiErrorResponse.of(status, message, request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
