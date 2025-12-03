package com.fablab.backend.dto;

/**
 * Payload linking a recommended control to a risk during assignment.
 *
 * @param riskId    identifier of the risk being treated
 * @param controlId identifier of the control selected to mitigate the risk
 */
public record ControlAssignmentRequest(
        Long riskId,
        Long controlId
) {
}