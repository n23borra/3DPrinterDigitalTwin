import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestPrinterQuery {

    private static final String IP = "10.29.232.69";
    private static final String PORT = "4408";
    private static final String TOKEN = "6da0b675d36f4448a89cfd4f2fa3f080";

    public static void main(String[] args) throws IOException, InterruptedException {

        String endpoint = "/printer/info";  // Try others later

        String url = "http://" + IP + ":" + PORT + endpoint;

        System.out.println("Querying: " + url);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Api-Key", TOKEN)
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + response.statusCode());
        System.out.println("Body:");
        System.out.println(response.body());
    }
}
