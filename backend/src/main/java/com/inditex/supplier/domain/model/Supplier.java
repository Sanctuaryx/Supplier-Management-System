package com.inditex.supplier.domain.model;

import com.inditex.supplier.domain.exception.InvalidSupplierStateException;

/**
 * Aggregate root representing a verified Inditex supplier.
 *
 * <p>A supplier is created from an accepted {@link Candidate} via
 * {@link #from(Candidate, SustainabilityRating)}. The initial status is determined
 * by the assigned {@link SustainabilityRating}:
 * <ul>
 *   <li>Ratings {@link SustainabilityRating#A} or {@link SustainabilityRating#B}
 *       → {@link SupplierStatus#ACTIVE}</li>
 *   <li>Ratings {@link SustainabilityRating#C}, {@link SustainabilityRating#D}, or
 *       {@link SustainabilityRating#E} → {@link SupplierStatus#ON_PROBATION}</li>
 * </ul>
 *
 * <p>A supplier with status {@link SupplierStatus#ON_PROBATION} can be banned via
 * {@link #ban()}, transitioning to {@link SupplierStatus#DISQUALIFIED}. A disqualified
 * supplier cannot reapply or appear in potential-supplier results.
 */
public class Supplier {

    private final Duns duns;
    private final String name;
    private final Country country;
    private final long annualTurnover;
    private final SustainabilityRating rating;
    private SupplierStatus status;

    /**
     * Reconstructs a {@link Supplier} from persistent storage with all known fields.
     *
     * @param duns           the unique {@link Duns} identifier
     * @param name           the supplier's trading name
     * @param country        the {@link Country} where the supplier is headquartered
     * @param annualTurnover the annual turnover in euros; must be non-negative
     * @param rating         the assigned {@link SustainabilityRating} (A–E)
     * @param status         the current {@link SupplierStatus}
     */
    public Supplier(Duns duns, String name, Country country, long annualTurnover,
                    SustainabilityRating rating, SupplierStatus status) {
        this.duns = duns;
        this.name = name;
        this.country = country;
        this.annualTurnover = annualTurnover;
        this.rating = rating;
        this.status = status;
    }

    /**
     * Factory method that creates a new {@link Supplier} from an accepted {@link Candidate}.
     *
     * <p>The initial {@link SupplierStatus} is derived from the rating via
     * {@link SustainabilityRating#leadsToActivation()}:
     * {@link SustainabilityRating#A}/{@link SustainabilityRating#B} → {@link SupplierStatus#ACTIVE},
     * {@link SustainabilityRating#C}/{@link SustainabilityRating#D}/{@link SustainabilityRating#E}
     * → {@link SupplierStatus#ON_PROBATION}.
     *
     * @param candidate the accepted {@link Candidate} to promote to supplier status
     * @param rating    the {@link SustainabilityRating} assigned by the supervisor
     * @return a new {@link Supplier} with the appropriate initial {@link SupplierStatus}
     */
    public static Supplier from(Candidate candidate, SustainabilityRating rating) {
        SupplierStatus initialStatus = rating.leadsToActivation()
                ? SupplierStatus.ACTIVE
                : SupplierStatus.ON_PROBATION;
        return new Supplier(
                candidate.getDuns(),
                candidate.getName(),
                candidate.getCountry(),
                candidate.getAnnualTurnover(),
                rating,
                initialStatus
        );
    }

    /**
     * Transitions this supplier from {@link SupplierStatus#ON_PROBATION} to
     * {@link SupplierStatus#DISQUALIFIED}, permanently excluding them from Inditex operations.
     *
     * @throws InvalidSupplierStateException if this supplier is not currently in
     *                                       {@link SupplierStatus#ON_PROBATION} status
     */
    public void ban() {
        if (status != SupplierStatus.ON_PROBATION) {
            throw new InvalidSupplierStateException("Supplier cannot be banned in status: " + status);
        }
        this.status = SupplierStatus.DISQUALIFIED;
    }

    /**
     * Returns whether this supplier has been permanently disqualified (banned).
     *
     * @return {@code true} if the current status is {@link SupplierStatus#DISQUALIFIED},
     *         {@code false} otherwise
     */
    public boolean isDisqualified() {
        return status == SupplierStatus.DISQUALIFIED;
    }

    /**
     * Returns whether this supplier is currently on probation.
     *
     * @return {@code true} if the current status is {@link SupplierStatus#ON_PROBATION},
     *         {@code false} otherwise
     */
    public boolean isOnProbation() {
        return status == SupplierStatus.ON_PROBATION;
    }

    /**
     * Calculates this supplier's score for potential-supplier ranking.
     *
     * <p>Formula: {@code annualTurnover × 0.1 × ratingConstant × bonusMultiplier},
     * where {@code bonusMultiplier} is {@code 1.25} when the small-supplier bonus applies
     * (i.e. this supplier's turnover is among the two lowest unique turnovers in their country),
     * and {@code 1.0} otherwise. The {@code ratingConstant} is provided by
     * {@link SustainabilityRating#toRatingConstant()}.
     *
     * @param bonusApplies {@code true} if this supplier qualifies for the 25% small-supplier bonus
     * @return the calculated score as a {@code double}; always non-negative
     */
    public double calculateScore(boolean bonusApplies) {
        double base = annualTurnover * 0.1 * rating.toRatingConstant();
        return bonusApplies ? base * 1.25 : base;
    }

    /**
     * Returns the unique DUNS identifier of this supplier.
     *
     * @return the {@link Duns} value object
     */
    public Duns getDuns() { return duns; }

    /**
     * Returns the trading name of this supplier.
     *
     * @return the supplier's name as a non-blank {@link String}
     */
    public String getName() { return name; }

    /**
     * Returns the country where this supplier's headquarters is located.
     *
     * @return the {@link Country} value object (ISO 3166-1 alpha-2)
     */
    public Country getCountry() { return country; }

    /**
     * Returns the annual turnover used for eligibility checks and score calculation.
     *
     * @return the annual turnover in euros; always non-negative
     */
    public long getAnnualTurnover() { return annualTurnover; }

    /**
     * Returns the sustainability rating assigned to this supplier upon acceptance.
     *
     * @return the {@link SustainabilityRating}, one of A through E
     */
    public SustainabilityRating getRating() { return rating; }

    /**
     * Returns the current lifecycle status of this supplier.
     *
     * @return one of {@link SupplierStatus#ACTIVE}, {@link SupplierStatus#ON_PROBATION},
     *         or {@link SupplierStatus#DISQUALIFIED}
     */
    public SupplierStatus getStatus() { return status; }
}
