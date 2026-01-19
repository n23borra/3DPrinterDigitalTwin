import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoonrakerPrinterConnector {

    private final MoonrakerClient client;

    public MoonrakerPrinterConnector(MoonrakerClient client) {
        this.client = client;
    }

    public PrinterState fetchState() {
        PrinterState state = new PrinterState();

        try {
            String printerInfo = client.get("/printer/info");
            state.state = extract(printerInfo, "\"state\"\\s*:\\s*\"([^\"]+)\"");
        } catch (Exception e) {
            System.out.println("Error fetching /printer/info: " + e.getMessage());
        }

        try {
            String extruder = client.get("/printer/objects/query?extruder");
            state.nozzleTemp = extractDouble(extruder, "\"temperature\"\\s*:\\s*([0-9.]+)");
        } catch (Exception e) {
            System.out.println("Error fetching extruder: " + e.getMessage());
        }

        try {
            String bed = client.get("/printer/objects/query?heater_bed");
            state.bedTemp = extractDouble(bed, "\"temperature\"\\s*:\\s*([0-9.]+)");
        } catch (Exception e) {
            System.out.println("Error fetching heater_bed: " + e.getMessage());
        }

        try {
            String toolhead = client.get("/printer/objects/query?toolhead");
            state.posX = extractDouble(toolhead, "\"position\"\\s*:\\s*\\[([0-9.]+)");
            state.posY = extractDouble(toolhead, "\"position\"\\s*:\\s*\\[[0-9.]+,\\s*([0-9.]+)");
            state.posZ = extractDouble(toolhead, "\"position\"\\s*:\\s*\\[[0-9.]+,\\s*[0-9.]+,\\s*([0-9.]+)");
        } catch (Exception e) {
            System.out.println("Error fetching toolhead: " + e.getMessage());
        }

        return state;
    }

    private String extract(String text, String regex) {
        if (text == null) return null;
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private Double extractDouble(String text, String regex) {
        try {
            String result = extract(text, regex);
            return (result != null) ? Double.parseDouble(result) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
