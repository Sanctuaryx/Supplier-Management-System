package com.inditex.supplier.domain.exception;

public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(int duns) {
        super("Supplier not found for DUNS: " + duns);
    }
}
