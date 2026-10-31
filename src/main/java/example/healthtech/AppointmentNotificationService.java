package example.healthtech;

import java.io.IOException;

public final class AppointmentNotificationService {
    private final PdfReportGenerator reports;
    private final EmailSender email;

    public AppointmentNotificationService(PdfReportGenerator reports, EmailSender email) {
        this.reports = reports;
        this.email = email;
    }

    public String notify(Appointment appointment) throws IOException, InterruptedException {
        if (!appointment.confirmed()) return "SKIPPED";
        String pdf = reports.create(appointment.patientReference(), appointment.date(), appointment.status());
        return email.send(appointment.to(), "Appointment report", "PDF report (base64): " + pdf, appointment.id());
    }

    public record Appointment(String id, String to, String patientReference, String date, String status) {
        public boolean confirmed() { return "CONFIRMED".equals(status); }
    }
}
