package com.fablab.backend.printer.connector;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Raw state returned by a connector before persistence or normalization.
 * Contains comprehensive printer data from Moonraker API for digital twin.
 */
@Value
@Builder
public class RawPrinterState {
    
    // ===== TIMESTAMPS =====
    Instant timestamp;
    
    // ===== TEMPERATURES =====
    Double bedTemp;
    Double targetBed;
    Double nozzleTemp;
    Double targetNozzle;
    Double chamberTemp;  // NEW - chamber temperature (if available)
    
    // ===== TOOLHEAD POSITION (Critical for real-time animation) =====
    Double posX;         // NEW - X position
    Double posY;         // NEW - Y position
    Double posZ;         // NEW - Z position (was zHeight before)
    Double posE;         // NEW - Extruder position
    
    // ===== TOOLHEAD STATE =====
    String homedAxes;    // NEW - which axes are homed (e.g., "xyz")
    Double maxVelocity;  // NEW - max velocity limit
    Double maxAccel;     // NEW - max acceleration limit
    
    // ===== MOTION REPORT (for smooth animation) =====
    Double liveVelocity; // NEW - current velocity
    Double livePositionX; // NEW - live position from motion_report
    Double livePositionY; // NEW
    Double livePositionZ; // NEW
    
    // ===== PRINT STATS =====
    String state;        // printing, paused, standby, complete, error
    String filename;     // NEW - current file being printed
    Double progress;     // Print progress percentage (0-100)
    Integer totalLayers; // NEW - total layers in print
    Integer currentLayer; // NEW - current layer number
    Long printDuration;  // NEW - print duration in seconds
    Long totalDuration;  // NEW - estimated total duration
    Double filamentUsed; // NEW - filament used in mm
    
    // ===== DISPLAY STATUS =====
    Double displayProgress; // NEW - progress from display_status
    
    // ===== FANS =====
    Double partFanSpeed;    // NEW - part cooling fan (0-1)
    Double partFanRPM;      // NEW - part fan actual RPM
    Double hotendFanSpeed;  // NEW - hotend fan speed
    
    // ===== SENSORS =====
    Boolean filamentDetected; // NEW - filament sensor state
    
    // ===== SYSTEM INFO (for monitoring) =====
    Double cpuTemp;      // NEW - CPU temperature
    String cpuUsage;     // NEW - CPU usage percentage
    String memUsage;     // NEW - Memory usage
    Long systemUptime;   // NEW - System uptime in seconds
    
    // ===== BED MESH (static data, loaded once) =====
    String bedMeshProfile;   // active bed mesh profile name
    String bedMeshMin;       // mesh area min [x, y] as JSON string
    String bedMeshMax;       // mesh area max [x, y] as JSON string
    String bedMeshMatrix;    // probed Z-offset matrix as JSON 2D array

    // ===== Z-TILT =====
    Boolean zTiltApplied;    // whether Z-tilt adjustment has been applied
    
    // ===== RAW DATA =====
    String rawPayload;   // Complete JSON response for debugging
    
    // ===== LEGACY FIELD (keep for backwards compatibility) =====
    @Deprecated
    Double zHeight;      // Use posZ instead
    
    // ===== BUILDER HELPER =====
    public static class RawPrinterStateBuilder {
        // Auto-set zHeight from posZ for backwards compatibility
        public RawPrinterStateBuilder posZ(Double posZ) {
            this.posZ = posZ;
            this.zHeight = posZ; // keep in sync
            return this;
        }
    }
}