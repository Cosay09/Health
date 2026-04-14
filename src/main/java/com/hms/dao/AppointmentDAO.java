package com.hms.dao;

import com.hms.model.Appointment;
import com.hms.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO
{

    // ── CREATE ───────────────────────────────────────────────
    public boolean addAppointment(Appointment a) throws SQLException
    {
        String sql = "INSERT INTO appointment (patient_id, doctor_id, appointment_date, appointment_time, condition_note, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a.getPatientId());
            stmt.setInt(2, a.getDoctorId());
            stmt.setDate(3, Date.valueOf(a.getDate()));
            stmt.setTime(4, Time.valueOf(a.getTime()));
            stmt.setString(5, a.getConditionNote());
            stmt.setString(6, a.getStatus());
            return stmt.executeUpdate() > 0;
        }
    }

    // ── READ ALL — JOIN to get patient and doctor names ──────
    public List<Appointment> getAllAppointments() throws SQLException
    {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, p.name AS patient_name, d.name AS doctor_name
            FROM appointment a
            JOIN patient p ON a.patient_id = p.patient_id
            JOIN doctor  d ON a.doctor_id  = d.doctor_id
            ORDER BY a.appointment_date DESC, a.appointment_time DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── READ — search by patient or doctor name ──────────────
    public List<Appointment> search(String keyword) throws SQLException
    {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, p.name AS patient_name, d.name AS doctor_name
            FROM appointment a
            JOIN patient p ON a.patient_id = p.patient_id
            JOIN doctor  d ON a.doctor_id  = d.doctor_id
            WHERE p.name LIKE ? OR d.name LIKE ?
            ORDER BY a.appointment_date DESC
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

    public List<Appointment> getByPatientId(int patientId) throws SQLException
    {
        List<Appointment> list = new ArrayList<>();
        String sql = """
        SELECT a.*, p.name AS patient_name, d.name AS doctor_name
        FROM appointment a
        JOIN patient p ON a.patient_id = p.patient_id
        JOIN doctor  d ON a.doctor_id  = d.doctor_id
        WHERE a.patient_id = ?
        ORDER BY a.appointment_date DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── UPDATE STATUS ONLY ───────────────────────────────────
    // Used when marking an appointment Completed or Cancelled
    public boolean updateStatus(int appointmentId, String status) throws SQLException
    {
        String sql = "UPDATE appointment SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, appointmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── UPDATE FULL ──────────────────────────────────────────
    public boolean updateAppointment(Appointment a) throws SQLException
    {
        String sql = "UPDATE appointment SET patient_id=?, doctor_id=?, appointment_date=?, appointment_time=?, condition_note=?, status=? WHERE appointment_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a.getPatientId());
            stmt.setInt(2, a.getDoctorId());
            stmt.setDate(3, Date.valueOf(a.getDate()));
            stmt.setTime(4, Time.valueOf(a.getTime()));
            stmt.setString(5, a.getConditionNote());
            stmt.setString(6, a.getStatus());
            stmt.setInt(7, a.getAppointmentId());
            return stmt.executeUpdate() > 0;
        }
    }

    // ── DELETE ───────────────────────────────────────────────
    public boolean deleteAppointment(int appointmentId) throws SQLException
    {
        String sql = "DELETE FROM appointment WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── COUNT — used by HomeController ──────────────────────
    public int countToday() throws SQLException
    {
        String sql = "SELECT COUNT(*) FROM appointment WHERE appointment_date = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ── HELPER ───────────────────────────────────────────────
    private Appointment mapRow(ResultSet rs) throws SQLException
    {
        return new Appointment(
                rs.getInt("appointment_id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getString("patient_name"),
                rs.getString("doctor_name"),
                rs.getDate("appointment_date").toLocalDate(),
                rs.getTime("appointment_time").toLocalTime(),
                rs.getString("condition_note"),
                rs.getString("status")
        );
    }

    // All appointments belonging to a specific doctor
    public List<Appointment> getByDoctorId(int doctorId) throws SQLException
    {
        List<Appointment> list = new ArrayList<>();
        String sql = """
        SELECT a.*, p.name AS patient_name, d.name AS doctor_name
        FROM appointment a
        JOIN patient p ON a.patient_id = p.patient_id
        JOIN doctor  d ON a.doctor_id  = d.doctor_id
        WHERE a.doctor_id = ?
        ORDER BY a.appointment_date DESC, a.appointment_time DESC
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }
}