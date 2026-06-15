package com.inditex.supplier.infrastructure.rest.mapper;

import com.inditex.supplier.domain.model.PotentialSupplier;
import com.inditex.supplier.domain.model.PotentialSuppliersPage;
import com.inditex.supplier.domain.model.Supplier;
import com.inditex.supplier.domain.model.SupplierStatus;
import com.inditex.supplier.infrastructure.rest.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SupplierApiMapper {

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getDuns().value(),
                supplier.getName(),
                supplier.getCountry().code(),
                supplier.getAnnualTurnover(),
                toApiStatus(supplier.getStatus()),
                supplier.getRating().name()
        );
    }

    public PotentialSuppliersResponse toResponse(PotentialSuppliersPage page) {
        List<PotentialSupplierResponse> data = page.suppliers().stream()
                .map(this::toPotentialResponse)
                .toList();
        PaginationResponse pagination = new PaginationResponse(page.limit(), page.offset(), page.total());
        return new PotentialSuppliersResponse(data, pagination);
    }

    private PotentialSupplierResponse toPotentialResponse(PotentialSupplier ps) {
        return new PotentialSupplierResponse(
                ps.duns().value(),
                ps.name(),
                ps.country().code(),
                ps.annualTurnover(),
                toApiStatus(ps.status()),
                ps.rating().name(),
                ps.score()
        );
    }

    private String toApiStatus(SupplierStatus status) {
        return status == SupplierStatus.DISQUALIFIED ? "Disqualified" : "Active";
    }
}
