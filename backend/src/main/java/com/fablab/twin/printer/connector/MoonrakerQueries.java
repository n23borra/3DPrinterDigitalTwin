package com.fablab.twin.printer.connector;

import com.fasterxml.jackson.databind.JsonNode;

public class MoonrakerQueries {

    private final MoonrakerClient client;

    public MoonrakerQueries(MoonrakerClient client) {
        this.client = client;
    }

    public double getExtruderTemperature() throws Exception {
        JsonNode json = client.get("/printer/objects/query?extruder");
        return json.path("result").path("status")
                   .path("extruder").path("temperature").asDouble();
    }

    public double getBedTemperature() throws Exception {
        JsonNode json = client.get("/printer/objects/query?heater_bed");
        return json.path("result").path("status")
                   .path("heater_bed").path("temperature").asDouble();
    }

    public String getPrintState() throws Exception {
        JsonNode json = client.get("/printer/objects/query?print_stats");
        return json.path("result").path("status")
                   .path("print_stats").path("state").asText();
    }

    public double[] getToolheadPosition() throws Exception {
        JsonNode json = client.get("/printer/objects/query?toolhead");
        JsonNode pos = json.path("result").path("status")
                           .path("toolhead").path("position");
        return new double[] {
            pos.get(0).asDouble(),
            pos.get(1).asDouble(),
            pos.get(2).asDouble(),
            pos.get(3).asDouble()
        };
    }

    public double getProgress() throws Exception {
        JsonNode json = client.get("/printer/objects/query?display_status");
        return json.path("result").path("status")
                   .path("display_status").path("progress").asDouble();
    }
}
