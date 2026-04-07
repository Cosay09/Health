package com.hms.util;

import com.hms.dao.AppointmentDAO;
import com.hms.dao.BillingDAO;
import com.hms.dao.DoctorDAO;
import com.hms.dao.LabTestDAO;
import com.hms.model.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {

    // ── Shared fonts ─────────────────────────────────────────
    private static final Font FONT_TITLE    = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE);
    private static final Font FONT_SUBTITLE = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font FONT_HEADER   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    private static final Font FONT_CELL     = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, BaseColor.BLACK);
    private static final Font FONT_LABEL    = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

    private static final BaseColor COLOR_PRIMARY = new BaseColor(83, 52, 131);  // #533483
    private static final BaseColor COLOR_ROW_ALT = new BaseColor(245, 245, 250);

    // ── PRESCRIPTION REPORT ──────────────────────────────────
    public static void generatePrescriptionPDF(Prescription prescription, String outputPath)
            throws Exception {

        Document doc = new Document(PageSize.A5);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        addHeader(doc, "Prescription", "ID: " + prescription.getPrescriptionId());

        // Patient and doctor info block
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(12);
        addInfoRow(infoTable, "Patient",    prescription.getPatientName());
        addInfoRow(infoTable, "Doctor",     "Dr. " + prescription.getDoctorName());
        addInfoRow(infoTable, "Date",       prescription.getDate().toString());
        addInfoRow(infoTable, "Appt. ID",   String.valueOf(prescription.getAppointmentId()));
        doc.add(infoTable);

        // Notes
        if (prescription.getNote() != null && !prescription.getNote().isEmpty()) {
            doc.add(new Paragraph("Notes: " + prescription.getNote(), FONT_SUBTITLE));
            doc.add(Chunk.NEWLINE);
        }

        // Medicine table
        doc.add(new Paragraph("Prescribed Medicines", FONT_LABEL));
        doc.add(Chunk.NEWLINE);

        PdfPTable medTable = new PdfPTable(new float[]{3, 4, 1});
        medTable.setWidthPercentage(100);
        addTableHeader(medTable, "Medicine", "Dosage", "Qty");

        boolean alt = false;
        for (PrescriptionItem item : prescription.getItems()) {
            BaseColor bg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
            addTableRow(medTable, bg,
                    item.getMedicineName(),
                    item.getDosage(),
                    String.valueOf(item.getQuantity())
            );
            alt = !alt;
        }
        doc.add(medTable);

        addFooter(doc);
        doc.close();
    }

    // ── APPOINTMENT REPORT ───────────────────────────────────
    public static void generateAppointmentReport(String outputPath) throws Exception {
        List<Appointment> appointments = new AppointmentDAO().getAllAppointments();

        Document doc = new Document(PageSize.A4.rotate()); // landscape for wide table
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        addHeader(doc, "Appointment Report",
                "Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        PdfPTable table = new PdfPTable(new float[]{1, 3, 3, 2, 2, 2});
        table.setWidthPercentage(100);
        addTableHeader(table, "ID", "Patient", "Doctor", "Date", "Time", "Status");

        boolean alt = false;
        for (Appointment a : appointments) {
            BaseColor bg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
            addTableRow(table, bg,
                    String.valueOf(a.getAppointmentId()),
                    a.getPatientName(),
                    "Dr. " + a.getDoctorName(),
                    a.getDate().toString(),
                    a.getTime().toString(),
                    a.getStatus()
            );
            alt = !alt;
        }
        doc.add(table);

        addSummaryLine(doc, "Total appointments: " + appointments.size());
        addFooter(doc);
        doc.close();
    }

    // ── DOCTOR REPORT ────────────────────────────────────────
    public static void generateDoctorReport(String outputPath) throws Exception {
        List<Doctor> doctors = new DoctorDAO().getAllDoctors();

        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        addHeader(doc, "Doctor Report",
                "Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        PdfPTable table = new PdfPTable(new float[]{1, 3, 3, 2, 2});
        table.setWidthPercentage(100);
        addTableHeader(table, "ID", "Name", "Specialization", "Phone", "Available");

        boolean alt = false;
        for (Doctor d : doctors) {
            BaseColor bg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
            addTableRow(table, bg,
                    String.valueOf(d.getDoctorId()),
                    "Dr. " + d.getName(),
                    d.getSpecialization(),
                    d.getPhone(),
                    d.isAvailable() ? "Yes" : "No"
            );
            alt = !alt;
        }
        doc.add(table);

        addSummaryLine(doc, "Total doctors: " + doctors.size());
        addFooter(doc);
        doc.close();
    }

    // ── BILLING REPORT ───────────────────────────────────────
    public static void generateBillingReport(String outputPath) throws Exception {
        List<Billing> bills = new BillingDAO().getAllBills();

        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        addHeader(doc, "Billing Report",
                "Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        PdfPTable table = new PdfPTable(new float[]{1, 1, 3, 2, 2});
        table.setWidthPercentage(100);
        addTableHeader(table, "Bill ID", "Appt ID", "Patient", "Amount (৳)", "Status");

        double total = 0;
        boolean alt = false;
        for (Billing b : bills) {
            BaseColor bg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
            addTableRow(table, bg,
                    String.valueOf(b.getBillId()),
                    String.valueOf(b.getAppointmentId()),
                    b.getPatientName(),
                    String.format("%.2f", b.getTotalAmount()),
                    b.getStatus()
            );
            total += b.getTotalAmount();
            alt = !alt;
        }
        doc.add(table);

        addSummaryLine(doc,
                "Total bills: " + bills.size() +
                        "    |    Total amount: ৳ " + String.format("%.2f", total)
        );
        addFooter(doc);
        doc.close();
    }

    // ── LAB REPORT ───────────────────────────────────────────
    public static void generateLabReport(String outputPath) throws Exception {
        List<LabTest> tests = new LabTestDAO().getAllLabTests();

        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        addHeader(doc, "Lab Test Report",
                "Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        PdfPTable table = new PdfPTable(new float[]{1, 1, 3, 3, 3, 2});
        table.setWidthPercentage(100);
        addTableHeader(table, "ID", "Appt ID", "Patient", "Test", "Result", "Status");

        boolean alt = false;
        for (LabTest t : tests) {
            BaseColor bg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
            addTableRow(table, bg,
                    String.valueOf(t.getLabId()),
                    String.valueOf(t.getAppointmentId()),
                    t.getPatientName(),
                    t.getTestName(),
                    t.getResult() != null ? t.getResult() : "—",
                    t.getStatus()
            );
            alt = !alt;
        }
        doc.add(table);

        addSummaryLine(doc, "Total tests: " + tests.size());
        addFooter(doc);
        doc.close();
    }

    // ── SHARED HELPERS ───────────────────────────────────────

    private static void addHeader(Document doc, String title, String subtitle)
            throws DocumentException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(16);

        PdfPCell titleCell = new PdfPCell(new Phrase(title, FONT_TITLE));
        titleCell.setBackgroundColor(COLOR_PRIMARY);
        titleCell.setPadding(14);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(titleCell);

        PdfPCell subCell = new PdfPCell(new Phrase(subtitle, FONT_SUBTITLE));
        subCell.setPadding(6);
        subCell.setBorder(Rectangle.NO_BORDER);
        subCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(subCell);

        doc.add(header);
    }

    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_HEADER));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setPadding(6);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
        }
    }

    private static void addTableRow(PdfPTable table, BaseColor bg, String... values) {
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v, FONT_CELL));
            cell.setBackgroundColor(bg);
            cell.setPadding(5);
            cell.setBorderColor(new BaseColor(220, 220, 220));
            table.addCell(cell);
        }
    }

    private static void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_LABEL));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(6);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_CELL));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(6);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private static void addSummaryLine(Document doc, String text)
            throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph summary = new Paragraph(text, FONT_LABEL);
        summary.setAlignment(Element.ALIGN_RIGHT);
        doc.add(summary);
    }

    private static void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
                "Hospital Management System  —  Generated automatically",
                new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }
}