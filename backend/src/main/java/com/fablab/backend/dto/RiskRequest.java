package com.fablab.backend.dto;

/**
 * Payload describing the triplet of asset, threat and vulnerability for which
 * a base risk must be created.
 *
 * @param assetId         identifier of the impacted asset
 * @param threatId        identifier of the applicable threat
 * @param vulnerabilityId identifier of the exploited vulnerability
 */
public record RiskRequest(
        Long assetId,
        Long threatId,
        Long vulnerabilityId
) {}
