package com.fablab.twin.printer.connector;

public class MoonrakerConfig {

    private final String baseUrl;
    private final String apiKey;

    public MoonrakerConfig() {
        this.baseUrl = "http://10.29.232.69:4408"; 
        this.apiKey  = "6da0b675d36f4448a89cfd4f2fa3f080";
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}
