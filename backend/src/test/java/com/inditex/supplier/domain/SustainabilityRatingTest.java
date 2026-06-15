package com.inditex.supplier.domain;

import com.inditex.supplier.domain.model.SustainabilityRating;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SustainabilityRatingTest {

    @ParameterizedTest
    @CsvSource({"A,1.0", "B,0.75", "C,0.5", "D,0.25", "E,0.1"})
    void ratingConstants(SustainabilityRating rating, double expected) {
        assertThat(rating.toRatingConstant()).isCloseTo(expected, within(0.001));
    }

    @Test
    void aAndBLeadToActivation() {
        assertThat(SustainabilityRating.A.leadsToActivation()).isTrue();
        assertThat(SustainabilityRating.B.leadsToActivation()).isTrue();
    }

    @Test
    void cDEDoNotLeadToActivation() {
        assertThat(SustainabilityRating.C.leadsToActivation()).isFalse();
        assertThat(SustainabilityRating.D.leadsToActivation()).isFalse();
        assertThat(SustainabilityRating.E.leadsToActivation()).isFalse();
    }
}
