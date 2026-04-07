package com.hms.dao;

import com.hms.model.Patient;
import com.hms.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    // ── CREATE ──────────────────────────────────────────────
    public boolean addPatient(Patient p) throws SQLException {
        String sql = "INSERT INTO patient (name, date_of_birth, gender, phone, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getName());
            stmt.setDate(2, Date.valueOf(p.getDateOfBirth()));
            stmt.setString(3, p.getGender());
            stmt.setString(4, p.getPhone());
            stmt.setString(5, p.getAddress());
            return stmt.executeUpdate() > 0;
        }
    }

    // ── READ ALL ─────────────────────────────────────────────
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patient ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── READ — search by name ────────────────────────────────
    public List<Patient> searchByName(String keyword) throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patient WHERE name LIKE ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── UPDATE ───────────────────────────────────────────────
    public boolean updatePatient(Patient p) throws SQLException {
        String sql = "UPDATE patient SET name=?, date_of_birth=?, gender=?, phone=?, address=? WHERE patient_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getName());
            stmt.setDate(2, Date.valueOf(p.getDateOfBirth()));
            stmt.setString(3, p.getGender());
            stmt.setString(4, p.getPhone());
            stmt.setString(5, p.getAddress());
            stmt.setInt(6, p.getPatientId());
            return stmt.executeUpdate() > 0;
        }
    }

    // ── DELETE ───────────────────────────────────────────────
    public boolean deletePatient(int patientId) throws SQLException {
        String sql = "DELETE FROM patient WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── COUNT ────────────────────────────────────────────────
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM patient";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── HELPER — maps one ResultSet row to a Patient object ──
    private Patient mapRow(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("patient_id"),
                rs.getString("name"),
                rs.getDate("date_of_birth").toLocalDate(),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("address")
        );
    }
}