package com.hms.dao;

import com.hms.model.Billing;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillingDAO {

    // ── CREATE ───────────────────────────────────────────────
    public boolean addBill(Billing b) throws SQLException {
        String sql = "INSERT INTO billing (patient_id, appointment_id, total_amount, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, b.getPatientId());
            stmt.setInt(2, b.getAppointmentId());
            stmt.setDouble(3, b.getTotalAmount());
            stmt.setString(4, b.getStatus());
            return stmt.executeUpdate() > 0;
        }
    }

    // ── READ ALL ─────────────────────────────────────────────
    public List<Billing> getAllBills() throws SQLException {
        List<Billing> list = new ArrayList<>();
        String sql = """
            SELECT b.*, p.name AS patient_name
            FROM billing b
            JOIN patient p ON b.patient_id = p.patient_id
            ORDER BY b.created_at DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── READ — search by patient name ────────────────────────
    public List<Billing> search(String keyword) throws SQLException {
        List<Billing> list = new ArrayList<>();
        String sql = """
            SELECT b.*, p.name AS patient_name
            FROM billing b
            JOIN patient p ON b.patient_id = p.patient_id
            WHERE p.name LIKE ?
            ORDER BY b.created_at DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── UPDATE STATUS ────────────────────────────────────────
    public boolean updateStatus(int billId, String status) throws SQLException {
        String sql = "UPDATE billing SET status = ? WHERE bill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, billId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── UPDATE FULL ──────────────────────────────────────────
    public boolean updateBill(Billing b) throws SQLException {
        String sql = "UPDATE billing SET total_amount=?, status=? WHERE bill_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, b.getTotalAmount());
            stmt.setString(2, b.getStatus());
            stmt.setInt(3, b.getBillId());
            return stmt.executeUpdate() > 0;
        }
    }

    // ── DELETE ───────────────────────────────────────────────
    public boolean deleteBill(int billId) throws SQLException {
        String sql = "DELETE FROM billing WHERE bill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── HELPER ───────────────────────────────────────────────
    private Billing mapRow(ResultSet rs) throws SQLException {
        return new Billing(
                rs.getInt("bill_id"),
                rs.getInt("patient_id"),
                rs.getInt("appointment_id"),
                rs.getString("patient_name"),
                rs.getDouble("total_amount"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    public List<Billing> getByPatientId(int patientId) throws SQLException {
        List<Billing> list = new ArrayList<>();
        String sql = """
        SELECT b.*, p.name AS patient_name
        FROM billing b
        JOIN patient p ON b.patient_id = p.patient_id
        WHERE b.patient_id = ?
        ORDER BY b.created_at DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }
}