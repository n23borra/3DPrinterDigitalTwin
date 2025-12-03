package com.fablab.backend.dto;

import com.fablab.backend.models.enums.ActionStatus;
import com.fablab.backend.models.enums.TreatmentStrategy;

import java.time.LocalDate;

/**
 * Payload for updating the details of an existing treatment plan.
 *
 * @param description   textual description of the planned actions
 * @param responsibleId identifier of the user overseeing the plan
 * @param dueDate       target completion date for the plan
 * @param status        current {@link ActionStatus} of the plan
 * @param strategy      updated {@link TreatmentStrategy} when applicable
 */
public record TreatmentPlanUpdateDTO(
        String description,
        Long responsibleId,
        LocalDate dueDate,
        ActionStatus status,
        TreatmentStrategy strategy
) {
}