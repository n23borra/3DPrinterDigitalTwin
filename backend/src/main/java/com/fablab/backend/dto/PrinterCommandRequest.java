package com.fablab.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for issuing G-code commands to a printer.
 */
@Getter
@Setter
public class PrinterCommandRequest {

    @NotBlank
    private String command;
}