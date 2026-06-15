package com.inditex.supplier.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "candidates")
public class CandidateJpaEntity {

    @Id
    @Column(name = "duns")
    private int duns;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "country", nullable = false, length = 2)
    private String country;

    @Column(name = "annual_turnover", nullable = false)
    private long annualTurnover;

    @Column(name = "status", nullable = false)
    private String status;

    protected CandidateJpaEntity() {}

    public CandidateJpaEntity(int duns, String name, String country, long annualTurnover, String status) {
        this.duns = duns;
        this.name = name;
        this.country = country;
        this.annualTurnover = annualTurnover;
        this.status = status;
    }

    public int getDuns() { return duns; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public long getAnnualTurnover() { return annualTurnover; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
