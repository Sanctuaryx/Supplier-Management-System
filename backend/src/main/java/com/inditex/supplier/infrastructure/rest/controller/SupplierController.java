package com.inditex.supplier.infrastructure.rest.controller;

import com.inditex.supplier.application.service.SupplierService;
import com.inditex.supplier.infrastructure.rest.dto.PotentialSuppliersResponse;
import com.inditex.supplier.infrastructure.rest.dto.SupplierResponse;
import com.inditex.supplier.infrastructure.rest.mapper.SupplierApiMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
@Validated
public class SupplierController {

    private final SupplierService supplierService;
    private final SupplierApiMapper mapper;

    public SupplierController(SupplierService supplierService, SupplierApiMapper mapper) {
        this.supplierService = supplierService;
        this.mapper = mapper;
    }

    @GetMapping("/potential")
    public PotentialSuppliersResponse getPotentialSuppliers(
            @RequestParam @Min(250) long rate,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return mapper.toResponse(supplierService.getPotentialSuppliers(rate, limit, offset));
    }

    @GetMapping("/{duns}")
    public SupplierResponse getSupplier(
            @PathVariable @Min(100000000) @Max(999999999) int duns) {
        return mapper.toResponse(supplierService.getSupplier(duns));
    }

    @PostMapping("/{duns}/ban")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void banSupplier(
            @PathVariable @Min(100000000) @Max(999999999) int duns) {
        supplierService.banSupplier(duns);
    }
}
