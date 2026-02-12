package com.fablab.backend.commands;

public record CommandDefinition(
        CommandKey key,
        String label,
        String group,
        String script,
        boolean requiresMacro,
        String macroName,
        boolean dangerous
) {
}