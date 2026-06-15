package com.inditex.supplier.infrastructure.rest.dto;

import jakarta.validation.constraints.*;

public record CreateCandidateRequest(
        @NotNull @Min(100000000) @Max(999999999) Integer duns,
        @NotBlank String name,
        @NotNull @Size(min = 2, max = 2) String country,
        @NotNull @Min(0) Long annualTurnover
) {}
