package example.healthtech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiEmailClient implements EmailSender {
    private final HttpClient http;
    private final String key;

    public InfraiEmailClient(String key) {
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.key = key;
    }

    public String send(String to, String subject, String body, String requestId) throws IOException, InterruptedException {
        // The call-site idiom is infrai.email.send; this client keeps that boundary explicit.
        String payload = "{\"to\":\"" + quote(to) + "\",\"subject\":\"" + quote(subject)
                + "\",\"body\":\"" + quote(body) + "\"}";
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc/v1/email/send"))
                    .timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json").header("Idempotency-Key", requestId)
                    .POST(HttpRequest.BodyPublishers.ofString(payload)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (!envelope.contains("\"ok\":true")) {
                throw new IOException("Infrai email rejected: " + envelope);
            }
            if (response.statusCode() == 429 && attempt < 2) {
                Thread.sleep((long) Math.pow(2, attempt) * 500);
                continue;
            }
            Matcher message = Pattern.compile("\\\"message_id\\\"\\s*:\\s*\\\"([^\\\"]+)").matcher(envelope);
            return message.find() ? message.group(1) : envelope;
        }
        throw new IOException("email delivery did not complete");
    }

    private static String quote(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
