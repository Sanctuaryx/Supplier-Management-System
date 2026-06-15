package com.inditex.supplier.domain.model;

public record PotentialSupplier(
        Duns duns,
        String name,
        Country country,
        long annualTurnover,
        SustainabilityRating rating,
        SupplierStatus status,
        double score
) {}
