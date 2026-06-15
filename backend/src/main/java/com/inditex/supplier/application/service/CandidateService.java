package com.inditex.supplier.application.service;

import com.inditex.supplier.domain.exception.CandidateAlreadyExistsException;
import com.inditex.supplier.domain.exception.CandidateNotAcceptableException;
import com.inditex.supplier.domain.exception.CandidateNotFoundException;
import com.inditex.supplier.domain.exception.CountryNotFoundException;
import com.inditex.supplier.domain.exception.SupplierBannedException;
import com.inditex.supplier.domain.model.Candidate;
import com.inditex.supplier.domain.model.Country;
import com.inditex.supplier.domain.model.Duns;
import com.inditex.supplier.domain.model.Supplier;
import com.inditex.supplier.domain.model.SustainabilityRating;
import com.inditex.supplier.domain.port.CandidateRepository;
import com.inditex.supplier.domain.port.CountryService;
import com.inditex.supplier.domain.port.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for the candidate lifecycle.
 *
 * <p>Orchestrates the four operations a candidate can go through — creation,
 * retrieval, acceptance, and refusal — coordinating the
 * {@link CandidateRepository}, {@link SupplierRepository}, and
 * {@link CountryService} ports.
 */
@Service
public class CandidateService {

    private static final long MINIMUM_ANNUAL_TURNOVER = 1_000_000L;

    private final CandidateRepository candidateRepository;
    private final SupplierRepository supplierRepository;
    private final CountryService countryService;

    public CandidateService(CandidateRepository candidateRepository,
            SupplierRepository supplierRepository,
            CountryService countryService) {
        this.candidateRepository = candidateRepository;
        this.supplierRepository = supplierRepository;
        this.countryService = countryService;
    }

    /**
     * Submits a new supplier candidacy application in {@code PENDING} status.
     *
     * <p>Validation order:
     * <ol>
     *   <li>The country code must be known to the external country service
     *       → {@link CountryNotFoundException} (HTTP 422) if not.</li>
     *   <li>A disqualified (banned) supplier must not reapply
     *       → {@link SupplierBannedException} (HTTP 409).</li>
     *   <li>No active candidacy or non-disqualified supplier may already exist for
     *       the same DUNS → {@link CandidateAlreadyExistsException} (HTTP 409).</li>
     * </ol>
     *
     * @param dunsValue      the DUNS identifier (100,000,000–999,999,999)
     * @param name           the supplier's trading name
     * @param countryCode    ISO 3166-1 alpha-2 country code of the supplier's headquarters
     * @param annualTurnover declared annual turnover in euros
     * @return the newly created {@link Candidate} with status {@code PENDING}
     */
    @Transactional
    public Candidate createCandidate(int dunsValue, String name, String countryCode, long annualTurnover) {
        Duns duns = new Duns(dunsValue);
        Country country = new Country(countryCode);

        countryService.isBanned(country);

        supplierRepository.findByDuns(duns).ifPresent(supplier -> {
            if (supplier.isDisqualified()) {
                throw new SupplierBannedException();
            }
            throw new CandidateAlreadyExistsException();
        });

        if (candidateRepository.existsActiveCandidacyForDuns(duns)) {
            throw new CandidateAlreadyExistsException();
        }

        return candidateRepository.save(Candidate.newApplication(duns, name, country, annualTurnover));
    }

    /**
     * Retrieves a candidacy by DUNS identifier.
     *
     * @param dunsValue the DUNS identifier to look up
     * @return the matching {@link Candidate}
     * @throws CandidateNotFoundException if no candidacy exists for the given DUNS
     */
    @Transactional(readOnly = true)
    public Candidate getCandidate(int dunsValue) {
        return candidateRepository.findByDuns(new Duns(dunsValue))
                .orElseThrow(() -> new CandidateNotFoundException(dunsValue));
    }

    /**
     * Accepts a pending candidacy, assigns a sustainability rating, and promotes the
     * candidate to a {@link Supplier}.
     *
     * <p>Acceptance conditions checked in order:
     * <ol>
     *   <li>The candidacy must exist → {@link CandidateNotFoundException} (HTTP 404).</li>
     *   <li>The candidacy must be in {@code PENDING} status
     *       → {@link CandidateNotAcceptableException} (HTTP 409).</li>
     *   <li>The candidate's country must not be banned
     *       → {@link CandidateNotAcceptableException} (HTTP 409).</li>
     *   <li>The annual turnover must be ≥ {@value #MINIMUM_ANNUAL_TURNOVER} euros
     *       → {@link CandidateNotAcceptableException} (HTTP 409).</li>
     * </ol>
     *
     * <p>On success the candidacy status is set to {@code ACCEPTED} and a new
     * {@link Supplier} is created; its initial status ({@code ACTIVE} or
     * {@code ON_PROBATION}) depends on the assigned rating.
     *
     * @param dunsValue the DUNS identifier of the candidate to accept
     * @param rating    the sustainability rating (A–E) assigned by the supervisor
     */
    @Transactional
    public void acceptCandidate(int dunsValue, SustainabilityRating rating) {
        Duns duns = new Duns(dunsValue);
        Candidate candidate = candidateRepository.findByDuns(duns)
                .orElseThrow(() -> new CandidateNotFoundException(dunsValue));

        if (!candidate.isPending()) {
            throw new CandidateNotAcceptableException("Candidate is not in PENDING status");
        }

        if (countryService.isBanned(candidate.getCountry())) {
            throw new CandidateNotAcceptableException("Candidate country is banned");
        }

        if (candidate.getAnnualTurnover() < MINIMUM_ANNUAL_TURNOVER) {
            throw new CandidateNotAcceptableException("Annual turnover is below the minimum required");
        }

        candidate.accept();
        candidateRepository.save(candidate);

        Supplier supplier = Supplier.from(candidate, rating);
        supplierRepository.save(supplier);
    }

    /**
     * Refuses a pending candidacy, setting its status to {@code REFUSED}.
     *
     * <p>A refused candidate may reapply by submitting a new candidacy.
     * Only {@code PENDING} candidates can be refused; attempting to refuse an
     * already-decided candidacy throws
     * {@link com.inditex.supplier.domain.exception.InvalidCandidateStateException}.
     *
     * @param dunsValue the DUNS identifier of the candidate to refuse
     * @throws CandidateNotFoundException if no candidacy exists for the given DUNS
     */
    @Transactional
    public void refuseCandidate(int dunsValue) {
        Duns duns = new Duns(dunsValue);
        Candidate candidate = candidateRepository.findByDuns(duns)
                .orElseThrow(() -> new CandidateNotFoundException(dunsValue));

        candidate.refuse();
        candidateRepository.save(candidate);
    }
}
