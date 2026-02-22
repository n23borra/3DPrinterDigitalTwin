package com.fablab.backend.commands.dto;

import com.fablab.backend.commands.CommandKey;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommandExecuteRequest(
        @NotNull UUID printerId,
        @NotNull CommandKey commandKey
) {
}