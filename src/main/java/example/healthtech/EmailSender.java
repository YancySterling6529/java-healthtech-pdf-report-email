package example.healthtech;

import java.io.IOException;

@FunctionalInterface
public interface EmailSender {
    String send(String to, String subject, String body, String requestId) throws IOException, InterruptedException;
}
