package com.immo.common.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class PdfDocumentService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    public byte[] contractPdf(
            String agencyName,
            String propertyTitle,
            String tenantName,
            String tenantEmail,
            LocalDate startDate,
            LocalDate endDate,
            String monthlyRent,
            String deposit,
            String status) {
        List<String> lines = new ArrayList<>();
        lines.add("CONTRAT DE LOCATION");
        lines.add("");
        lines.add("Agence: " + blank(agencyName));
        lines.add("Locataire: " + blank(tenantName));
        lines.add("Email locataire: " + blank(tenantEmail));
        lines.add("Bien loue: " + blank(propertyTitle));
        lines.add("");
        lines.add("Date de debut: " + formatDate(startDate));
        lines.add("Date de fin: " + formatDate(endDate));
        lines.add("Loyer mensuel: " + blank(monthlyRent));
        lines.add("Depot de garantie: " + blank(deposit));
        lines.add("Statut: " + blank(status));
        lines.add("");
        lines.add("Clauses principales");
        lines.add("Le locataire s'engage a regler le loyer aux echeances convenues.");
        lines.add("L'agence confirme la mise a disposition du bien indique ci-dessus.");
        lines.add("Ce document est genere automatiquement par Kermanager.");
        lines.add("");
        lines.add("Signature agence: ____________________");
        lines.add("Signature locataire: ________________");
        return simplePdf(lines);
    }

    public byte[] receiptPdf(
            String agencyName,
            String propertyTitle,
            String tenantName,
            String period,
            String amount,
            LocalDate issuedAt,
            String reference) {
        List<String> lines = new ArrayList<>();
        lines.add("QUITTANCE DE LOYER");
        lines.add("");
        lines.add("Agence: " + blank(agencyName));
        lines.add("Locataire: " + blank(tenantName));
        lines.add("Bien loue: " + blank(propertyTitle));
        lines.add("Periode: " + blank(period));
        lines.add("Montant regle: " + blank(amount));
        lines.add("Date d'emission: " + formatDate(issuedAt));
        lines.add("Reference: " + blank(reference));
        lines.add("");
        lines.add("Nous attestons avoir recu le paiement du loyer pour la periode indiquee.");
        lines.add("Cette quittance est generee automatiquement par Kermanager.");
        return simplePdf(lines);
    }

    private byte[] simplePdf(List<String> lines) {
        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 18 Tf\n50 790 Td\n");
        int index = 0;
        for (String line : lines) {
            if (index == 1) {
                content.append("/F1 11 Tf\n");
            }
            content.append("(").append(escapePdf(toPdfText(line))).append(") Tj\n");
            content.append("0 -24 Td\n");
            index++;
        }
        content.append("ET\n");

        byte[] stream = content.toString().getBytes(StandardCharsets.ISO_8859_1);
        List<byte[]> objects = List.of(
                "<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.ISO_8859_1),
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>".getBytes(StandardCharsets.ISO_8859_1),
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>".getBytes(StandardCharsets.ISO_8859_1),
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".getBytes(StandardCharsets.ISO_8859_1),
                ("<< /Length " + stream.length + " >>\nstream\n" + new String(stream, StandardCharsets.ISO_8859_1) + "endstream").getBytes(StandardCharsets.ISO_8859_1));

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        write(pdf, "%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.size());
            write(pdf, (i + 1) + " 0 obj\n");
            pdf.writeBytes(objects.get(i));
            write(pdf, "\nendobj\n");
        }

        int xrefOffset = pdf.size();
        write(pdf, "xref\n0 " + (objects.size() + 1) + "\n");
        write(pdf, "0000000000 65535 f \n");
        for (int i = 1; i < offsets.size(); i++) {
            write(pdf, String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(i)));
        }
        write(pdf, "trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n");
        write(pdf, "startxref\n" + xrefOffset + "\n%%EOF");
        return pdf.toByteArray();
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FORMATTER.format(date);
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String toPdfText(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("’", "'")
                .replace("€", "EUR");
    }

    private String escapePdf(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private void write(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.ISO_8859_1));
    }
}
