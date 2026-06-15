package com.inditex.supplier.infrastructure.rest.dto;

public record PotentialSupplierResponse(
        Integer duns,
        String name,
        String country,
        Long annualTurnover,
        String status,
        String sustainabilityRating,
        Double score
) {}
