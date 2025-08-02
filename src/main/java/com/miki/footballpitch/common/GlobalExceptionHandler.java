package com.miki.footballpitch.common;

import com.miki.footballpitch.booking.model.ValidationErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = ValidationErrorResponse.of(
                "Validation failed", errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = ValidationErrorResponse.of(
                "Validation failed", errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponse> handleBusinessLogicException(IllegalArgumentException ex) {
        String message = ex.getMessage();

        // Business logic exceptions that should return 404
        if (isNotFoundError(message)) {
            ValidationErrorResponse errorResponse = ValidationErrorResponse.of(
                    "Resource not found", List.of(message));
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // Other business rule violations return 400
        ValidationErrorResponse errorResponse = ValidationErrorResponse.of(
                "Business rule violation", List.of(message));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private boolean isNotFoundError(String message) {
        return message != null && (
                message.contains("not found") ||
                        message.contains("does not exist") ||
                        message.contains("Player not found") ||
                        message.contains("Booking not found") ||
                        message.contains("Pitch not found")
        );
    }
}