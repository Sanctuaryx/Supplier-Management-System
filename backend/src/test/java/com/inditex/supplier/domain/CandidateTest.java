package com.inditex.supplier.domain;

import com.inditex.supplier.domain.exception.InvalidCandidateStateException;
import com.inditex.supplier.domain.model.Candidate;
import com.inditex.supplier.domain.model.CandidateStatus;
import com.inditex.supplier.domain.model.Country;
import com.inditex.supplier.domain.model.Duns;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CandidateTest {

    private Candidate newCandidate() {
        return Candidate.newApplication(new Duns(123456789), "Test Supplier", new Country("ES"), 2_000_000L);
    }

    @Test
    void newApplicationIsAlwaysPending() {
        assertThat(newCandidate().getStatus()).isEqualTo(CandidateStatus.PENDING);
    }

    @Test
    void acceptTransitionsPendingToAccepted() {
        Candidate candidate = newCandidate();
        candidate.accept();
        assertThat(candidate.getStatus()).isEqualTo(CandidateStatus.ACCEPTED);
    }

    @Test
    void refuseTransitionsPendingToRefused() {
        Candidate candidate = newCandidate();
        candidate.refuse();
        assertThat(candidate.getStatus()).isEqualTo(CandidateStatus.REFUSED);
    }

    @Test
    void cannotAcceptAlreadyAccepted() {
        Candidate candidate = newCandidate();
        candidate.accept();
        assertThatThrownBy(candidate::accept)
                .isInstanceOf(InvalidCandidateStateException.class);
    }

    @Test
    void cannotRefuseAlreadyRefused() {
        Candidate candidate = newCandidate();
        candidate.refuse();
        assertThatThrownBy(candidate::refuse)
                .isInstanceOf(InvalidCandidateStateException.class);
    }

    @Test
    void cannotAcceptRefusedCandidate() {
        Candidate candidate = newCandidate();
        candidate.refuse();
        assertThatThrownBy(candidate::accept)
                .isInstanceOf(InvalidCandidateStateException.class);
    }
}
