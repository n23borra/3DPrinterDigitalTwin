public class ConnectorTestRunner {

    public static void main(String[] args) {

        String ip = "10.29.232.69";
        int port = 4408;
        String apiKey = "6da0b675d36f4448a89cfd4f2fa3f080";

        MoonrakerClient client = new MoonrakerClient(ip, port, apiKey);
        MoonrakerPrinterConnector connector = new MoonrakerPrinterConnector(client);

        PrinterState state = connector.fetchState();

        System.out.println("\n===== PRINTER STATE OBJECT =====");
        System.out.println(state);
    }
}
