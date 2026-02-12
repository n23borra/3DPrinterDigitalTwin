package com.fablab.backend.commands.dto;

import com.fablab.backend.commands.CommandKey;

public record AvailableCommandDTO(
        CommandKey commandKey,
        String label,
        String group,
        boolean dangerous
) {
}