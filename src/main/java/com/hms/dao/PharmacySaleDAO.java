package com.hms.dao;

import com.hms.model.PharmacySale;
import com.hms.model.PharmacySaleItem;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PharmacySaleDAO {

    // ── CREATE — saves sale header + items in one transaction ─
    public int addSale(PharmacySale sale) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);

        try
        {
            // 1. Insert sale header
            String headerSql = """
                INSERT INTO pharmacy_sale
                    (patient_id, appointment_id, prescription_id, sold_by, total_amount)
                VALUES (?, ?, ?, ?, ?)
                """;
            int saleId;
            try (PreparedStatement stmt = conn.prepareStatement(
                    headerSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, sale.getPatientId());

                // appointment_id and prescription_id are nullable
                if (sale.getAppointmentId() > 0)
                    stmt.setInt(2, sale.getAppointmentId());
                else
                    stmt.setNull(2, Types.INTEGER);

                if (sale.getPrescriptionId() > 0)
                    stmt.setInt(3, sale.getPrescriptionId());
                else
                    stmt.setNull(3, Types.INTEGER);

                stmt.setInt(4, sale.getSoldBy());
                stmt.setDouble(5, sale.getTotalAmount());
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                keys.next();
                saleId = keys.getInt(1);
            }

            // 2. Insert line items
            String itemSql = """
                INSERT INTO pharmacy_sale_item
                    (sale_id, medicine_id, quantity, unit_price)
                VALUES (?, ?, ?, ?)
                """;
            try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                for (PharmacySaleItem item : sale.getItems()) {
                    stmt.setInt(1, saleId);
                    stmt.setInt(2, item.getMedicineId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setDouble(4, item.getUnitPrice());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // 3. Reduce stock for each item
            MedicineDAO medicineDAO = new MedicineDAO();
            for (PharmacySaleItem item : sale.getItems()) {
                boolean reduced = medicineDAO.reduceStock(
                        conn, item.getMedicineId(), item.getQuantity());
                if (!reduced) {
                    conn.rollback();
                    throw new SQLException("Insufficient stock for: "
                            + item.getMedicineName());
                }
            }

            conn.commit();
            return saleId;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ── READ ALL ─────────────────────────────────────────────
    public List<PharmacySale> getAllSales() throws SQLException {
        List<PharmacySale> list = new ArrayList<>();
        String sql = """
            SELECT ps.*, p.name AS patient_name
            FROM pharmacy_sale ps
            LEFT JOIN patient p ON ps.patient_id = p.patient_id
            ORDER BY ps.sale_date DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── SEARCH by patient name ────────────────────────────────
    public List<PharmacySale> searchByPatientName(String keyword) throws SQLException {
        List<PharmacySale> list = new ArrayList<>();
        String sql = """
            SELECT ps.*, p.name AS patient_name
            FROM pharmacy_sale ps
            LEFT JOIN patient p ON ps.patient_id = p.patient_id
            WHERE p.name LIKE ?
            ORDER BY ps.sale_date DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── GET items for a specific sale ─────────────────────────
    public List<PharmacySaleItem> getItemsBySaleId(int saleId) throws SQLException {
        List<PharmacySaleItem> list = new ArrayList<>();
        String sql = """
            SELECT psi.*, m.name AS medicine_name
            FROM pharmacy_sale_item psi
            JOIN medicine m ON psi.medicine_id = m.medicine_id
            WHERE psi.sale_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new PharmacySaleItem(
                        rs.getInt("item_id"),
                        rs.getInt("sale_id"),
                        rs.getInt("medicine_id"),
                        rs.getString("medicine_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price")
                ));
            }
        }
        return list;
    }

    // ── HELPER ───────────────────────────────────────────────
    private PharmacySale mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("sale_date");
        return new PharmacySale(
                rs.getInt("sale_id"),
                rs.getInt("patient_id"),
                rs.getInt("appointment_id"),
                rs.getInt("prescription_id"),
                rs.getInt("sold_by"),
                rs.getDouble("total_amount"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("patient_name")
        );
    }
}