package com.fablab.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "control_recommendation", uniqueConstraints =
@UniqueConstraint(columnNames = {"threat_id", "vulnerability_id", "control_id"}))
public class ControlRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threat_id")
    private Threat threat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vulnerability_id")
    private Vulnerability vulnerability;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "control_id")
    private Control control;
}