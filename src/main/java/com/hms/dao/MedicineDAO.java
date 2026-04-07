package com.hms.dao;

import com.hms.model.Medicine;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    public boolean addMedicine(Medicine m) throws SQLException {
        String sql = "INSERT INTO medicine (name, stock, unit, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getName());
            stmt.setInt(2, m.getStock());
            stmt.setString(3, m.getUnit());
            stmt.setDouble(4, m.getPrice());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Medicine> getAllMedicines() throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicine ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Medicine> search(String keyword) throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicine WHERE name LIKE ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean updateMedicine(Medicine m) throws SQLException {
        String sql = "UPDATE medicine SET name=?, stock=?, unit=?, price=? WHERE medicine_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getName());
            stmt.setInt(2, m.getStock());
            stmt.setString(3, m.getUnit());
            stmt.setDouble(4, m.getPrice());
            stmt.setInt(5, m.getMedicineId());
            return stmt.executeUpdate() > 0;
        }
    }

    // Called automatically when a prescription is dispensed
    public boolean reduceStock(int medicineId, int quantity) throws SQLException {
        String sql = "UPDATE medicine SET stock = stock - ? WHERE medicine_id = ? AND stock >= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, medicineId);
            stmt.setInt(3, quantity); // prevents negative stock
            return stmt.executeUpdate() > 0; // returns false if stock insufficient
        }
    }

    public boolean deleteMedicine(int medicineId) throws SQLException {
        String sql = "DELETE FROM medicine WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, medicineId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Returns medicines where stock is below threshold — used for low stock warning
    public List<Medicine> getLowStock(int threshold) throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicine WHERE stock <= ? ORDER BY stock ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, threshold);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        return new Medicine(
                rs.getInt("medicine_id"),
                rs.getString("name"),
                rs.getInt("stock"),
                rs.getString("unit"),
                rs.getDouble("price")
        );
    }
}