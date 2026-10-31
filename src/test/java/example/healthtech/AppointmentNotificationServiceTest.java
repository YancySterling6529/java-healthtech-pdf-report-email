package example.healthtech;

public final class AppointmentNotificationServiceTest {
    public static void main(String[] args) throws Exception {
        var service = new AppointmentNotificationService(new PdfReportGenerator(), (to, subject, body, id) -> "local-message");
        var pending = new AppointmentNotificationService.Appointment("a-1", "patient@example.test", "p-1", "2026-10-24", "PENDING");
        if (!"SKIPPED".equals(service.notify(pending))) throw new AssertionError("pending appointments must not notify");
        var confirmed = new AppointmentNotificationService.Appointment("a-2", "patient@example.test", "p-2", "2026-10-24", "CONFIRMED");
        if (!"local-message".equals(service.notify(confirmed))) throw new AssertionError("confirmed appointments must notify");
        System.out.println("appointment decision test passed");
    }
}
