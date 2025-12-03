package com.fablab.backend.models;


import com.fablab.backend.models.enums.ActionStatus;
import com.fablab.backend.models.enums.TreatmentStrategy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "treatment_plan")
public class TreatmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_id")
    private RiskResult riskResult;

    @Enumerated(EnumType.STRING)
    private TreatmentStrategy strategy;

    @Column(columnDefinition = "text")
    private String description;

    private Long responsibleId;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private ActionStatus status = ActionStatus.PLANNED;

    private Instant createdAt = Instant.now();
    private Instant closedAt;
}
