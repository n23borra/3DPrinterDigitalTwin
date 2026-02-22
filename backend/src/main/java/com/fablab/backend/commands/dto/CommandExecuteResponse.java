package com.fablab.backend.commands.dto;

import java.time.Instant;

public record CommandExecuteResponse(boolean ok, String message, Instant executedAt) {
}