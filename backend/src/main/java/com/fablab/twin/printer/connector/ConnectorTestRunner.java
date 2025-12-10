package com.fablab.twin.printer.connector;

import com.fablab.twin.domain.model.Printer;

public class ConnectorTestRunner {

    public static void main(String[] args) {

        Printer printer = new Printer();
        printer.setName("K2Plus-Test");
        printer.setIp("10.29.232.69");
        printer.setPort(4408);
        printer.setApiKey("6da0b675d36f4448a89cfd4f2fa3f080");

        MoonrakerPrinterConnector connector = new MoonrakerPrinterConnector();

        System.out.println("\n==== TEST 1 : /printer/info ====");
        System.out.println(connector.fetchPrinterInfo(printer));

        System.out.println("\n==== TEST 2 : /extruder and heater_bed ====");
        System.out.println(connector.fetchTemperatures(printer));

        System.out.println("\n==== TEST 3 : toolhead / position ====");
        System.out.println(connector.fetchToolhead(printer));

        System.out.println("\n==== TEST 4 : print statistics ====");
        System.out.println(connector.fetchPrintStats(printer));

        System.out.println("\n==== TEST 5 : motion report ====");
        System.out.println(connector.fetchMotion(printer));

        System.out.println("\n==== TEST 6 : bed mesh ====");
        System.out.println(connector.fetchBedMesh(printer));

        System.out.println("\n==== TEST 7 : complete snapshot ====");
        System.out.println(connector.fetchSnapshot(printer));

        System.out.println("\n===== ALL TESTS EXECUTED =====");
    }
}
