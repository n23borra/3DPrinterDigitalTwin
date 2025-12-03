package com.fablab.backend.models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity @Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "asset_category")
public class AssetCategory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 30)
    private String label;
}
