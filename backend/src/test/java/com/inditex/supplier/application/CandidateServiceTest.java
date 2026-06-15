package com.inditex.supplier.application;

import com.inditex.supplier.application.service.CandidateService;
import com.inditex.supplier.domain.exception.CandidateAlreadyExistsException;
import com.inditex.supplier.domain.exception.CandidateNotAcceptableException;
import com.inditex.supplier.domain.exception.CandidateNotFoundException;
import com.inditex.supplier.domain.exception.CountryNotFoundException;
import com.inditex.supplier.domain.exception.SupplierBannedException;
import com.inditex.supplier.domain.model.*;
import com.inditex.supplier.domain.port.CandidateRepository;
import com.inditex.supplier.domain.port.CountryService;
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
class CandidateServiceTest {

    @Mock CandidateRepository candidateRepository;
    @Mock SupplierRepository supplierRepository;
    @Mock CountryService countryService;

    CandidateService service;

    private static final int DUNS = 123456789;

    @BeforeEach
    void setUp() {
        service = new CandidateService(candidateRepository, supplierRepository, countryService);
    }

    private Candidate pendingCandidate(String country, long turnover) {
        return Candidate.newApplication(new Duns(DUNS), "Test", new Country(country), turnover);
    }

    // --- createCandidate ---

    @Test
    void createCandidate_happyPath_createsPendingCandidate() {
        when(supplierRepository.findByDuns(any())).thenReturn(Optional.empty());
        when(candidateRepository.existsActiveCandidacyForDuns(any())).thenReturn(false);
        when(candidateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createCandidate(DUNS, "Test", "ES", 2_000_000L);

        verify(candidateRepository).save(any(Candidate.class));
    }

    @Test
    void createCandidate_throwsSupplierBanned_whenDisqualifiedSupplierExists() {
        Supplier banned = new Supplier(new Duns(DUNS), "T", new Country("ES"),
                2_000_000L, SustainabilityRating.C, SupplierStatus.DISQUALIFIED);
        when(supplierRepository.findByDuns(any())).thenReturn(Optional.of(banned));

        assertThatThrownBy(() -> service.createCandidate(DUNS, "Test", "ES", 2_000_000L))
                .isInstanceOf(SupplierBannedException.class);
    }

    @Test
    void createCandidate_throwsCandidateAlreadyExists_whenSupplierExistsAndNotBanned() {
        Supplier active = new Supplier(new Duns(DUNS), "T", new Country("ES"),
                2_000_000L, SustainabilityRating.A, SupplierStatus.ACTIVE);
        when(supplierRepository.findByDuns(any())).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.createCandidate(DUNS, "Test", "ES", 2_000_000L))
                .isInstanceOf(CandidateAlreadyExistsException.class);
    }

    @Test
    void createCandidate_throwsCandidateAlreadyExists_whenActiveCandidacyExists() {
        when(supplierRepository.findByDuns(any())).thenReturn(Optional.empty());
        when(candidateRepository.existsActiveCandidacyForDuns(any())).thenReturn(true);

        assertThatThrownBy(() -> service.createCandidate(DUNS, "Test", "ES", 2_000_000L))
                .isInstanceOf(CandidateAlreadyExistsException.class);
    }

    @Test
    void createCandidate_throwsCountryNotFound_whenCountryUnknown() {
        doThrow(new CountryNotFoundException("XX")).when(countryService).isBanned(any());

        assertThatThrownBy(() -> service.createCandidate(DUNS, "Test", "XX", 2_000_000L))
                .isInstanceOf(CountryNotFoundException.class);
    }

    // --- acceptCandidate ---

    @Test
    void acceptCandidate_happyPath_ratingA_createsActiveSupplier() {
        Candidate candidate = pendingCandidate("ES", 2_000_000L);
        when(candidateRepository.findByDuns(any())).thenReturn(Optional.of(candidate));
        when(countryService.isBanned(any())).thenReturn(false);
        when(candidateRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(supplierRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.acceptCandidate(DUNS, SustainabilityRating.A);

        verify(supplierRepository).save(argThat(s -> s.getStatus() == SupplierStatus.ACTIVE));
    }

    @Test
    void acceptCandidate_happyPath_ratingC_createsOnProbationSupplier() {
        Candidate candidate = pendingCandidate("ES", 2_000_000L);
        when(candidateRepository.findByDuns(any())).thenReturn(Optional.of(candidate));
        when(countryService.isBanned(any())).thenReturn(false);
        when(candidateRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(supplierRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.acceptCandidate(DUNS, SustainabilityRating.C);

        verify(supplierRepository).save(argThat(s -> s.getStatus() == SupplierStatus.ON_PROBATION));
    }

    @Test
    void acceptCandidate_throwsNotFound_whenCandidateAbsent() {
        when(candidateRepository.findByDuns(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acceptCandidate(DUNS, SustainabilityRating.A))
                .isInstanceOf(CandidateNotFoundException.class);
    }

    @Test
    void acceptCandidate_throwsNotAcceptable_whenCountryBanned() {
        Candidate candidate = pendingCandidate("ZZ", 2_000_000L);
        when(candidateRepository.findByDuns(any())).thenReturn(Optional.of(candidate));
        when(countryService.isBanned(any())).thenReturn(true);

        assertThatThrownBy(() -> service.acceptCandidate(DUNS, SustainabilityRating.A))
                .isInstanceOf(CandidateNotAcceptableException.class);
    }

    @Test
    void acceptCandidate_throwsNotAcceptable_whenTurnoverBelowMinimum() {
        Candidate candidate = pendingCandidate("ES", 999_999L);
        when(candidateRepository.findByDuns(any())).thenReturn(Optional.of(candidate));
        when(countryService.isBanned(any())).thenReturn(false);

        assertThatThrownBy(() -> service.acceptCandidate(DUNS, SustainabilityRating.A))
                .isInstanceOf(CandidateNotAcceptableException.class);
    }

    @Test
    void acceptCandidate_throwsNotAcceptable_whenCandidateNotPending() {
        Candidate candidate = pendingCandidate("ES", 2_000_000L);
        candidate.refuse();
        when(candidateRepository.findByDuns(any())).thenReturn(Optional.of(candidate));

        assertThatThrownBy(() -> service.acceptCandidate(DUNS, SustainabilityRating.A))
                .isInstanceOf(CandidateNotAcceptableException.class);
    }
}
