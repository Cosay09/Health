package com.hms.util;

import com.hms.dao.AppointmentDAO;
import com.hms.dao.BillingDAO;
import com.hms.dao.DoctorDAO;
import com.hms.dao.LabTestDAO;
import com.hms.model.PharmacySale;
import com.hms.model.PharmacySaleItem;
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
        Paragraph footer = new Paragraph();
        footer.setAlignment(Element.ALIGN_CENTER);

        footer.add(new Chunk(
                "HealthPlus+\n",
                new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(40, 40, 40))
        ));

        footer.add(new Chunk(
                "Where Care Meets Excellence",
                new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY)
        ));

        doc.add(Chunk.NEWLINE);
        doc.add(footer);
    }

    // ── PATIENT INVOICE ──────────────────────────────────────
    public static void generateInvoice(Patient patient, List<Billing> bills,
                                       String outputPath) throws Exception {
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        addHeader(doc, "Patient Invoice", "Invoice for: " + patient.getName());

        // Patient info block
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setSpacingAfter(16);
        addInfoRow(info, "Patient",       patient.getName());
        addInfoRow(info, "Age",           String.valueOf(patient.getAge()));
        addInfoRow(info, "Gender",        patient.getGender());
        addInfoRow(info, "Phone",         patient.getPhone() != null ? patient.getPhone() : "—");
        addInfoRow(info, "Invoice Date",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        doc.add(info);

        // Bills table
        PdfPTable table = new PdfPTable(new float[]{1, 1, 3, 2});
        table.setWidthPercentage(100);
        addTableHeader(table, "Bill ID", "Appt ID", "Status", "Amount (৳)");

        double totalDue  = 0;
        double totalPaid = 0;
        boolean alt = false;

        for (Billing b : bills) {
            BaseColor bg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
            addTableRow(table, bg,
                    String.valueOf(b.getBillId()),
                    String.valueOf(b.getAppointmentId()),
                    b.getStatus(),
                    String.format("%.2f", b.getTotalAmount())
            );
            totalDue += b.getTotalAmount();
            if (b.getStatus().equals("Paid")) totalPaid += b.getTotalAmount();
            alt = !alt;
        }
        doc.add(table);

        // Summary
        doc.add(Chunk.NEWLINE);
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(50);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
        addInfoRow(summary, "Total Billed:",  String.format("৳ %.2f", totalDue));
        addInfoRow(summary, "Total Paid:",    String.format("৳ %.2f", totalPaid));
        addInfoRow(summary, "Balance Due:",   String.format("৳ %.2f", totalDue - totalPaid));
        doc.add(summary);

        addFooter(doc);
        doc.close();
    }

    // ── FINANCIAL REPORT (admin) ─────────────────────────────
    public static void generateFinancialReport(String outputPath) throws Exception {
        List<Billing> bills = new BillingDAO().getAllBills();

        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        addHeader(doc, "Financial Report",
                "Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        // Revenue summary block
        double totalBilled  = bills.stream().mapToDouble(Billing::getTotalAmount).sum();
        double totalPaid    = bills.stream()
                .filter(b -> b.getStatus().equals("Paid"))
                .mapToDouble(Billing::getTotalAmount).sum();
        double totalPending = bills.stream()
                .filter(b -> b.getStatus().equals("Unpaid"))
                .mapToDouble(Billing::getTotalAmount).sum();
        double totalPartial = bills.stream()
                .filter(b -> b.getStatus().equals("Partial"))
                .mapToDouble(Billing::getTotalAmount).sum();

        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(60);
        summary.setSpacingAfter(20);
        addInfoRow(summary, "Total Revenue Billed:",  String.format("৳ %.2f", totalBilled));
        addInfoRow(summary, "Collected (Paid):",      String.format("৳ %.2f", totalPaid));
        addInfoRow(summary, "Partial Payments:",      String.format("৳ %.2f", totalPartial));
        addInfoRow(summary, "Outstanding (Unpaid):",  String.format("৳ %.2f", totalPending));
        addInfoRow(summary, "Total Bills:",           String.valueOf(bills.size()));
        doc.add(summary);

        // Monthly breakdown
        doc.add(new Paragraph("Monthly Breakdown", FONT_LABEL));
        doc.add(Chunk.NEWLINE);

        // Group bills by year-month
        java.util.Map<String, Double> monthly = new java.util.TreeMap<>();
        for (Billing b : bills) {
            if (b.getStatus().equals("Paid")) {
                String month = b.getCreatedAt().toLocalDate()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthly.merge(month, b.getTotalAmount(), Double::sum);
            }
        }

        PdfPTable monthTable = new PdfPTable(new float[]{2, 3});
        monthTable.setWidthPercentage(60);
        addTableHeader(monthTable, "Month", "Revenue Collected (৳)");

        boolean alt = false;
        for (java.util.Map.Entry<String, Double> entry : monthly.entrySet()) {
            BaseColor bg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
            addTableRow(monthTable, bg,
                    entry.getKey(),
                    String.format("%.2f", entry.getValue())
            );
            alt = !alt;
        }
        doc.add(monthTable);

        addFooter(doc);
        doc.close();
    }

    public static void generatePharmacyReceipt(PharmacySale sale, Patient patient,
                                               Prescription prescription,
                                               String outputPath) throws Exception {
        Document doc = new Document(PageSize.A5);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        addHeader(doc, "Pharmacy Receipt", "Sale ID: " + sale.getSaleId());

        // Patient info
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setSpacingAfter(12);
        addInfoRow(info, "Patient",    patient.getName());
        addInfoRow(info, "Age",        String.valueOf(patient.getAge()));
        addInfoRow(info, "Date",
                sale.getSaleDate() != null
                        ? sale.getSaleDate().toLocalDate().toString()
                        : LocalDate.now().toString());
        if (prescription != null) {
            addInfoRow(info, "Doctor",      "Dr. " + prescription.getDoctorName());
            addInfoRow(info, "Prescription","# " + prescription.getPrescriptionId());
        }
        doc.add(info);

        // Medicine items table
        doc.add(new Paragraph("Items Dispensed", FONT_LABEL));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(new float[]{3, 1, 2, 2});
        table.setWidthPercentage(100);
        addTableHeader(table, "Medicine", "Qty", "Unit Price", "Subtotal");

        boolean alt = false;
        for (PharmacySaleItem item : sale.getItems()) {
            BaseColor bg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
            addTableRow(table, bg,
                    item.getMedicineName(),
                    String.valueOf(item.getQuantity()),
                    String.format("৳ %.2f", item.getUnitPrice()),
                    String.format("৳ %.2f", item.getSubtotal())
            );
            alt = !alt;
        }
        doc.add(table);

        // Total
        doc.add(Chunk.NEWLINE);
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(50);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        addInfoRow(totalTable, "Total Paid:",
                String.format("৳ %.2f", sale.getTotalAmount()));
        doc.add(totalTable);

        addFooter(doc);
        doc.close();
    }
}