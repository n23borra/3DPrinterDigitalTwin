package com.fablab.twin.printer.connector;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MoonrakerClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final MoonrakerConfig config;

    public MoonrakerClient(MoonrakerConfig config) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.config = config;
    }

    public JsonNode get(String endpoint) throws Exception {

        String url = config.getBaseUrl() + endpoint;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Api-Key", config.getApiKey())
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readTree(response.body());
    }
}
