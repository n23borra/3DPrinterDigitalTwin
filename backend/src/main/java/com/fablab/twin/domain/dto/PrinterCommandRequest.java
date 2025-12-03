package com.fablab.twin.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record PrinterCommandRequest(@NotBlank String command) {}