package com.inditex.supplier.domain.model;

import java.util.List;

public record PotentialSuppliersPage(
        List<PotentialSupplier> suppliers,
        int total,
        int limit,
        int offset
) {}
