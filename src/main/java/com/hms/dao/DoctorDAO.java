package com.hms.dao;

import com.hms.model.Doctor;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO
{

    // ── CREATE ───────────────────────────────────────────────
    public boolean addDoctor(Doctor d) throws SQLException
    {
        String sql = "INSERT INTO doctor (name, specialization, phone, available) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, d.getName());
            stmt.setString(2, d.getSpecialization());
            stmt.setString(3, d.getPhone());
            stmt.setBoolean(4, d.isAvailable());
            return stmt.executeUpdate() > 0;
        }
    }

    // ── READ ALL ─────────────────────────────────────────────
    public List<Doctor> getAllDoctors() throws SQLException
    {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctor ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── READ — search by name or specialization ───────────────
    public List<Doctor> search(String keyword) throws SQLException
    {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctor WHERE name LIKE ? OR specialization LIKE ? ORDER BY name";
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

    // ── READ — only available doctors (used by Appointment module later) ──
    public List<Doctor> getAvailableDoctors() throws SQLException
    {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctor WHERE available = TRUE ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── UPDATE ───────────────────────────────────────────────
    public boolean updateDoctor(Doctor d) throws SQLException
    {
        String sql = "UPDATE doctor SET name=?, specialization=?, phone=?, available=? WHERE doctor_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, d.getName());
            stmt.setString(2, d.getSpecialization());
            stmt.setString(3, d.getPhone());
            stmt.setBoolean(4, d.isAvailable());
            stmt.setInt(5, d.getDoctorId());
            return stmt.executeUpdate() > 0;
        }
    }

    // ── DELETE ───────────────────────────────────────────────
    public boolean deleteDoctor(int doctorId) throws SQLException
    {
        String sql = "DELETE FROM doctor WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── HELPER ───────────────────────────────────────────────
    private Doctor mapRow(ResultSet rs) throws SQLException
    {
        return new Doctor(
                rs.getInt("doctor_id"),
                rs.getString("name"),
                rs.getString("specialization"),
                rs.getString("phone"),
                rs.getBoolean("available")
        );
    }

    // Gets the doctor_id for the currently logged-in doctor user
    public Doctor getDoctorByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM doctor WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }
}