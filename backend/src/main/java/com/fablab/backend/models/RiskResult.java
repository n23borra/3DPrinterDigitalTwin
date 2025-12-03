package com.fablab.backend.models;


import com.fablab.backend.models.enums.RiskStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Stores the calculated risk metrics for a {@link RiskBase}, including the
 * various residual scenarios (R0–R3) and the resulting status.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "risk_result")
public class RiskResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_id")
    private RiskBase risk;

    private Double r0, r1, r2, r3, fr;

    @Enumerated(EnumType.STRING)
    private RiskStatus status;

    private Instant lastCalc = Instant.now();

    public Long getId() {
        return id;
    }

    public RiskBase getRisk() {
        return risk;
    }

    public Double getR0() {
        return r0;
    }

    public Double getR1() {
        return r1;
    }

    public Double getR2() {
        return r2;
    }

    public Double getR3() {
        return r3;
    }

    public Double getFr() {
        return fr;
    }

    public RiskStatus getStatus() {
        return status;
    }

    public Instant getLastCalc() {
        return lastCalc;
    }

    public void setRisk(RiskBase risk) {
        this.risk = risk;
    }

    public void setR0(Double r0) {
        this.r0 = r0;
    }

    public void setR1(Double r1) {
        this.r1 = r1;
    }

    public void setR2(Double r2) {
        this.r2 = r2;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLastCalc(Instant lastCalc) {
        this.lastCalc = lastCalc;
    }

    public void setStatus(RiskStatus status) {
        this.status = status;
    }

    public void setFr(Double fr) {
        this.fr = fr;
    }

    public void setR3(Double r3) {
        this.r3 = r3;
    }
}
