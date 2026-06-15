package com.inditex.supplier.domain.model;

/**
 * Value object representing a country using the ISO 3166-1 alpha-2 standard.
 *
 * <p>Used as part of both the {@link Candidate} and {@link Supplier} aggregates.
 * The country code is normalised to uppercase on construction, so {@code "es"} and
 * {@code "ES"} are treated as equivalent.
 *
 * @param code the two-letter country code (e.g. {@code "ES"}, {@code "FR"}, {@code "DE"});
 *             must be exactly 2 characters after whitespace trimming
 * @throws IllegalArgumentException if {@code code} is {@code null} or not exactly 2 characters
 */
public record Country(String code) {

    public Country {
        if (code == null || code.length() != 2) {
            throw new IllegalArgumentException("Country code must be exactly 2 characters");
        }
        code = code.toUpperCase();
    }
}
