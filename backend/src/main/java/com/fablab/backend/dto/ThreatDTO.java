package com.fablab.backend.dto;

import com.fablab.backend.models.Threat;

/**
 * DTO for {@link Threat} exposing the minimal attributes required by the UI.
 *
 * @param id          identifier of the threat
 * @param label       display label of the threat
 * @param probability likelihood value scored between 0 and 1
 */
public record ThreatDTO(
        Long id,
        String label,
        Double probability
) {
    public static ThreatDTO from(Threat t) {
        if (t == null) return null;
        return new ThreatDTO(t.getId(), t.getLabel(), t.getProbability());
    }
}
