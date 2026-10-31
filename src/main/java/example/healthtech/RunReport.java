package example.healthtech;

public final class RunReport {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        String to = System.getenv("REPORT_EMAIL_TO");
        if (key == null || key.isBlank() || to == null || to.isBlank()) {
            throw new IllegalArgumentException("INFRAI_API_KEY and REPORT_EMAIL_TO are required");
        }
        var appointment = new AppointmentNotificationService.Appointment(
                "appointment-2026-001", to, "patient-4821", "2026-10-24", "CONFIRMED");
        var service = new AppointmentNotificationService(new PdfReportGenerator(), new InfraiEmailClient(key));
        System.out.println("message_id=" + service.notify(appointment));
    }
}
