package com.inditex.supplier.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AcceptCandidateRequest(
        @NotNull @Pattern(regexp = "[ABCDE]") String sustainabilityRating
) {}
