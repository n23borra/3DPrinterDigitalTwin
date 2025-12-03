package com.fablab.twin.domain.dto;

import java.util.Set;
import java.util.UUID;

public record LoginResponse(UUID userId, String token, Set<String> roles) {}