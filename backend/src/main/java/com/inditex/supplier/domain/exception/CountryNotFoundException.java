package com.inditex.supplier.domain.exception;

public class CountryNotFoundException extends RuntimeException {
    public CountryNotFoundException(String countryCode) {
        super("Country not found: " + countryCode);
    }
}
