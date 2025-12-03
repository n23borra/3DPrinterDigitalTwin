package com.fablab.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "asset")
public class Asset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private AssetCategory category;

    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;

    /** impact scores 0–4 */
    @Column(name = "impact_c")
    private Short impactC;
    @Column(name = "impact_i")
    private Short impactI;
    @Column(name = "impact_a")
    private Short impactA;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
