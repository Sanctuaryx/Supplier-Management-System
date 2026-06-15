package com.inditex.supplier.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, Integer> {

    boolean existsByDuns(int duns);
}
