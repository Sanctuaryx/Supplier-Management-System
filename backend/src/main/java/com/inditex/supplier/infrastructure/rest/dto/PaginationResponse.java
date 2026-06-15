package com.inditex.supplier.infrastructure.rest.dto;

public record PaginationResponse(Integer limit, Integer offset, Integer total) {}
