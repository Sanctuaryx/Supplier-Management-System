package com.inditex.supplier.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateJpaRepository extends JpaRepository<CandidateJpaEntity, Integer> {

    boolean existsByDunsAndStatusNot(int duns, String status);
}
