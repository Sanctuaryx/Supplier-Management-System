package com.inditex.supplier.domain.port;

import com.inditex.supplier.domain.model.Candidate;
import com.inditex.supplier.domain.model.Duns;

import java.util.Optional;

/**
 * Outbound port for persisting and querying {@link Candidate} aggregates.
 * Implementations are provided by the infrastructure layer (e.g. JPA adapter).
 */
public interface CandidateRepository {

    /**
     * Retrieves the most recent candidacy record for the given DUNS.
     * Note: because a refused candidate may reapply, multiple candidacy records
     * can exist for the same DUNS; this returns the single active one (PENDING or ACCEPTED).
     *
     * @param duns the DUNS identifier to look up
     * @return an {@link Optional} containing the candidate, or empty if none exists
     */
    Optional<Candidate> findByDuns(Duns duns);

    /**
     * Checks whether an active candidacy (status PENDING or ACCEPTED) exists for the given DUNS.
     * Used to enforce the integrity rule that only one active candidacy per DUNS is allowed.
     *
     * @param duns the DUNS identifier to check
     * @return {@code true} if a non-refused candidacy exists for this DUNS
     */
    boolean existsActiveCandidacyForDuns(Duns duns);

    /**
     * Persists a new or updated Candidate. Acts as both insert and update
     * (upsert by DUNS primary key).
     *
     * @param candidate the candidate to persist
     * @return the saved candidate (same instance)
     */
    Candidate save(Candidate candidate);
}
