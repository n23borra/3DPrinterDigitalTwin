package com.fablab.backend.dto;

import com.fablab.backend.models.printer.PrinterType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePrinterRequest {

    @NotBlank
    private String name;

    @NotNull
    private PrinterType type;

    @NotBlank
    private String ipAddress;

    @Min(1)
    @Max(65535)
    private Integer port;

    private String apiKey;
}