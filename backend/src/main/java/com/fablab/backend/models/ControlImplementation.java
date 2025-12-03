package com.fablab.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@IdClass(ControlImplementation.PK.class)
@Table(name = "control_implementation")
public class ControlImplementation {

    /* --- Composite key : relations --- */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_id")
    private RiskBase risk;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "control_id")
    private Control control;

    /**
     * Control implementation level expressed as 0, 0.5 or 1.
     */
    private double level;


    /* --- Helper primary key type --- */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements java.io.Serializable {
        private Long risk;      // Hibernate reads the internal identifier
        private Long control;
    }
}
