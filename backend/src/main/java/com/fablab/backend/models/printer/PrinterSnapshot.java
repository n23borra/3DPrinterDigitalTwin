package com.fablab.backend.models.printer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Historical normalized snapshot captured from a printer connector.
 * Stores comprehensive printer state for digital twin and analytics.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "printer_snapshots")
public class PrinterSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "printer_id")
    private Printer printer;

    @Column(name = "ts", nullable = false)
    private Instant timestamp = Instant.now();

    // ===== TEMPERATURES =====
    @Column(name = "bed_temp")
    private Double bedTemp;

    @Column(name = "nozzle_temp")
    private Double nozzleTemp;

    @Column(name = "target_bed")
    private Double targetBed;

    @Column(name = "target_nozzle")
    private Double targetNozzle;

    @Column(name = "chamber_temp")
    private Double chamberTemp;

    // ===== TOOLHEAD POSITION (Critical for digital twin animation) =====
    @Column(name = "pos_x")
    private Double posX;

    @Column(name = "pos_y")
    private Double posY;

    @Column(name = "pos_z")
    private Double posZ;

    @Column(name = "pos_e")
    private Double posE;

    // ===== TOOLHEAD STATE =====
    @Column(name = "homed_axes")
    private String homedAxes;

    @Column(name = "max_velocity")
    private Double maxVelocity;

    @Column(name = "max_accel")
    private Double maxAccel;

    // ===== MOTION REPORT (for smooth animation) =====
    @Column(name = "live_velocity")
    private Double liveVelocity;

    @Column(name = "live_position_x")
    private Double livePositionX;

    @Column(name = "live_position_y")
    private Double livePositionY;

    @Column(name = "live_position_z")
    private Double livePositionZ;

    // ===== PRINT STATS =====
    @Column(name = "state")
    private String state;

    @Column(name = "filename")
    private String filename;

    @Column(name = "progress")
    private Double progress;

    @Column(name = "total_layers")
    private Integer totalLayers;

    @Column(name = "current_layer")
    private Integer currentLayer;

    @Column(name = "print_duration")
    private Long printDuration;

    @Column(name = "total_duration")
    private Long totalDuration;

    @Column(name = "filament_used")
    private Double filamentUsed;

    // ===== DISPLAY STATUS =====
    @Column(name = "display_progress")
    private Double displayProgress;

    // ===== FANS =====
    @Column(name = "part_fan_speed")
    private Double partFanSpeed;

    @Column(name = "part_fan_rpm")
    private Double partFanRPM;

    @Column(name = "hotend_fan_speed")
    private Double hotendFanSpeed;

    // ===== SENSORS =====
    @Column(name = "filament_detected")
    private Boolean filamentDetected;

    // ===== SYSTEM INFO =====
    @Column(name = "cpu_temp")
    private Double cpuTemp;

    @Column(name = "cpu_usage")
    private String cpuUsage;

    @Column(name = "mem_usage")
    private String memUsage;

    @Column(name = "system_uptime")
    private Long systemUptime;

    // ===== BED MESH =====
    @Column(name = "bed_mesh_profile")
    private String bedMeshProfile;

    // ===== RAW DATA (for debugging/analysis) =====
    @Column(columnDefinition = "jsonb")
    private String rawPayload;

    // ===== LEGACY FIELD (backwards compatibility) =====
    @Deprecated
    @Column(name = "z_height")
    private Double zHeight;
}