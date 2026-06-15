package com.inditex.supplier.infrastructure.persistence;

import com.inditex.supplier.domain.model.*;
import com.inditex.supplier.domain.port.SupplierRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JPA + JDBC implementation of {@link SupplierRepository}.
 *
 * <p>Simple CRUD (find, exists, save) is handled via Spring Data JPA ({@link SupplierJpaRepository}).
 * The complex potential-suppliers query — which involves window functions, a bonus CTE, score
 * calculation, and pagination — is executed via {@link JdbcTemplate} to bypass Hibernate's
 * native-query type mapping and use raw JDBC types directly.
 */
@Repository
public class SupplierRepositoryAdapter implements SupplierRepository {

    /**
     * Single SQL query that computes eligible suppliers, their scores, the small-supplier bonus,
     * and the total result count in one database round-trip.
     *
     * <p>Key design choices:
     * <ul>
     *   <li>{@code DENSE_RANK()} groups tied turnovers correctly — rank ≤ 2 picks the two
     *       lowest unique values.</li>
     *   <li>The bonus pool is ALL non-disqualified suppliers per country, not just those
     *       that qualify for this specific rate.</li>
     *   <li>{@code COUNT(*) OVER()} computes the total in a single pass without a second query.</li>
     * </ul>
     */
    private static final String POTENTIAL_SUPPLIERS_SQL = """
            WITH non_disq AS (
                SELECT duns, name, country, annual_turnover, rating, status
                FROM suppliers
                WHERE status != 'DISQUALIFIED'
            ),
            ranked_turnovers AS (
                SELECT country, annual_turnover,
                       DENSE_RANK() OVER (PARTITION BY country ORDER BY annual_turnover) AS dr
                FROM (SELECT DISTINCT country, annual_turnover FROM non_disq) t
            ),
            bonus_set AS (
                SELECT DISTINCT country, annual_turnover FROM ranked_turnovers WHERE dr <= 2
            ),
            eligible AS (
                SELECT nd.duns, nd.name, nd.country, nd.annual_turnover, nd.rating, nd.status,
                       nd.annual_turnover * 0.1
                         * CASE nd.rating
                             WHEN 'A' THEN 1.0 WHEN 'B' THEN 0.75 WHEN 'C' THEN 0.5
                             WHEN 'D' THEN 0.25 ELSE 0.1
                           END
                         * CASE WHEN bt.country IS NOT NULL THEN 1.25 ELSE 1.0 END AS score
                FROM non_disq nd
                LEFT JOIN bonus_set bt
                       ON nd.country = bt.country AND nd.annual_turnover = bt.annual_turnover
                WHERE nd.annual_turnover > ?
            )
            SELECT COUNT(*) OVER () AS total_count,
                   duns, name, country, annual_turnover, rating, status, score
            FROM eligible
            ORDER BY score DESC
            LIMIT ? OFFSET ?
            """;

    private final SupplierJpaRepository jpaRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * @param jpaRepository Spring Data JPA repository for simple CRUD
     * @param jdbcTemplate  raw JDBC template for the complex potential-suppliers query
     */
    public SupplierRepositoryAdapter(SupplierJpaRepository jpaRepository, JdbcTemplate jdbcTemplate) {
        this.jpaRepository = jpaRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Supplier> findByDuns(Duns duns) {
        return jpaRepository.findById(duns.value()).map(this::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsForDuns(Duns duns) {
        return jpaRepository.existsByDuns(duns.value());
    }

    /** {@inheritDoc} */
    @Override
    public Supplier save(Supplier supplier) {
        jpaRepository.save(toEntity(supplier));
        return supplier;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Executes {@link #POTENTIAL_SUPPLIERS_SQL} via JdbcTemplate. The total result count
     * is captured from the first row's {@code total_count} window-function column.
     */
    @Override
    public PotentialSuppliersPage findPotentialSuppliers(long rate, int limit, int offset) {
        AtomicInteger totalHolder = new AtomicInteger(0);

        List<PotentialSupplier> suppliers = jdbcTemplate.query(
                POTENTIAL_SUPPLIERS_SQL,
                ps -> {
                    ps.setLong(1, rate);
                    ps.setInt(2, limit);
                    ps.setInt(3, offset);
                },
                (rs, rowNum) -> {
                    if (rowNum == 0) {
                        totalHolder.set(rs.getInt("total_count"));
                    }
                    return new PotentialSupplier(
                            new Duns(rs.getInt("duns")),
                            rs.getString("name"),
                            new Country(rs.getString("country")),
                            rs.getLong("annual_turnover"),
                            SustainabilityRating.valueOf(rs.getString("rating")),
                            SupplierStatus.valueOf(rs.getString("status")),
                            rs.getDouble("score")
                    );
                }
        );

        int total = suppliers.isEmpty() ? 0 : totalHolder.get();
        return new PotentialSuppliersPage(suppliers, total, limit, offset);
    }

    /**
     * Maps a JPA entity to a domain {@link Supplier}.
     *
     * @param entity the JPA entity read from the database
     * @return the equivalent domain object
     */
    private Supplier toDomain(SupplierJpaEntity entity) {
        return new Supplier(
                new Duns(entity.getDuns()),
                entity.getName(),
                new Country(entity.getCountry()),
                entity.getAnnualTurnover(),
                SustainabilityRating.valueOf(entity.getRating()),
                SupplierStatus.valueOf(entity.getStatus())
        );
    }

    /**
     * Maps a domain {@link Supplier} to a JPA entity for persistence.
     *
     * @param supplier the domain object to persist
     * @return the JPA entity representation
     */
    @NonNull
    private SupplierJpaEntity toEntity(Supplier supplier) {
        return new SupplierJpaEntity(
                supplier.getDuns().value(),
                supplier.getName(),
                supplier.getCountry().code(),
                supplier.getAnnualTurnover(),
                supplier.getRating().name(),
                supplier.getStatus().name()
        );
    }
}
