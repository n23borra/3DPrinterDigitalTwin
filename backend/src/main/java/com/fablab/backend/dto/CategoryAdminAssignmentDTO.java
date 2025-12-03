package com.fablab.backend.dto;

import java.util.List;

/**
 * Represents the users assigned to administer a specific asset category.
 *
 * @param categoryId identifier of the category being administered
 * @param label      label of the category for display purposes
 * @param emails     list of user emails currently assigned as administrators
 */
public record CategoryAdminAssignmentDTO(
        Short categoryId,
        String label,
        List<String> emails
) {}