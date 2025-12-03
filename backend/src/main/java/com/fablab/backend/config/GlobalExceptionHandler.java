package com.fablab.backend.config;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralizes exception handling for REST controllers so that API consumers
 * receive consistent error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Converts bean-validation failures into a {@code 400 Bad Request} response.
     *
     * @param ex the validation exception raised when request payload constraints
     *           are violated
     * @return a response highlighting the first detected validation issue
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body("Validation error: " + ex.getBindingResult().getFieldError().getDefaultMessage());
    }

    /**
     * Logs unexpected errors and returns a {@code 500 Internal Server Error}
     * response that surfaces the root cause message when available.
     *
     * @param ex the unhandled exception bubbling up from the request pipeline
     * @return a generic server error response suitable for clients
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllOtherExceptions(Exception ex) {
        log.error("Unexpected error", ex);
        String rootMessage = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        return ResponseEntity.internalServerError()
                .body("An error occurred: " + rootMessage);
    }
}
