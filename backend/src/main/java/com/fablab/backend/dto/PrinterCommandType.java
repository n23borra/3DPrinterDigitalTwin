package com.fablab.backend.dto;

/**
 * Types of commands that can be sent to a printer.
 * Used in PrinterCommandRequest to route to the correct Moonraker endpoint.
 */
public enum PrinterCommandType {

    /** Arbitrary G-code string (e.g. "G28", "M104 S200") */
    GCODE,

    /** Start printing the currently loaded file */
    PRINT_START,

    /** Pause the current print */
    PRINT_PAUSE,

    /** Resume a paused print */
    PRINT_RESUME,

    /** Cancel the current print */
    PRINT_CANCEL,

    /** Immediate emergency stop — kills all heaters and motors */
    EMERGENCY_STOP,

    /** Restart the Klipper firmware (keeps the host running) */
    FIRMWARE_RESTART,

    /** Full machine reboot */
    MACHINE_REBOOT
}
