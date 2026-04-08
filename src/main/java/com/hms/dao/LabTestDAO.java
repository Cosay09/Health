// LabTestDAO.java
package com.hms.dao;

import com.hms.model.LabTest;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LabTestDAO {

    public boolean addLabTest(LabTest t) throws SQLException {
        String sql = "INSERT INTO lab_test (appointment_id, patient_id, test_name, result, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, t.getAppointmentId());
            stmt.setInt(2, t.getPatientId());
            stmt.setString(3, t.getTestName());
            stmt.setString(4, t.getResult());
            stmt.setString(5, t.getStatus());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<LabTest> getAllLabTests() throws SQLException {
        List<LabTest> list = new ArrayList<>();
        String sql = """
            SELECT l.*, p.name AS patient_name
            FROM lab_test l
            JOIN patient p ON l.patient_id = p.patient_id
            ORDER BY l.ordered_at DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<LabTest> search(String keyword) throws SQLException {
        List<LabTest> list = new ArrayList<>();
        String sql = """
            SELECT l.*, p.name AS patient_name
            FROM lab_test l
            JOIN patient p ON l.patient_id = p.patient_id
            WHERE p.name LIKE ? OR l.test_name LIKE ?
            ORDER BY l.ordered_at DESC
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

    public boolean updateResult(int labId, String result) throws SQLException {
        String sql = "UPDATE lab_test SET result = ?, status = 'Completed' WHERE lab_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, result);
            stmt.setInt(2, labId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteLabTest(int labId) throws SQLException {
        String sql = "DELETE FROM lab_test WHERE lab_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, labId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int countPending() throws SQLException {
        String sql = "SELECT COUNT(*) FROM lab_test WHERE status = 'Pending'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private LabTest mapRow(ResultSet rs) throws SQLException {
        return new LabTest(
                rs.getInt("lab_id"),
                rs.getInt("appointment_id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getString("test_name"),
                rs.getString("result"),
                rs.getString("status"),
                rs.getTimestamp("ordered_at").toLocalDateTime()
        );
    }

    public List<LabTest> getByPatientId(int patientId) throws SQLException {
        List<LabTest> list = new ArrayList<>();
        String sql = """
        SELECT l.*, p.name AS patient_name
        FROM lab_test l
        JOIN patient p ON l.patient_id = p.patient_id
        WHERE l.patient_id = ?
        ORDER BY l.ordered_at DESC
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