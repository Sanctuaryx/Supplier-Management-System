package com.inditex.supplier.infrastructure.persistence;

import com.inditex.supplier.domain.model.Candidate;
import com.inditex.supplier.domain.model.CandidateStatus;
import com.inditex.supplier.domain.model.Country;
import com.inditex.supplier.domain.model.Duns;
import com.inditex.supplier.domain.port.CandidateRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA-backed implementation of {@link CandidateRepository}.
 * Maps between the domain {@link Candidate} aggregate and the JPA entity
 * {@link CandidateJpaEntity}.
 */
@Repository
public class CandidateRepositoryAdapter implements CandidateRepository {

    private final CandidateJpaRepository jpaRepository;

    /**
     * @param jpaRepository Spring Data JPA repository for candidate persistence
     */
    public CandidateRepositoryAdapter(CandidateJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * {@inheritDoc}
     * Looks up by DUNS primary key. Returns the single record (PENDING or ACCEPTED)
     * for the given DUNS, if any exists.
     */
    @Override
    public Optional<Candidate> findByDuns(Duns duns) {
        return jpaRepository.findById(duns.value()).map(this::toDomain);
    }

    /**
     * {@inheritDoc}
     * Implemented as: does a record exist with this DUNS whose status is NOT
     * REFUSED?
     */
    @Override
    public boolean existsActiveCandidacyForDuns(Duns duns) {
        return jpaRepository.existsByDunsAndStatusNot(duns.value(), CandidateStatus.REFUSED.name());
    }

    /**
     * {@inheritDoc}
     * Performs an upsert via Spring Data's {@code save()} (insert or update by PK).
     */
    @Override
    public Candidate save(Candidate candidate) {
        CandidateJpaEntity entity = toEntity(candidate);
        jpaRepository.save(entity);
        return candidate;
    }

    /**
     * Maps a JPA entity to a domain {@link Candidate}.
     *
     * @param entity the JPA entity read from the database
     * @return the equivalent domain object
     */
    private Candidate toDomain(CandidateJpaEntity entity) {
        return new Candidate(
                new Duns(entity.getDuns()),
                entity.getName(),
                new Country(entity.getCountry()),
                entity.getAnnualTurnover(),
                CandidateStatus.valueOf(entity.getStatus()));
    }

    /**
     * Maps a domain {@link Candidate} to a JPA entity for persistence.
     *
     * @param candidate the domain object to persist
     * @return the JPA entity representation
     */
    @NonNull
    private CandidateJpaEntity toEntity(Candidate candidate) {
        return new CandidateJpaEntity(
                candidate.getDuns().value(),
                candidate.getName(),
                candidate.getCountry().code(),
                candidate.getAnnualTurnover(),
                candidate.getStatus().name());
    }
}
