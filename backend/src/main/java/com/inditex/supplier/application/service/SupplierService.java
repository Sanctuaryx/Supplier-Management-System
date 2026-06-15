package com.inditex.supplier.application.service;

import com.inditex.supplier.domain.exception.InvalidSupplierStateException;
import com.inditex.supplier.domain.exception.SupplierNotFoundException;
import com.inditex.supplier.domain.model.Duns;
import com.inditex.supplier.domain.model.PotentialSuppliersPage;
import com.inditex.supplier.domain.model.Supplier;
import com.inditex.supplier.domain.model.SupplierStatus;
import com.inditex.supplier.domain.port.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for supplier operations.
 *
 * <p>Covers three responsibilities: retrieving a single supplier by DUNS,
 * banning a supplier on probation, and querying the scored list of potential
 * suppliers for a given order amount.
 */
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    /**
     * Retrieves a supplier by DUNS identifier.
     *
     * @param dunsValue the DUNS identifier to look up
     * @return the matching {@link Supplier}
     * @throws SupplierNotFoundException if no supplier exists for the given DUNS
     */
    @Transactional(readOnly = true)
    public Supplier getSupplier(int dunsValue) {
        return supplierRepository.findByDuns(new Duns(dunsValue))
                .orElseThrow(() -> new SupplierNotFoundException(dunsValue));
    }

    /**
     * Bans a supplier who is currently on probation, permanently disqualifying them.
     *
     * <p>Only suppliers with status {@link SupplierStatus#ON_PROBATION} can be banned.
     * A disqualified supplier cannot be re-activated and cannot reapply as a candidate.
     *
     * @param dunsValue the DUNS identifier of the supplier to ban
     * @throws SupplierNotFoundException     if no supplier exists for the given DUNS
     * @throws InvalidSupplierStateException if the supplier is not currently {@code ON_PROBATION}
     */
    @Transactional
    public void banSupplier(int dunsValue) {
        Duns duns = new Duns(dunsValue);
        Supplier supplier = supplierRepository.findByDuns(duns)
                .orElseThrow(() -> new SupplierNotFoundException(dunsValue));

        supplier.ban();
        supplierRepository.save(supplier);
    }

    /**
     * Returns a paginated, score-ranked list of suppliers eligible for a given order amount.
     *
     * <p><strong>Eligibility:</strong> the supplier must not be {@code DISQUALIFIED} and
     * their annual turnover must be strictly greater than {@code rate}.
     *
     * <p><strong>Score formula:</strong>
     * {@code annualTurnover × 0.1 × ratingConstant × bonusMultiplier}, where a 25% bonus
     * is applied to suppliers whose annual turnover is among the two lowest unique turnovers
     * in their country (across all non-disqualified suppliers).
     *
     * @param rate   the order amount in euros (minimum 250); used as the turnover threshold
     * @param limit  maximum number of results per page (1–10)
     * @param offset zero-based index of the first result
     * @return a {@link PotentialSuppliersPage} with scored suppliers and total count
     */
    @Transactional(readOnly = true)
    public PotentialSuppliersPage getPotentialSuppliers(long rate, int limit, int offset) {
        return supplierRepository.findPotentialSuppliers(rate, limit, offset);
    }
}
