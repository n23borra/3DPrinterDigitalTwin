package com.fablab.backend.models;

import com.fablab.backend.models.enums.CriticalityLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "analysis")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "text")
    private String description;
    private String language;
    private String scope;

    @Enumerated(EnumType.STRING)
    private CriticalityLevel criticality;

    /**
     * thresholds inherited from the preset, stored for traceability
     */
    private Integer s1;
    private Integer s2;

    /**
     * Defensive Maturity & Threat Actor capability
     */
    private Short dm;
    private Short ta;

    private Long ownerId;
    private Instant createdAt = Instant.now();
}
