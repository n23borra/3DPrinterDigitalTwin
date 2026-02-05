import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MoonrakerClient {

    private final String baseUrl;
    private final String apiKey;

    public MoonrakerClient(String host, int port, String apiKey) {
        this.baseUrl = "http://" + host + ":" + port;
        this.apiKey = apiKey;
    }

    public String get(String endpoint) throws Exception {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Api-Key", apiKey);

        int status = conn.getResponseCode();
        BufferedReader reader;

        if (status >= 200 && status < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        return response.toString();
    }
}
