package com.inditex.supplier.domain.model;

import com.inditex.supplier.domain.exception.InvalidCandidateStateException;

/**
 * Aggregate root representing a supplier candidacy application.
 *
 * <p>A candidate is created with status {@link CandidateStatus#PENDING} via
 * {@link #newApplication(Duns, String, Country, long)} and can transition to either
 * {@link CandidateStatus#ACCEPTED} or {@link CandidateStatus#REFUSED}.
 *
 * <p>A refused candidate may reapply (a new {@link Candidate} can be created for the same
 * {@link Duns}), but only one active (non-refused) candidacy per DUNS is allowed at any time.
 */
public class Candidate {

    private final Duns duns;
    private final String name;
    private final Country country;
    private final long annualTurnover;
    private CandidateStatus status;

    /**
     * Reconstructs a {@link Candidate} from persistent storage with all known fields.
     *
     * @param duns           the unique {@link Duns} identifier of the supplier
     * @param name           the supplier's trading name; must not be blank
     * @param country        the {@link Country} where the supplier is headquartered
     * @param annualTurnover the declared annual turnover in euros; must be non-negative
     * @param status         the current {@link CandidateStatus} of this candidacy
     */
    public Candidate(Duns duns, String name, Country country, long annualTurnover, CandidateStatus status) {
        this.duns = duns;
        this.name = name;
        this.country = country;
        this.annualTurnover = annualTurnover;
        this.status = status;
    }

    /**
     * Factory method that creates a new candidacy application in {@link CandidateStatus#PENDING} status.
     *
     * @param duns           the {@link Duns} identifier of the applying supplier
     * @param name           the supplier's trading name
     * @param country        the {@link Country} of the supplier's headquarters (ISO 3166-1 alpha-2)
     * @param annualTurnover the declared annual turnover in euros
     * @return a new {@link Candidate} with status {@link CandidateStatus#PENDING}
     */
    public static Candidate newApplication(Duns duns, String name, Country country, long annualTurnover) {
        return new Candidate(duns, name, country, annualTurnover, CandidateStatus.PENDING);
    }

    /**
     * Transitions this candidacy from {@link CandidateStatus#PENDING} to {@link CandidateStatus#ACCEPTED}.
     * Should be called only after all acceptance conditions have been verified by the use case.
     *
     * @throws InvalidCandidateStateException if this candidacy is not currently in
     *                                        {@link CandidateStatus#PENDING} status
     */
    public void accept() {
        if (status != CandidateStatus.PENDING) {
            throw new InvalidCandidateStateException("Candidate cannot be accepted in status: " + status);
        }
        this.status = CandidateStatus.ACCEPTED;
    }

    /**
     * Transitions this candidacy from {@link CandidateStatus#PENDING} to {@link CandidateStatus#REFUSED}.
     * A refused candidate is permitted to reapply by submitting a new candidacy application.
     *
     * @throws InvalidCandidateStateException if this candidacy is not currently in
     *                                        {@link CandidateStatus#PENDING} status
     */
    public void refuse() {
        if (status != CandidateStatus.PENDING) {
            throw new InvalidCandidateStateException("Candidate cannot be refused in status: " + status);
        }
        this.status = CandidateStatus.REFUSED;
    }

    /**
     * Returns whether this candidacy is still awaiting a supervisor decision.
     *
     * @return {@code true} if the current status is {@link CandidateStatus#PENDING},
     *         {@code false} otherwise
     */
    public boolean isPending() {
        return status == CandidateStatus.PENDING;
    }

    /**
     * Returns the unique DUNS identifier of this candidate.
     *
     * @return the {@link Duns} value object
     */
    public Duns getDuns() { return duns; }

    /**
     * Returns the trading name of this candidate.
     *
     * @return the supplier's name as a non-blank {@link String}
     */
    public String getName() { return name; }

    /**
     * Returns the country where this candidate's headquarters is located.
     *
     * @return the {@link Country} value object (ISO 3166-1 alpha-2)
     */
    public Country getCountry() { return country; }

    /**
     * Returns the declared annual turnover used for acceptance and scoring.
     *
     * @return the annual turnover in euros; always non-negative
     */
    public long getAnnualTurnover() { return annualTurnover; }

    /**
     * Returns the current lifecycle status of this candidacy.
     *
     * @return one of {@link CandidateStatus#PENDING}, {@link CandidateStatus#ACCEPTED},
     *         or {@link CandidateStatus#REFUSED}
     */
    public CandidateStatus getStatus() { return status; }
}
