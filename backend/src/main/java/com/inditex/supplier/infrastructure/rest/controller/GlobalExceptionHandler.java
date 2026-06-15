package com.inditex.supplier.infrastructure.rest.controller;

import com.inditex.supplier.domain.exception.*;
import com.inditex.supplier.infrastructure.rest.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CandidateNotFoundException.class, SupplierNotFoundException.class})
    public ResponseEntity<Void> handleNotFound(RuntimeException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({CandidateAlreadyExistsException.class, SupplierBannedException.class,
            InvalidCandidateStateException.class, InvalidSupplierStateException.class,
            CandidateNotAcceptableException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnprocessable(CountryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed: " + ex.getMessage()));
    }
}
