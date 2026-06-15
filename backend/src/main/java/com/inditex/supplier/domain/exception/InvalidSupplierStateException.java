package com.inditex.supplier.domain.exception;

public class InvalidSupplierStateException extends RuntimeException {
    public InvalidSupplierStateException(String message) {
        super(message);
    }
}
