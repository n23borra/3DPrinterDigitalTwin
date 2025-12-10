package com.fablab.twin.printer.connector;

public class PrinterState {

    private Double extruderTemp;
    private Double bedTemp;
    private String printState;
    private Double[] toolheadPosition;
    private Double progress;

    public Double getExtruderTemp() { return extruderTemp; }
    public void setExtruderTemp(Double v) { extruderTemp = v; }

    public Double getBedTemp() { return bedTemp; }
    public void setBedTemp(Double v) { bedTemp = v; }

    public String getPrintState() { return printState; }
    public void setPrintState(String s) { printState = s; }

    public Double[] getToolheadPosition() { return toolheadPosition; }
    public void setToolheadPosition(Double[] p) { toolheadPosition = p; }

    public Double getProgress() { return progress; }
    public void setProgress(Double p) { progress = p; }
}
