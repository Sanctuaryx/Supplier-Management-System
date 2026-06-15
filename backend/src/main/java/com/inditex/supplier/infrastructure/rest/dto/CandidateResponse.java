package com.inditex.supplier.infrastructure.rest.dto;

public record CandidateResponse(
        Integer duns,
        String name,
        String country,
        Long annualTurnover
) {}
