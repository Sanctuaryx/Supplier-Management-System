package com.inditex.supplier.infrastructure.rest.dto;

import java.util.List;

public record PotentialSuppliersResponse(List<PotentialSupplierResponse> data, PaginationResponse pagination) {}
