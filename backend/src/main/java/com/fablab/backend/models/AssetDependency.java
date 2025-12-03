package com.fablab.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(AssetDependency.PK.class)
@Table(name = "asset_dependency")
public class AssetDependency {

    @Id
    private Long parentAsset;

    @Id
    private Long childAsset;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements java.io.Serializable {
        private Long parentAsset;
        private Long childAsset;
    }
}

