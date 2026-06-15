package com.inditex.supplier.domain.port;

import com.inditex.supplier.domain.model.Duns;
import com.inditex.supplier.domain.model.PotentialSuppliersPage;
import com.inditex.supplier.domain.model.Supplier;
import com.inditex.supplier.domain.model.SupplierStatus;

import java.util.Optional;

/**
 * Outbound port (driven side) for persisting and querying {@link Supplier} aggregates.
 *
 * <p>Simple CRUD methods are intended to be backed by JPA, while
 * {@link #findPotentialSuppliers(long, int, int)} is backed by a native SQL query
 * that computes scores and the small-supplier bonus in a single database round-trip.
 */
public interface SupplierRepository {

    /**
     * Retrieves the supplier with the given {@link Duns} identifier.
     *
     * @param duns the {@link Duns} identifier to look up; must not be {@code null}
     * @return an {@link Optional} containing the matching {@link Supplier},
     *         or {@link Optional#empty()} if no supplier exists for that DUNS
     */
    Optional<Supplier> findByDuns(Duns duns);

    /**
     * Checks whether any {@link Supplier} record exists for the given {@link Duns},
     * regardless of its {@link SupplierStatus}.
     *
     * <p>Used by the create-candidate flow to prevent registering a new candidacy
     * when a supplier (active or disqualified) already exists for the same DUNS.
     *
     * @param duns the {@link Duns} identifier to check; must not be {@code null}
     * @return {@code true} if a supplier record exists, {@code false} otherwise
     */
    boolean existsForDuns(Duns duns);

    /**
     * Persists a new or updated {@link Supplier}.
     * Acts as an upsert: inserts on first save, updates on subsequent saves.
     *
     * @param supplier the {@link Supplier} to persist; must not be {@code null}
     * @return the saved {@link Supplier} (same instance as the input)
     */
    Supplier save(Supplier supplier);

    /**
     * Queries non-disqualified suppliers eligible for an order of the given amount,
     * with score and small-supplier bonus computed in a single SQL query.
     *
     * <p><strong>Eligibility:</strong> {@code annualTurnover > rate} and
     * {@link SupplierStatus} is not {@link SupplierStatus#DISQUALIFIED}.
     *
     * <p><strong>Score formula:</strong>
     * {@code annualTurnover × 0.1 × ratingConstant × bonusMultiplier},
     * where {@code bonusMultiplier} is {@code 1.25} for suppliers whose turnover is among
     * the two lowest unique turnovers in their country (across all non-disqualified suppliers),
     * and {@code 1.0} otherwise.
     *
     * <p>Results are ordered by score descending. Pagination is applied via {@code limit}
     * and {@code offset}, and the total matching count is returned alongside the page data.
     *
     * @param rate   the order amount in euros; only suppliers with turnover strictly
     *               greater than this value are included
     * @param limit  the maximum number of results to return (page size)
     * @param offset the zero-based index of the first result
     * @return a {@link PotentialSuppliersPage} containing the scored and paginated results,
     *         plus the total count of matching suppliers before pagination
     */
    PotentialSuppliersPage findPotentialSuppliers(long rate, int limit, int offset);
}
