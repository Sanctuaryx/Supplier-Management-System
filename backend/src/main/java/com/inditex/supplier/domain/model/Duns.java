package com.inditex.supplier.domain.model;

/**
 * Value object representing a DUNS (Data Universal Numbering System) identifier.
 *
 * <p>DUNS is a 9-digit numeric identifier used to uniquely identify a business entity.
 * Both {@link Candidate} and {@link Supplier} aggregates are keyed by this value.
 * Construction validates that {@code value} is within the valid 9-digit range.
 *
 * @param value the DUNS number; must be between {@code 100,000,000} and {@code 999,999,999} inclusive
 * @throws IllegalArgumentException if {@code value} is outside the valid range
 */
public record Duns(int value) {

    private static final int MIN = 100_000_000;
    private static final int MAX = 999_999_999;

    public Duns {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException("DUNS must be between " + MIN + " and " + MAX);
        }
    }
}
