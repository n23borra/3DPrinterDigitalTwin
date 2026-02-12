package com.fablab.backend.printer.connector;

import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Connector implementation for Moonraker/Klipper powered printers.
 * Fetches comprehensive printer data from multiple API endpoints.
 */
@Component
public class MoonrakerPrinterConnector implements PrinterConnector {

    private static final Logger log = LoggerFactory.getLogger(MoonrakerPrinterConnector.class);
    
    private final MoonrakerClient client;

    public MoonrakerPrinterConnector(MoonrakerClient client) {
        this.client = client;
    }

    @Override
    public PrinterType getType() {
        return PrinterType.MOONRAKER;
    }

    @Override
    public RawPrinterState fetchState(Printer printer) {
        String baseUrl = buildBaseUrl(printer);
        String apiKey = printer.getApiKey();
        Instant timestamp = Instant.now();
        
        RawPrinterState.RawPrinterStateBuilder builder = RawPrinterState.builder()
                .timestamp(timestamp);

        try {
            // Fetch printer state (high priority - 1Hz)
            fetchPrinterInfo(baseUrl, apiKey, builder);
            
            // Fetch temperatures (high priority - 1-2Hz)
            fetchExtruderData(baseUrl, apiKey, builder);
            fetchBedData(baseUrl, apiKey, builder);
            fetchChamberData(baseUrl, apiKey, builder);
            
            // Fetch toolhead position (CRITICAL - needs 5-10Hz in real implementation)
            fetchToolheadData(baseUrl, apiKey, builder);
            
            // Fetch print statistics (medium priority - 1Hz)
            fetchPrintStats(baseUrl, apiKey, builder);
            fetchDisplayStatus(baseUrl, apiKey, builder);
            
            // Fetch motion report (high priority for smooth animation - 5-10Hz)
            fetchMotionReport(baseUrl, apiKey, builder);
            
            // Fetch fan data (medium priority - 1Hz)
            fetchFanData(baseUrl, apiKey, builder);
            
            // Fetch sensors (medium priority - 1Hz)
            fetchFilamentSensor(baseUrl, apiKey, builder);
            
            // Fetch system info (low priority - 10s intervals)
            fetchSystemInfo(baseUrl, apiKey, builder);
            
            log.debug("Successfully fetched state from printer {}", printer.getName());
            
        } catch (Exception e) {
            log.warn("Failed to fetch complete state from printer {}: {}", 
                    printer.getName(), e.getMessage());
            // Return partial state - some data is better than none
        }

        return builder.build();
    }

    @Override
    public void sendCommand(Printer printer, String gcodeOrAction) {
        try {
            String baseUrl = buildBaseUrl(printer);
            String endpoint = "/printer/gcode/script?script=" + 
                    java.net.URLEncoder.encode(gcodeOrAction, "UTF-8");
            client.get(baseUrl, printer.getApiKey(), endpoint);
            log.info("Sent command '{}' to printer {}", gcodeOrAction, printer.getName());
        } catch (Exception e) {
            log.error("Failed to send command to printer {}: {}", 
                    printer.getName(), e.getMessage());
            throw new RuntimeException("Unable to send command", e);
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    private String buildBaseUrl(Printer printer) {
        return String.format("http://%s:%d", printer.getIpAddress(), printer.getPort());
    }

    /**
     * Fetch general printer info and state
     */
    private void fetchPrinterInfo(String baseUrl, String apiKey, 
                                   RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, "/printer/info");
            String state = extract(response, "\"state\"\\s*:\\s*\"([^\"]+)\"");
            builder.state(state);
        } catch (Exception e) {
            log.debug("Could not fetch printer info: {}", e.getMessage());
        }
    }

    /**
     * Fetch extruder/nozzle temperature data
     */
    private void fetchExtruderData(String baseUrl, String apiKey, 
                                    RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, "/printer/objects/query?extruder");
            builder.nozzleTemp(extractDouble(response, "\"temperature\"\\s*:\\s*([0-9.]+)"));
            builder.targetNozzle(extractDouble(response, "\"target\"\\s*:\\s*([0-9.]+)"));
        } catch (Exception e) {
            log.debug("Could not fetch extruder data: {}", e.getMessage());
        }
    }

    /**
     * Fetch heated bed temperature data
     */
    private void fetchBedData(String baseUrl, String apiKey, 
                              RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, "/printer/objects/query?heater_bed");
            builder.bedTemp(extractDouble(response, "\"temperature\"\\s*:\\s*([0-9.]+)"));
            builder.targetBed(extractDouble(response, "\"target\"\\s*:\\s*([0-9.]+)"));
        } catch (Exception e) {
            log.debug("Could not fetch bed data: {}", e.getMessage());
        }
    }

    /**
     * Fetch chamber temperature (if available)
     */
    private void fetchChamberData(String baseUrl, String apiKey, 
                                   RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, 
                    "/printer/objects/query?temperature_sensor%20chamber_temp");
            builder.chamberTemp(extractDouble(response, "\"temperature\"\\s*:\\s*([0-9.]+)"));
        } catch (Exception e) {
            // Chamber sensor may not exist - this is normal
            log.trace("No chamber temperature sensor");
        }
    }

    /**
     * Fetch toolhead position and limits (CRITICAL for digital twin)
     */
    private void fetchToolheadData(String baseUrl, String apiKey, 
                                    RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, "/printer/objects/query?toolhead");
            
            // Position array: [x, y, z, e]
            builder.posX(extractDouble(response, "\"position\"\\s*:\\s*\\[([0-9.]+)"));
            builder.posY(extractDouble(response, "\"position\"\\s*:\\s*\\[[0-9.]+,\\s*([0-9.]+)"));
            builder.posZ(extractDouble(response, "\"position\"\\s*:\\s*\\[[0-9.]+,\\s*[0-9.]+,\\s*([0-9.]+)"));
            builder.posE(extractDouble(response, "\"position\"\\s*:\\s*\\[[0-9.]+,\\s*[0-9.]+,\\s*[0-9.]+,\\s*([0-9.]+)"));
            
            // Homed axes (string like "xyz")
            builder.homedAxes(extract(response, "\"homed_axes\"\\s*:\\s*\"([^\"]+)\""));
            
            // Velocity and acceleration limits
            builder.maxVelocity(extractDouble(response, "\"max_velocity\"\\s*:\\s*([0-9.]+)"));
            builder.maxAccel(extractDouble(response, "\"max_accel\"\\s*:\\s*([0-9.]+)"));
            
        } catch (Exception e) {
            log.debug("Could not fetch toolhead data: {}", e.getMessage());
        }
    }

    /**
     * Fetch print statistics (file, state, progress)
     */
    private void fetchPrintStats(String baseUrl, String apiKey, 
                                  RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, "/printer/objects/query?print_stats");
            
            builder.filename(extract(response, "\"filename\"\\s*:\\s*\"([^\"]+)\""));
            builder.state(extract(response, "\"state\"\\s*:\\s*\"([^\"]+)\""));
            
            // Progress (0.0 to 1.0, convert to percentage)
            Double progress = extractDouble(response, "\"print_duration\"\\s*:\\s*([0-9.]+)");
            if (progress != null) {
                builder.printDuration(progress.longValue());
            }
            
            Double totalDuration = extractDouble(response, "\"total_duration\"\\s*:\\s*([0-9.]+)");
            if (totalDuration != null) {
                builder.totalDuration(totalDuration.longValue());
            }
            
            builder.filamentUsed(extractDouble(response, "\"filament_used\"\\s*:\\s*([0-9.]+)"));
            
        } catch (Exception e) {
            log.debug("Could not fetch print stats: {}", e.getMessage());
        }
    }

    /**
     * Fetch display progress percentage
     */
    private void fetchDisplayStatus(String baseUrl, String apiKey, 
                                     RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, 
                    "/printer/objects/query?display_status");
            
            Double progress = extractDouble(response, "\"progress\"\\s*:\\s*([0-9.]+)");
            if (progress != null) {
                builder.displayProgress(progress * 100.0); // Convert to percentage
                builder.progress(progress * 100.0); // Also set main progress field
            }
            
        } catch (Exception e) {
            log.debug("Could not fetch display status: {}", e.getMessage());
        }
    }

    /**
     * Fetch motion report (live velocity and position for smooth animation)
     */
    private void fetchMotionReport(String baseUrl, String apiKey, 
                                    RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, 
                    "/printer/objects/query?motion_report");
            
            // Live position
            builder.livePositionX(extractDouble(response, 
                    "\"live_position\"\\s*:\\s*\\[([0-9.]+)"));
            builder.livePositionY(extractDouble(response, 
                    "\"live_position\"\\s*:\\s*\\[[0-9.]+,\\s*([0-9.]+)"));
            builder.livePositionZ(extractDouble(response, 
                    "\"live_position\"\\s*:\\s*\\[[0-9.]+,\\s*[0-9.]+,\\s*([0-9.]+)"));
            
            // Live velocity
            builder.liveVelocity(extractDouble(response, "\"live_velocity\"\\s*:\\s*([0-9.]+)"));
            
        } catch (Exception e) {
            log.debug("Could not fetch motion report: {}", e.getMessage());
        }
    }

    /**
     * Fetch fan data (speeds and RPM feedback)
     */
    private void fetchFanData(String baseUrl, String apiKey, 
                              RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, 
                    "/printer/objects/query?fan_feedback");
            
            builder.partFanSpeed(extractDouble(response, "\"speed\"\\s*:\\s*([0-9.]+)"));
            builder.partFanRPM(extractDouble(response, "\"rpm\"\\s*:\\s*([0-9.]+)"));
            
        } catch (Exception e) {
            log.trace("Could not fetch fan feedback: {}", e.getMessage());
        }
    }

    /**
     * Fetch filament sensor state
     */
    private void fetchFilamentSensor(String baseUrl, String apiKey, 
                                      RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, 
                    "/printer/objects/query?filament_switch_sensor%20filament_sensor");
            
            String enabled = extract(response, "\"enabled\"\\s*:\\s*(true|false)");
            String detected = extract(response, "\"filament_detected\"\\s*:\\s*(true|false)");
            
            if ("true".equals(enabled) && detected != null) {
                builder.filamentDetected("true".equals(detected));
            }
            
        } catch (Exception e) {
            log.trace("Could not fetch filament sensor: {}", e.getMessage());
        }
    }

    /**
     * Fetch system information (CPU, memory, uptime)
     */
    private void fetchSystemInfo(String baseUrl, String apiKey, 
                                  RawPrinterState.RawPrinterStateBuilder builder) {
        try {
            String response = client.get(baseUrl, apiKey, "/machine/proc_stats");
            
            builder.cpuTemp(extractDouble(response, "\"cpu_temp\"\\s*:\\s*([0-9.]+)"));
            builder.cpuUsage(extract(response, "\"cpu\"\\s*:\\s*([0-9.]+)"));
            builder.memUsage(extract(response, "\"memory\"\\s*:\\s*\"([^\"]+)\""));
            
        } catch (Exception e) {
            log.trace("Could not fetch system info: {}", e.getMessage());
        }
    }

    // ===== REGEX EXTRACTION HELPERS =====

    /**
     * Extract a string value from JSON using regex
     */
    private String extract(String text, String regex) {
        if (text == null) return null;
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Extract a double value from JSON using regex
     */
    private Double extractDouble(String text, String regex) {
        try {
            String result = extract(text, regex);
            return (result != null) ? Double.parseDouble(result) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}