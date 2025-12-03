package com.fablab.backend.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "control")
public class Control {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optional external UUID */
    @Column(unique = true)
    private String uuid;

    /** External short code */
    private String code;

    @Column(unique = true, nullable = false)
    private String label;

    @Column(columnDefinition = "text")
    private String description;

    /** Additional metadata as JSON */
    @Column(columnDefinition = "jsonb")
    private String meta;

    private String category;

    private String referential;

    @Column(name = "referential_label")
    private String referentialLabel;

    private Double efficiency; // 0–1
}
