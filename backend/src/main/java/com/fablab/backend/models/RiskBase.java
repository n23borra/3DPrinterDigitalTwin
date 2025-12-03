package com.fablab.backend.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "risk_base", uniqueConstraints = @UniqueConstraint(columnNames = {"asset_id", "threat_id", "vulnerability_id"}))
public class RiskBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threat_id")
    private Threat threat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vulnerability_id")
    private Vulnerability vulnerability;
}

