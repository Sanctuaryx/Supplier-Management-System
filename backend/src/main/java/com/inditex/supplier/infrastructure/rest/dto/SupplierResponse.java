package com.inditex.supplier.infrastructure.rest.dto;

public record SupplierResponse(
        Integer duns,
        String name,
        String country,
        Long annualTurnover,
        String status,
        String sustainabilityRating
) {}
