package com.hms.dao;

import com.hms.model.Prescription;
import com.hms.model.PrescriptionItem;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    // ── CREATE — saves header + all items in one transaction ─
    public boolean addPrescription(Prescription p) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false); // start transaction

        try {
            // 1. Insert prescription header, get generated ID back
            String headerSql = "INSERT INTO prescription (appointment_id, doctor_id, prescription_date, note) VALUES (?, ?, ?, ?)";
            int prescriptionId;

            try (PreparedStatement stmt = conn.prepareStatement(
                    headerSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, p.getAppointmentId());
                stmt.setInt(2, p.getDoctorId());
                stmt.setDate(3, Date.valueOf(p.getDate()));
                stmt.setString(4, p.getNote());
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                keys.next();
                prescriptionId = keys.getInt(1);
            }

            // 2. Insert each medicine line item
            String itemSql = "INSERT INTO prescription_medicine (prescription_id, medicine_id, dosage, quantity) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                for (PrescriptionItem item : p.getItems()) {
                    stmt.setInt(1, prescriptionId);
                    stmt.setInt(2, item.getMedicineId());
                    stmt.setString(3, item.getDosage());
                    stmt.setInt(4, item.getQuantity());
                    stmt.addBatch(); // batch insert — more efficient than one-by-one
                }
                stmt.executeBatch();
            }

            conn.commit(); // everything succeeded — commit
            return true;

        } catch (SQLException e) {
            conn.rollback(); // something failed — undo everything
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ── READ ALL ─────────────────────────────────────────────
    public List<Prescription> getAllPrescriptions() throws SQLException {
        List<Prescription> list = new ArrayList<>();
        String sql = """
            SELECT pr.*, d.name AS doctor_name, p.name AS patient_name
            FROM prescription pr
            JOIN doctor d      ON pr.doctor_id      = d.doctor_id
            JOIN appointment a ON pr.appointment_id = a.appointment_id
            JOIN patient p     ON a.patient_id      = p.patient_id
            ORDER BY pr.prescription_date DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── READ items for one prescription ──────────────────────
    public List<PrescriptionItem> getItems(int prescriptionId) throws SQLException {
        List<PrescriptionItem> items = new ArrayList<>();
        String sql = """
            SELECT pm.*, m.name AS medicine_name
            FROM prescription_medicine pm
            JOIN medicine m ON pm.medicine_id = m.medicine_id
            WHERE pm.prescription_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prescriptionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(new PrescriptionItem(
                        rs.getInt("medicine_id"),
                        rs.getString("medicine_name"),
                        rs.getString("dosage"),
                        rs.getInt("quantity")
                ));
            }
        }
        return items;
    }

    // ── SEARCH ───────────────────────────────────────────────
    public List<Prescription> search(String keyword) throws SQLException {
        List<Prescription> list = new ArrayList<>();
        String sql = """
            SELECT pr.*, d.name AS doctor_name, p.name AS patient_name
            FROM prescription pr
            JOIN doctor d      ON pr.doctor_id      = d.doctor_id
            JOIN appointment a ON pr.appointment_id = a.appointment_id
            JOIN patient p     ON a.patient_id      = p.patient_id
            WHERE p.name LIKE ? OR d.name LIKE ?
            ORDER BY pr.prescription_date DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            stmt.setString(1, kw);
            stmt.setString(2, kw);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── DELETE ───────────────────────────────────────────────
    public boolean deletePrescription(int prescriptionId) throws SQLException {
        // Junction table rows delete automatically via ON DELETE CASCADE
        // Make sure your FK has ON DELETE CASCADE — or delete items first
        String sql = "DELETE FROM prescription WHERE prescription_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prescriptionId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Prescription> getByPatientId(int patientId) throws SQLException {
        List<Prescription> list = new ArrayList<>();
        String sql = """
        SELECT pr.*, d.name AS doctor_name, p.name AS patient_name
        FROM prescription pr
        JOIN doctor d      ON pr.doctor_id      = d.doctor_id
        JOIN appointment a ON pr.appointment_id = a.appointment_id
        JOIN patient p     ON a.patient_id      = p.patient_id
        WHERE a.patient_id = ?
        ORDER BY pr.prescription_date DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Prescription mapRow(ResultSet rs) throws SQLException {
        return new Prescription(
                rs.getInt("prescription_id"),
                rs.getInt("appointment_id"),
                rs.getInt("doctor_id"),
                rs.getString("doctor_name"),
                rs.getString("patient_name"),
                rs.getDate("prescription_date").toLocalDate(),
                rs.getString("note")
        );
    }
}