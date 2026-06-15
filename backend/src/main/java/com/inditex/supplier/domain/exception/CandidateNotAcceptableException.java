package com.inditex.supplier.domain.exception;

public class CandidateNotAcceptableException extends RuntimeException {
    public CandidateNotAcceptableException(String reason) {
        super(reason);
    }
}
