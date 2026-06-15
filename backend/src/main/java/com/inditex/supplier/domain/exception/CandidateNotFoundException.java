package com.inditex.supplier.domain.exception;

public class CandidateNotFoundException extends RuntimeException {
    public CandidateNotFoundException(int duns) {
        super("Candidate not found for DUNS: " + duns);
    }
}
