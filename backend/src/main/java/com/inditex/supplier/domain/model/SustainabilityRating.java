package com.inditex.supplier.domain.model;

/**
 * Sustainability rating assigned to a {@link Supplier} upon acceptance, graded from
 * {@link #A} (best) to {@link #E} (worst).
 *
 * <p>Each value carries a {@link #toRatingConstant() rating constant} used in the score
 * formula, and determines whether the supplier starts as {@link SupplierStatus#ACTIVE}
 * or {@link SupplierStatus#ON_PROBATION} via {@link #leadsToActivation()}.
 */
public enum SustainabilityRating {

    /** Best rating. Rating constant: {@code 1.0}. Leads to {@link SupplierStatus#ACTIVE}. */
    A(1.0),

    /** Second-best rating. Rating constant: {@code 0.75}. Leads to {@link SupplierStatus#ACTIVE}. */
    B(0.75),

    /** Mid rating. Rating constant: {@code 0.5}. Leads to {@link SupplierStatus#ON_PROBATION}. */
    C(0.5),

    /** Below-average rating. Rating constant: {@code 0.25}. Leads to {@link SupplierStatus#ON_PROBATION}. */
    D(0.25),

    /** Worst rating. Rating constant: {@code 0.1}. Leads to {@link SupplierStatus#ON_PROBATION}. */
    E(0.1);

    private final double ratingConstant;

    SustainabilityRating(double ratingConstant) {
        this.ratingConstant = ratingConstant;
    }

    /**
     * Returns the multiplier applied in the score formula
     * {@code score = annualTurnover × 0.1 × ratingConstant × bonusMultiplier}.
     *
     * @return the rating constant: {@code 1.0} for {@link #A}, {@code 0.75} for {@link #B},
     *         {@code 0.5} for {@link #C}, {@code 0.25} for {@link #D}, {@code 0.1} for {@link #E}
     */
    public double toRatingConstant() {
        return ratingConstant;
    }

    /**
     * Determines whether a newly accepted supplier with this rating should start as
     * {@link SupplierStatus#ACTIVE} rather than {@link SupplierStatus#ON_PROBATION}.
     *
     * @return {@code true} for {@link #A} and {@link #B};
     *         {@code false} for {@link #C}, {@link #D}, and {@link #E}
     */
    public boolean leadsToActivation() {
        return this == A || this == B;
    }
}
