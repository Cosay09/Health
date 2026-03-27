package com.hms.dao;

import com.hms.model.Patient;
import com.hms.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    // Save a new patient to the database
    public boolean addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patient (name, date_of_birth, gender, phone, address) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getName());
            stmt.setDate(2, Date.valueOf(patient.getDateOfBirth()));
            stmt.setString(3, patient.getGender());
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());

            return stmt.executeUpdate() > 0;
        }
    }

    // Get all patients
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patient";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                patients.add(new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getDate("date_of_birth").toLocalDate(),
                        rs.getString("gender"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }
        }
        return patients;
    }

    // Find one patient by ID
    public Patient getPatientById(int id) throws SQLException {
        String sql = "SELECT * FROM patient WHERE patient_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
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
        return null;
    }
}