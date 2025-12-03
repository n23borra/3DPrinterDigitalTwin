package com.fablab.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload describing a directional dependency between two assets.
 *
 * @param parentId identifier of the upstream asset providing capabilities
 * @param childId  identifier of the downstream asset depending on the parent
 */
public record AssetDependencyRequest(@NotNull Long parentId, @NotNull Long childId) {}