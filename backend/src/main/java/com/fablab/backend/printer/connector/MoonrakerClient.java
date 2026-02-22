package com.fablab.backend.printer.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Low-level HTTP client for Moonraker API communication.
 * Handles HTTP requests with API key authentication.
 */
@Component
public class MoonrakerClient {

    private static final Logger log = LoggerFactory.getLogger(MoonrakerClient.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;
    private static final int COMMAND_READ_TIMEOUT_MS = 60000; // 60s for commands like G28

    /**
     * Execute a GET request to the Moonraker API.
     *
     * @param baseUrl Base URL (e.g., "http://192.168.1.100:7125")
     * @param apiKey  API key for authentication
     * @param endpoint API endpoint (e.g., "/printer/info")
     * @return Raw JSON response as String
     * @throws Exception if request fails
     */
    public String get(String baseUrl, String apiKey, String endpoint) throws Exception {
        String fullUrl = baseUrl + endpoint;
        log.debug("GET request to: {}", fullUrl);

        HttpURLConnection conn = openConnection(fullUrl, apiKey);

        try {
            conn.setRequestMethod("GET");
            int status = conn.getResponseCode();

            if (status >= 200 && status < 300) {
                return readResponse(conn.getInputStream());
            }

            String errorBody = readResponse(conn.getErrorStream());
            log.warn("HTTP {} from {}: {}", status, fullUrl, errorBody);
            throw new Exception("HTTP " + status + ": " + errorBody);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Execute a POST request to the Moonraker API.
     *
     * @param baseUrl base URL of the printer
     * @param apiKey API key for authentication
     * @param endpoint endpoint path (e.g. /printer/gcode/script)
     * @param bodyJson JSON body payload
     * @return raw JSON response as String
     * @throws Exception if request fails
     */
    public String postJson(String baseUrl, String apiKey, String endpoint, String bodyJson) throws Exception {
        String fullUrl = baseUrl + endpoint;
        log.debug("POST request to: {}", fullUrl);

        HttpURLConnection conn = openConnection(fullUrl, apiKey);

        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyJson.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            
            if (status >= 200 && status < 300) {
                return readResponse(conn.getInputStream());
            } else {
                String errorBody = readResponse(conn.getErrorStream());
                log.warn("HTTP {} from {}: {}", status, fullUrl, errorBody);
                throw new Exception("HTTP " + status + ": " + errorBody);
            }

        } finally {
            conn.disconnect();
        }
    }

    /**
     * Execute a POST request to the Moonraker API (no request body).
     * Most Moonraker action endpoints (emergency stop, pause, reboot, etc.)
     * are triggered via POST with an empty body.
     *
     * @param baseUrl  Base URL (e.g., "http://192.168.1.100:7125")
     * @param apiKey   API key for authentication
     * @param endpoint API endpoint (e.g., "/printer/emergency_stop")
     * @return Raw JSON response as String
     * @throws Exception if request fails
     */
    public String post(String baseUrl, String apiKey, String endpoint) throws Exception {
        String fullUrl = baseUrl + endpoint;
        log.debug("POST request to: {}", fullUrl);

        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(COMMAND_READ_TIMEOUT_MS);
            conn.setDoOutput(true);

            if (apiKey != null && !apiKey.isEmpty()) {
                conn.setRequestProperty("X-Api-Key", apiKey);
            }

            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Length", "0");
            conn.setRequestProperty("User-Agent", "FabLab-DigitalTwin/1.0");

            int status = conn.getResponseCode();

            if (status >= 200 && status < 300) {
                return readResponse(conn.getInputStream());
            }

            String errorBody = readResponse(conn.getErrorStream());
            log.warn("HTTP {} from {}: {}", status, fullUrl, errorBody);
            throw new Exception("HTTP " + status + ": " + errorBody);
        } finally {
            conn.disconnect();
        }
    }

    private HttpURLConnection openConnection(String fullUrl, String apiKey) throws Exception {
        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);

        if (apiKey != null && !apiKey.isEmpty()) {
            conn.setRequestProperty("X-Api-Key", apiKey);
        }

        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "FabLab-DigitalTwin/1.0");
        return conn;
    }

    /**
     * Read response body from input stream.
     */
    private String readResponse(java.io.InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            return response.toString();
        }
    }

    /**
     * Test connectivity to a Moonraker instance.
     *
     * @param baseUrl Base URL of the printer
     * @param apiKey  API key (optional)
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection(String baseUrl, String apiKey) {
        try {
            String response = get(baseUrl, apiKey, "/server/info");
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            log.debug("Connection test failed for {}: {}", baseUrl, e.getMessage());
            return false;
        }
    }
}