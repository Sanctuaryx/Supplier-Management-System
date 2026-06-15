package com.inditex.supplier.domain.exception;

public class CandidateAlreadyExistsException extends RuntimeException {
    public CandidateAlreadyExistsException() {
        super("Candidate already exists");
    }
}
