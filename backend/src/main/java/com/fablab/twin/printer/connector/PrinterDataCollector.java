package com.fablab.twin.printer.connector;

public class PrinterDataCollector {

    private final MoonrakerQueries queries;
    private final PrinterState state;

    public PrinterDataCollector(MoonrakerQueries queries, PrinterState state) {
        this.queries = queries;
        this.state = state;
    }

    public void refreshTemperatures() throws Exception {
        state.setExtruderTemp(queries.getExtruderTemperature());
        state.setBedTemp(queries.getBedTemperature());
    }

    public void refreshPosition() throws Exception {
        double[] pos = queries.getToolheadPosition();
        state.setToolheadPosition(new Double[]{ pos[0], pos[1], pos[2], pos[3] });
    }

    public void refreshPrintStatus() throws Exception {
        state.setPrintState(queries.getPrintState());
        state.setProgress(queries.getProgress());
    }

    public void refreshAll() throws Exception {
        refreshTemperatures();
        refreshPosition();
        refreshPrintStatus();
    }
}
