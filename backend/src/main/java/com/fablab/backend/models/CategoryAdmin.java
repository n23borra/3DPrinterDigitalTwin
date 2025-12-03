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
@IdClass(CategoryAdmin.PK.class)
@Table(name = "category_admin")
public class CategoryAdmin {

    @Id
    private Short categoryId;

    @Id
    private Long userId;

    /* ------------ Composite key ------------ */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements java.io.Serializable {
        private Short categoryId;
        private Long userId;
    }
}
