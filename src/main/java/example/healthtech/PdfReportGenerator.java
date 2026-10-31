package example.healthtech;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class PdfReportGenerator {
    public String create(String patientReference, String appointmentDate, String status) {
        String text = "Appointment report\nPatient reference: " + patientReference
                + "\nDate: " + appointmentDate + "\nStatus: " + status;
        String safe = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("\n", ") Tj 0 -16 Td (");
        String body = "BT /F1 12 Tf 72 720 Td (" + safe + ") Tj ET";
        String pdf = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
                + "2 0 obj<</Type/Pages/Count 1/Kids[3 0 R]>>endobj\n"
                + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Resources<</Font<</F1 4 0 R>>>>/Contents 5 0 R>>endobj\n"
                + "4 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n"
                + "5 0 obj<</Length " + body.length() + ">>stream\n" + body + "\nendstream endobj\n"
                + "trailer<</Root 1 0 R>>\n%%EOF";
        return Base64.getEncoder().encodeToString(pdf.getBytes(StandardCharsets.US_ASCII));
    }
}
