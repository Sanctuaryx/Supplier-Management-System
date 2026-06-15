package com.inditex.supplier.application;

import com.inditex.supplier.application.service.SupplierService;
import com.inditex.supplier.domain.exception.InvalidSupplierStateException;
import com.inditex.supplier.domain.exception.SupplierNotFoundException;
import com.inditex.supplier.domain.model.*;
import com.inditex.supplier.domain.port.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock SupplierRepository supplierRepository;

    SupplierService service;

    private static final int DUNS = 123456789;

    @BeforeEach
    void setUp() {
        service = new SupplierService(supplierRepository);
    }

    @Test
    void banSupplier_happyPath_bansOnProbationSupplier() {
        Supplier supplier = new Supplier(new Duns(DUNS), "T", new Country("ES"),
                2_000_000L, SustainabilityRating.C, SupplierStatus.ON_PROBATION);
        when(supplierRepository.findByDuns(any())).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.banSupplier(DUNS);

        verify(supplierRepository).save(argThat(Supplier::isDisqualified));
    }

    @Test
    void banSupplier_throwsNotFound_whenSupplierAbsent() {
        when(supplierRepository.findByDuns(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.banSupplier(DUNS))
                .isInstanceOf(SupplierNotFoundException.class);
    }

    @Test
    void banSupplier_throwsInvalidState_whenActiveSupplier() {
        Supplier supplier = new Supplier(new Duns(DUNS), "T", new Country("ES"),
                2_000_000L, SustainabilityRating.A, SupplierStatus.ACTIVE);
        when(supplierRepository.findByDuns(any())).thenReturn(Optional.of(supplier));

        assertThatThrownBy(() -> service.banSupplier(DUNS))
                .isInstanceOf(InvalidSupplierStateException.class);
    }
}
