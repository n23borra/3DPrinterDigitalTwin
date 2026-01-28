package com.fablab.backend.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fablab.backend.models.Alert;
import com.fablab.backend.models.User;
import com.fablab.backend.repositories.AlertRepository;
import com.fablab.backend.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertModuleService {

    private static class PrinterError {
        private final String errorCode;
        private final String message;
        private final String component;

        public PrinterError(String errorCode, String message, String component){
            this.errorCode = errorCode;
            this.message = message;
            this.component = component;
        }

        public String getErrorCode(){return errorCode;};
        public String getMessage(){return message;};
        public String getComponent(){return component;};
    }

    private static final String IP = "10.29.232.69";
    private static final int PORT = 4408;
    private final UserRepository userRepository;
    private final AuditLogService auditService;
    private final AlertRepository alertRepository;

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode status;
    // Optional human-friendly descriptions (can be extended)
    private static final Map<String, String> ERROR_DESCRIPTIONS = new HashMap<>();
    static {
        ERROR_DESCRIPTIONS.put("CX2573", "Anomalie du repérage de l'axe X — La limite de l'axe X n'est pas déclenchée. Vérifier si la limite est endommagée (la série K1 peut avoir un problème de limite souple, ce qui nécessite de vérifier si l'entraînement du moteur est endommagé).");
        ERROR_DESCRIPTIONS.put("CY2577", "Anomalie du repérage de l'axe Y — La limite de l'axe Y n'est pas déclenchée. Vérifier si la limite est endommagée (la série K1 peut présenter un problème de limite souple, ce qui nécessite de vérifier si l'entraînement du moteur est endommagé).");
        ERROR_DESCRIPTIONS.put("CZ2581", "Anomalie du repérage de l'axe Z — Causes possibles : problème de déclenchement du capteur de l'axe Z. Vérifier si le fil du lit chaud est tiré.");
        ERROR_DESCRIPTIONS.put("CX2585", "Coordonnées d'impression de l'axe X hors plage — 1) Sélectionner un logiciel de tranchage incorrect et refaire le tranchage en fonction de l'imprimante. 2) Utiliser un logiciel de découpage tiers et l'imprimante doit être configurée en fonction de la taille de l'imprimante.");
        ERROR_DESCRIPTIONS.put("CY2586", "Les coordonnées d'impression de l'axe Y sont en dehors de la plage — 1) Sélectionner un logiciel de découpage incorrect et redécouper en fonction de l'imprimante. 2) Utiliser un logiciel de découpage tiers et l'imprimante doit être configurée en stricte conformité avec la taille de l'imprimante.");
        ERROR_DESCRIPTIONS.put("CZ2587", "Les coordonnées d'impression de l'axe Z sont en dehors de la plage — 1) Sélectionner un logiciel de découpage incorrect et redécouper en fonction de l'imprimante. 2) Utilisez un logiciel de découpe tiers et la machine doit être configurée en fonction de la taille de l'imprimante.");
    }


    public void startAlertModule() throws Exception {
        // Try to get the currently authenticated user
        Long userId = null;
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = auth.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }
        
        // Make userId final for lambda expressions
        final Long finalUserId = userId;
        
        // If a user is authenticated, log the startup; otherwise create a system alert (userId=null)
        if (finalUserId != null) {
            auditService.logAction(finalUserId, "STARTING ALERT LOG", "");
        }

        // initial fetch
        status = query(
            "printer/objects/query" +
            "?print_stats" +
            "&heater_bed" +
            "&extruder" +
            "&toolhead" +
            "&stepper_x" +
            "&stepper_y" +
            "&stepper_z" +
            "&endstop_x" +
            "&endstop_y" +
            "&endstop_z" +
            "&output_pin%20fan0" +
            "&output_pin%20fan1" +
            "&output_pin%20fan2" +
            "&heater_fan%20hotend_fan=rpm" +
            "&z_tilt" +
            "&bed_mesh" 
        );


        // start a background monitor for heatbed errors (reads the shared static `status`)
        double prevTemp = Double.NaN;
        Thread heatbedMonitor = new Thread(() -> {
            monitorHeatbedErrors(prevTemp, finalUserId);
        });
        heatbedMonitor.setDaemon(true);
        heatbedMonitor.start();

        // start a separate monitor for unfinished-print / power-loss-recovery (CM0115)
        Thread powerLossMonitor = new Thread(() -> {
            monitorPowerLossRecoveryError(finalUserId);
        });
        powerLossMonitor.setDaemon(true);
        powerLossMonitor.start();

        // start axis homing anomaly monitor (X/Y/Z)
        Thread axisHomingMonitor = new Thread(() -> {
            monitorAxisHomingErrors(finalUserId);
        });
        axisHomingMonitor.setDaemon(true);
        axisHomingMonitor.start();
        
        // start axis coordinate-range anomaly monitor (X/Y/Z)
        Thread axisCoordMonitor = new Thread(() -> {
            monitorAxisCoordinateRangeErrors(finalUserId);
        });
        axisCoordMonitor.setDaemon(true);
        axisCoordMonitor.start();

        Thread fansMonitor = new Thread(() -> {
            checkFans(finalUserId);
        });
        fansMonitor.setDaemon(true);
        fansMonitor.start();

        Thread extruderMonitor = new Thread(() -> {
            checkExtruder(finalUserId);
        });
        extruderMonitor.setDaemon(true);
        extruderMonitor.start();


        // continuous polling loop: fetch status every second and analyze
        while (true) {
            status = query(
                "printer/objects/query" +
                "?print_stats" +
                "&heater_bed" +
                "&extruder" +
                "&toolhead" +
                "&stepper_x" +
                "&stepper_y" +
                "&stepper_z" +
                "&endstop_x" +
                "&endstop_y" +
                "&endstop_z" +
                "&output_pin%20fan0" +
                "&output_pin%20fan1" +
                "&output_pin%20fan2" +
                "&heater_fan%20hotend_fan=rpm" +
                "&z_tilt" +
                "&bed_mesh"
            );
            if (finalUserId != null) {
                auditService.logAction(finalUserId, "NEW_ALERT_LOG", "No alerts for now");
            }
            Thread.sleep(1000);
        }
    }

    // ---------------- HTTP ----------------

    private static JsonNode query(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://" + IP + ":" + PORT + "/" + endpoint))
            .GET()
            .build();

        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readTree(response.body()).get("result").get("status");
    }

    private JsonNode getHeatBedData() throws Exception {
       return status.at("/heater_bed");
    }
    private JsonNode getPrintStats() throws Exception {
        return status.at("/print_stats");
    }
    private JsonNode getToolhead() throws Exception {
        return status.at("/toolhead");
    }
    private JsonNode getExtruderData() throws Exception{
        return status.at("/extruder");
    }
    private JsonNode getStepperXData() throws Exception{
        return status.at("/stepper_x");
    }
    private JsonNode getStepperYData() throws Exception{
        return status.at("/stepper_y");
    }
    private JsonNode getStepperZData() throws Exception{
        return status.at("/stepper_z");
    }
    private JsonNode getEndstopXData() throws Exception{
        return status.at("/endstop_x");
    }
    private JsonNode getEndstopYData() throws Exception{
        return status.at("/endstop_y");
    }
    private JsonNode getEndstopZData() throws Exception{
        return status.at("/endstop_z");
    }
    private JsonNode getFan0Data() throws Exception{
        return status.at("/output_pin fan0");
    }
    private JsonNode getFan1Data() throws Exception{
        return status.at("/output_pin fan1");
    }
    private JsonNode getFan2Data() throws Exception{
        return status.at("/output_pin fan2");
    }
     private JsonNode getZTilt() throws Exception{
        return status.at("/z_tilt");
    }
    private JsonNode getBedMesh() throws Exception{
        return status.at("/bed_mesh");
    }


    // Monitor multiple heatbed errors concurrently and report when each persists
    // for the required consecutive duration.
    private void monitorHeatbedErrors(double initialTemp, Long userId) {
        final long requiredConsecutiveSeconds = 60; // require 60s of the same error
        double prevTemp = initialTemp; 
        Map<String, Long> candidateStart = new HashMap<>();
        Map<String, PrinterError> candidateError = new HashMap<>();
        Set<String> reported = new HashSet<>();

        while (true) {
            try {
                Thread.sleep(1000);

                JsonNode result = getHeatBedData();
                long now = System.currentTimeMillis();

                if (result == null || result.isMissingNode()) {
                    candidateStart.clear();
                    candidateError.clear();
                    reported.clear();
                    continue;
                }

                double newTemp = result.at("/temperature").asDouble();
                double newPower = result.at("/power").asDouble();
                double newTarget = result.at("/target").asDouble();

                // collect detected errors this tick
                Map<String, PrinterError> detected = new HashMap<>();

                if(newTarget > 0 && (newTemp < newTarget - 10) && newPower < 0.1){
                    PrinterError e = new PrinterError("CB2565", "Aucun courant détecté dans le lit chauffant", "heater_bed");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }
                if((newTemp < -5 || newTemp == 0)){
                    PrinterError e = new PrinterError("CB2510", "Circuit ouvert de la thermistance du lit chauffant. Remplacer le thermistor ou vérifier le fil thermique et le réinsérer.", "heater_bed");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }
                if(newTarget > 0 && (newTemp < newTarget - 10)){
                    PrinterError e = new PrinterError("CB2565", "Température trop basse, le capteur est peut être déconnecté", "heater_bed");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }
                if(newTarget > 0 && (newTemp > newTarget + 10)){
                    PrinterError e = new PrinterError("CB2565", "Température trop haute, le capteur est peut être déconnecté", "heater_bed");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }
                if(newTemp > 280){
                    PrinterError e = new PrinterError("CB2516", "Température > 280°, la thermistance est en court-circuit.", "heater_bed");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }
                if(!Double.isNaN(prevTemp) && newTarget > newTemp && newPower == 1.0 && newTemp - prevTemp < 1 ){
                    PrinterError e = new PrinterError("CB2565", "La température n'augmente pas.", "heater_bed");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }

                // Update candidates and reporting
                // Start or continue candidates
                for (Map.Entry<String, PrinterError> ent : detected.entrySet()) {
                    String key = ent.getKey();
                    PrinterError err = ent.getValue();
                    if (!candidateStart.containsKey(key)) {
                        candidateStart.put(key, now);
                        candidateError.put(key, err);
                    } else {
                        long elapsedSec = (now - candidateStart.get(key)) / 1000;
                        if (elapsedSec >= requiredConsecutiveSeconds && !reported.contains(key)) {
                            // report
                            System.out.println("Heatbed error detected: " + err.getErrorCode() + " - " + err.getMessage());
                            recordAlertAndAudit(userId, err.getErrorCode(), err.getMessage(), "HEATBED", Alert.Severity.WARNING);
                            reported.add(key);
                        }
                    }
                }

                // Clear candidates that are no longer detected
                Set<String> toRemove = new HashSet<>();
                for (String key : candidateStart.keySet()) {
                    if (!detected.containsKey(key)) {
                        toRemove.add(key);
                    }
                }
                for (String k : toRemove) {
                    candidateStart.remove(k);
                    candidateError.remove(k);
                    reported.remove(k);
                }

                prevTemp = newTemp;

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Separate monitor for power-loss-recovery / unfinished-print (CM0115)
    private void monitorPowerLossRecoveryError(Long userId) {
        final long requiredConsecutiveSeconds = 60;
        String candidateKey = null;
        long candidateStart = 0L;

        while (true) {
            try {
                Thread.sleep(1000);

                JsonNode s = getPrintStats();
                if (s == null || s.isMissingNode()) {
                    candidateKey = null;
                    candidateStart = 0L;
                    continue;
                }

                String state = s.at("/state").asText("unknown");
                String filename = s.at("/filename").asText(null);
                double progress = s.at("/progress").asDouble(-1);
                double printTime = s.at("/print_duration").asDouble(0);

                boolean hasFile = filename != null && !filename.isEmpty();
                boolean hasProgress = progress > 0 && progress < 100;
                boolean hasPrintTime = printTime > 0;

                boolean unfinishedCondition = hasFile && (hasProgress || hasPrintTime) && !state.equals("printing");

                long now = System.currentTimeMillis();

                if (!unfinishedCondition) {
                    candidateKey = null;
                    candidateStart = 0L;
                    continue;
                }

                String key = "CM0115|" + (filename == null ? "<unknown>" : filename);
                if (candidateKey != null && candidateKey.equals(key)) {
                    long elapsed = (now - candidateStart) / 1000;
                    if (elapsed >= requiredConsecutiveSeconds) {
                        // report once
                        System.out.println("CM0115 - Tâche inachevée détectée pour le fichier: " + filename);
                        System.out.println("Voulez-vous poursuivre l'impression ? Si l'imprimante s'est arrêtée en raison d'une coupure de courant, vous pouvez reprendre la tâche ou redémarrer l'impression.");
                        recordAlertAndAudit(userId, "CM0115", "Tâche inachevée détectée pour le fichier: " + filename, "POWER", Alert.Severity.CRITICAL);
                        // reset so we don't spam every second; require reappearance to report again
                        candidateKey = null;
                        candidateStart = 0L;
                    }
                } else {
                    candidateKey = key;
                    candidateStart = now;
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Monitor axis homing anomalies for X/Y/Z (CX2573/CY2577/CZ2581)
    private void monitorAxisHomingErrors(Long userId) {
        final long requiredConsecutiveSeconds = 60;

        Map<String, Long> candidateStart = new HashMap<>();
        Map<String, PrinterError> candidateError = new HashMap<>();
        Set<String> reported = new HashSet<>();

        while (true) {
            try {
                Thread.sleep(1000);

                JsonNode s = status;
                long now = System.currentTimeMillis();

                if (s == null || s.isMissingNode()) {
                    candidateStart.clear();
                    candidateError.clear();
                    reported.clear();
                    continue;
                }

                Map<String, PrinterError> detected = new HashMap<>();

                // X axis
                JsonNode sxLast = getStepperXData().at("/last_error");
                JsonNode ex = getEndstopXData();
                boolean exTriggered = !ex.isMissingNode() && ex.at("/triggered").asBoolean(false);
                if ((!sxLast.isMissingNode() && !sxLast.isNull()) || !exTriggered) {
                    PrinterError e = new PrinterError("CX2573", "Anomalie du repérage de l'axe X", "stepper_x");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }

                // Y axis
                JsonNode syLast = getStepperYData().at("/last_error");
                JsonNode ey = getEndstopYData();
                boolean eyTriggered = !ey.isMissingNode() && ey.at("/triggered").asBoolean(false);
                if ((!syLast.isMissingNode() && !syLast.isNull()) || !eyTriggered) {
                    PrinterError e = new PrinterError("CY2577", "Anomalie du repérage de l'axe Y", "stepper_y");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }

                // Z axis
                JsonNode szLast = getStepperZData().at("/last_error");
                JsonNode ez = getEndstopZData();
                boolean ezTriggered = !ez.isMissingNode() && ez.at("/triggered").asBoolean(false);
                if ((!szLast.isMissingNode() && !szLast.isNull()) || !ezTriggered) {
                    PrinterError e = new PrinterError("CZ2581", "Anomalie du repérage de l'axe Z", "stepper_z");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }

                // Update candidates and reporting
                for (Map.Entry<String, PrinterError> ent : detected.entrySet()) {
                    String key = ent.getKey();
                    PrinterError err = ent.getValue();
                    if (!candidateStart.containsKey(key)) {
                        candidateStart.put(key, now);
                        candidateError.put(key, err);
                    } else {
                        long elapsedSec = (now - candidateStart.get(key)) / 1000;
                        if (elapsedSec >= requiredConsecutiveSeconds && !reported.contains(key)) {
                            String code = err.getErrorCode();
                            String baseMsg = err.getMessage();
                            String desc = ERROR_DESCRIPTIONS.get(code);
                            System.out.println(code + " - " + baseMsg + ":");
                            recordAlertAndAudit(userId, code, baseMsg, "AXIS", Alert.Severity.WARNING);
                            if (desc != null) 
                                System.out.println(desc);
                            reported.add(key);
                        }
                    }
                }

                // Clear candidates no longer detected
                Set<String> toRemove = new HashSet<>();
                for (String key : candidateStart.keySet()) {
                    if (!detected.containsKey(key)) 
                        toRemove.add(key);
                }
                for (String k : toRemove) {
                    candidateStart.remove(k);
                    candidateError.remove(k);
                    reported.remove(k);
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Monitor axis coordinate-range anomalies for X/Y/Z (CX2585/CY2586/CZ2587)
    private void monitorAxisCoordinateRangeErrors(Long userId) {
        final long requiredConsecutiveSeconds = 60;

        Map<String, Long> candidateStart = new HashMap<>();
        Map<String, PrinterError> candidateError = new HashMap<>();
        Set<String> reported = new HashSet<>();

        while (true) {
            try {
                Thread.sleep(1000);

                JsonNode s = status;
                long now = System.currentTimeMillis();

                if (s == null || s.isMissingNode()) {
                    candidateStart.clear();
                    candidateError.clear();
                    reported.clear();
                    continue;
                }

                Map<String, PrinterError> detected = new HashMap<>();

                // Detect coordinate-range anomalies primarily via stepper last_error or explicit 'out_of_range' flags if present
                JsonNode sxLast = getStepperXData().at("/last_error");
                if (!sxLast.isMissingNode() && !sxLast.isNull()) {
                    PrinterError e = new PrinterError("CX2585", "Coordonnées d'impression de l'axe X hors plage", "stepper_x");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }

                JsonNode syLast = getStepperYData().at("/last_error");
                if (!syLast.isMissingNode() && !syLast.isNull()) {
                    PrinterError e = new PrinterError("CY2586", "Les coordonnées d'impression de l'axe Y sont en dehors de la plage", "stepper_y");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }

                JsonNode szLast = getStepperZData().at("/last_error");
                if (!szLast.isMissingNode() && !szLast.isNull()) {
                    PrinterError e = new PrinterError("CZ2587", "Les coordonnées d'impression de l'axe Z sont en dehors de la plage", "stepper_z");
                    detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                }

                // Update candidates and reporting
                for (Map.Entry<String, PrinterError> ent : detected.entrySet()) {
                    String key = ent.getKey();
                    PrinterError err = ent.getValue();
                    if (!candidateStart.containsKey(key)) {
                        candidateStart.put(key, now);
                        candidateError.put(key, err);
                    } else {
                        long elapsedSec = (now - candidateStart.get(key)) / 1000;
                        if (elapsedSec >= requiredConsecutiveSeconds && !reported.contains(key)) {
                            String code = err.getErrorCode();
                            String baseMsg = err.getMessage();
                            String desc = ERROR_DESCRIPTIONS.get(code);
                            System.out.println(code + " - " + baseMsg + ":");
                            recordAlertAndAudit(userId, code, baseMsg, "AXIS", Alert.Severity.WARNING);
                            if (desc != null) 
                                System.out.println(desc);
                            reported.add(key);
                        }
                    }
                }

                // Clear candidates no longer detected
                Set<String> toRemove = new HashSet<>();
                for (String key : candidateStart.keySet()) {
                    if (!detected.containsKey(key)) toRemove.add(key);
                }
                for (String k : toRemove) {
                    candidateStart.remove(k);
                    candidateError.remove(k);
                    reported.remove(k);
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private PrinterError checkBedLeveling(Long userId) {

        try{

            JsonNode zTilt = getZTilt();

            if (zTilt.isMissingNode() || !zTilt.at("/applied").asBoolean(false)) {
                String code = "ZT0001";
                String baseMsg = "Plateau non ajusté horizontalement : Z_TILT_ADJUST non appliqué";
                recordAlertAndAudit(userId, code, baseMsg, "LEVELING", Alert.Severity.INFO);
                return new PrinterError(
                    code,
                    baseMsg,
                    "z_tilt"
                );
            }

            JsonNode matrix = getBedMesh().at("/probed_matrix");

            if (!matrix.isArray()) {
                String code = "BM0000";
                String baseMsg = "Bed mesh absent ou invalide";
                recordAlertAndAudit(userId, code, baseMsg, "LEVELING", Alert.Severity.WARNING);
                return new PrinterError(
                    code,
                    baseMsg,
                    "bed_mesh"
                );
            }

            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            for (JsonNode row : matrix) {
                for (JsonNode value : row) {
                    double v = value.asDouble();
                    min = Math.min(min, v);
                    max = Math.max(max, v);
                }
            }

            double delta = max - min;

            double MAX_ACCEPTABLE_DELTA_MM = 0.6;

            if (delta > MAX_ACCEPTABLE_DELTA_MM) {
                String code = "BM0001";
                String baseMsg = String.format("Plateau trop bosselé malgré z_tilt (Δ=%.2f mm > %.2f mm)", delta, MAX_ACCEPTABLE_DELTA_MM);
                recordAlertAndAudit(userId, code, baseMsg, "LEVELING", Alert.Severity.WARNING);
                return new PrinterError(
                    code,
                    baseMsg,
                    "bed_mesh"
                );
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return new PrinterError("00000", "", "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new PrinterError("00000", "", "");
    }

    private void checkFans(Long userId) {

        final long requiredConsecutiveSeconds = 15;

        Map<String, Long> candidateStart = new HashMap<>();
        Map<String, PrinterError> candidateError = new HashMap<>();
        Set<String> reported = new HashSet<>();

        while (true) {
            try {
                Thread.sleep(1000);

                JsonNode s = status;
                long now = System.currentTimeMillis();

                if (s == null || s.isMissingNode()) {
                    candidateStart.clear();
                    candidateError.clear();
                    reported.clear();
                    continue;
                }

                Map<String, PrinterError> detected = new HashMap<>();
                String[] fans = {
                    "heater_fan hotend_fan",
                    "output_pin fan0",
                    "output_pin fan1",
                    "output_pin fan2"
                };

                for (String fanName : fans) {
                    JsonNode fan = s.get(fanName);
                    if (fan == null || fan.isMissingNode()) 
                        continue;

                    double speed = fan.at("/speed").asDouble(0.0);
                    int rpm = fan.at("/rpm").asInt(-1);

                    if (speed > 0.3 && rpm == 0) {
                        String code = "FN0001";
                        String baseMsg = "Ventilateur " + fanName + " commandé mais RPM = 0 (bloqué ou débranché)";
                        PrinterError e = new PrinterError(
                            code,
                            baseMsg,
                            fanName
                        );
                        detected.put(e.getErrorCode() + "|" + e.getMessage(), e);

                    }

                    // Ventilo à fond mais RPM trop bas
                    if (speed > 0.9 && rpm > 0 && rpm < 1000) {
                        String code = "FN0002";
                        String baseMsg = "Ventilateur " + fanName + " à fond mais RPM anormalement bas (" + rpm + ")";
                        PrinterError e = new PrinterError(
                            code,
                            baseMsg,
                            fanName
                        );
                        detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                    }
                }

                // Update candidates and reporting
                for (Map.Entry<String, PrinterError> ent : detected.entrySet()) {
                    String key = ent.getKey();
                    PrinterError err = ent.getValue();
                    if (!candidateStart.containsKey(key)) {
                        candidateStart.put(key, now);
                        candidateError.put(key, err);
                    } else {
                        long elapsedSec = (now - candidateStart.get(key)) / 1000;
                        if (elapsedSec >= requiredConsecutiveSeconds && !reported.contains(key)) {
                            String code = err.getErrorCode();
                            String baseMsg = err.getMessage();
                            String desc = ERROR_DESCRIPTIONS.get(code);
                            System.out.println(code + " - " + baseMsg + ":");
                            recordAlertAndAudit(userId, code, baseMsg, "FAN", Alert.Severity.WARNING);
                            if (desc != null) 
                                System.out.println(desc);
                            reported.add(key);
                        }
                    }
                }

                // Clear candidates no longer detected
                Set<String> toRemove = new HashSet<>();
                for (String key : candidateStart.keySet()) {
                    if (!detected.containsKey(key)) toRemove.add(key);
                }
                for (String k : toRemove) {
                    candidateStart.remove(k);
                    candidateError.remove(k);
                    reported.remove(k);
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void checkExtruder(Long userId) {

        final long requiredConsecutiveSeconds = 15;

        Map<String, Long> candidateStart = new HashMap<>();
        Map<String, PrinterError> candidateError = new HashMap<>();
        Set<String> reported = new HashSet<>();

        while (true) {
            try {
                Thread.sleep(1000);

                JsonNode s = status;
                long now = System.currentTimeMillis();

                if (s == null || s.isMissingNode()) {
                    candidateStart.clear();
                    candidateError.clear();
                    reported.clear();
                    continue;
                }

                Map<String, PrinterError> detected = new HashMap<>();
        
                JsonNode extruder = getExtruderData();

                if (extruder != null && !extruder.isMissingNode()) {
                    double temp = extruder.at("/temperature").asDouble();
                    double target = extruder.at("/target").asDouble();
                    double power = extruder.at("/power").asDouble();

                    // Chauffe demandée mais température ne monte pas
                    if (target > 0 && power > 0.8 && temp < target - 15) {
                        PrinterError e = new PrinterError(
                            "EX0001",
                            "Extrudeur chauffe mais température trop basse (cartouche ou thermistance)",
                            "extruder"
                        );
                        detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                    }

                    // Température absurde → thermistance HS
                    if (temp < -10 || temp > 320) {
                        PrinterError e =  new PrinterError(
                            "EX0002",
                            "Température extrudeur incohérente (sonde en court-circuit ou ouverte)",
                            "extruder"
                        );
                        detected.put(e.getErrorCode() + "|" + e.getMessage(), e);
                    }
                }

            // Update candidates and reporting
                for (Map.Entry<String, PrinterError> ent : detected.entrySet()) {
                    String key = ent.getKey();
                    PrinterError err = ent.getValue();
                    if (!candidateStart.containsKey(key)) {
                        candidateStart.put(key, now);
                        candidateError.put(key, err);
                    } else {
                        long elapsedSec = (now - candidateStart.get(key)) / 1000;
                        if (elapsedSec >= requiredConsecutiveSeconds && !reported.contains(key)) {
                            String code = err.getErrorCode();
                            String baseMsg = err.getMessage();
                            String desc = ERROR_DESCRIPTIONS.get(code);
                            System.out.println(code + " - " + baseMsg + ":");
                            recordAlertAndAudit(userId, code, baseMsg, "EXTRUDER AND TOOLHEAD", Alert.Severity.WARNING);
                            if (desc != null) 
                                System.out.println(desc);
                            reported.add(key);
                        }
                    }
                }

                // Clear candidates no longer detected
                Set<String> toRemove = new HashSet<>();
                for (String key : candidateStart.keySet()) {
                    if (!detected.containsKey(key)) toRemove.add(key);
                }
                for (String k : toRemove) {
                    candidateStart.remove(k);
                    candidateError.remove(k);
                    reported.remove(k);
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Helper method to record both an audit log entry and create an Alert.
     * Called when an error is detected and persisted.
     *
     * @param userId the user ID (system admin)
     * @param errorCode the error/alert code (e.g., CB2565, CM0115)
     * @param message the alert message/description
     * @param category optional category (e.g., HEATBED, AXIS, POWER)
     * @param severity alert severity level
     */
    private void recordAlertAndAudit(Long userId, String errorCode, String message, String category, Alert.Severity severity) {
        try {
            // Log to audit trail
            auditService.logAction(userId, "ALERT_" + errorCode, message);

            // Create an Alert entry
            Alert alert = Alert.builder()
                    .userId(userId)
                    .title(errorCode + " - " + message)
                    .details(message)
                    .category(category)
                    .severity(severity)
                    .priority(severity == Alert.Severity.CRITICAL ? Alert.Priority.HIGH : Alert.Priority.MEDIUM)
                    .resolved(false)
                    .build();
            alertRepository.save(alert);

            System.out.println("Alert recorded: " + errorCode + " for user " + userId);
        } catch (Exception e) {
            System.err.println("Failed to record alert: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
