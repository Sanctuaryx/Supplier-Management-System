package com.inditex.supplier.domain;

import com.inditex.supplier.domain.exception.InvalidSupplierStateException;
import com.inditex.supplier.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class SupplierTest {

    private Supplier activeSupplier() {
        return new Supplier(new Duns(123456789), "Test", new Country("ES"),
                2_000_000L, SustainabilityRating.A, SupplierStatus.ACTIVE);
    }

    private Supplier onProbationSupplier() {
        return new Supplier(new Duns(123456789), "Test", new Country("ES"),
                2_000_000L, SustainabilityRating.C, SupplierStatus.ON_PROBATION);
    }

    @Test
    void ratingABCreatesActiveSupplier() {
        Candidate candidate = Candidate.newApplication(new Duns(123456789), "T", new Country("ES"), 2_000_000L);
        candidate.accept();
        Supplier s = Supplier.from(candidate, SustainabilityRating.A);
        assertThat(s.getStatus()).isEqualTo(SupplierStatus.ACTIVE);
        Supplier s2 = Supplier.from(candidate, SustainabilityRating.B);
        assertThat(s2.getStatus()).isEqualTo(SupplierStatus.ACTIVE);
    }

    @Test
    void ratingCDECreatesOnProbationSupplier() {
        Candidate candidate = Candidate.newApplication(new Duns(123456789), "T", new Country("ES"), 2_000_000L);
        candidate.accept();
        for (SustainabilityRating r : new SustainabilityRating[]{SustainabilityRating.C, SustainabilityRating.D, SustainabilityRating.E}) {
            assertThat(Supplier.from(candidate, r).getStatus()).isEqualTo(SupplierStatus.ON_PROBATION);
        }
    }

    @Test
    void banTransitionsOnProbationToDisqualified() {
        Supplier supplier = onProbationSupplier();
        supplier.ban();
        assertThat(supplier.isDisqualified()).isTrue();
    }

    @Test
    void cannotBanActiveSupplier() {
        assertThatThrownBy(() -> activeSupplier().ban())
                .isInstanceOf(InvalidSupplierStateException.class);
    }

    @Test
    void cannotBanAlreadyDisqualified() {
        Supplier supplier = onProbationSupplier();
        supplier.ban();
        assertThatThrownBy(supplier::ban)
                .isInstanceOf(InvalidSupplierStateException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "A,1000000,100000.0,false",
        "B,1000000,75000.0,false",
        "C,1000000,50000.0,false",
        "D,1000000,25000.0,false",
        "E,1000000,10000.0,false",
        "A,1000000,125000.0,true",
    })
    void calculateScore(SustainabilityRating rating, long turnover, double expected, boolean bonus) {
        Supplier s = new Supplier(new Duns(123456789), "T", new Country("ES"), turnover, rating, SupplierStatus.ACTIVE);
        assertThat(s.calculateScore(bonus)).isCloseTo(expected, within(0.01));
    }
}
