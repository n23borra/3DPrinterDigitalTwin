package com.fablab.backend.repositories;

import com.fablab.backend.models.CategoryAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository managing administrators assigned to asset categories.
 */
public interface CategoryAdminRepository extends JpaRepository<CategoryAdmin, CategoryAdmin.PK> {
    /**
     * Retrieves the administrators of a given category.
     *
     * @param categoryId identifier of the category
     * @return list of category-admin relations
     */
    List<CategoryAdmin> findAllByCategoryId(Short categoryId);

    /**
     * Lists the categories administered by a specific user.
     *
     * @param userId identifier of the user
     * @return relations linking the user to categories
     */
    List<CategoryAdmin> findAllByUserId(Long userId);

    /**
     * Checks whether a given user already administers a specific category.
     *
     * @param categoryId identifier of the category
     * @param userId     identifier of the user
     * @return {@code true} if the assignment exists
     */
    boolean existsByCategoryIdAndUserId(Short categoryId, Long userId);

    /**
     * Removes every administrator assignment for the supplied category.
     *
     * @param categoryId identifier of the category whose assignments are cleared
     */
    void deleteByCategoryId(Short categoryId);
}

