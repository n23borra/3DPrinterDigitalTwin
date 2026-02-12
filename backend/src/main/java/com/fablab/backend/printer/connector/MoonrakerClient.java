package com.fablab.backend.printer.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Low-level HTTP client for Moonraker API communication.
 * Handles HTTP GET requests with API key authentication.
 */
@Component
public class MoonrakerClient {

    private static final Logger log = LoggerFactory.getLogger(MoonrakerClient.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

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

        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            // Configure request
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            
            // Add API key header if provided
            if (apiKey != null && !apiKey.isEmpty()) {
                conn.setRequestProperty("X-Api-Key", apiKey);
            }
            
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "FabLab-DigitalTwin/1.0");

            // Get response
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