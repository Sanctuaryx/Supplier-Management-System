package com.inditex.supplier.domain.exception;

public class SupplierBannedException extends RuntimeException {
    public SupplierBannedException() {
        super("Supplier banned");
    }
}
