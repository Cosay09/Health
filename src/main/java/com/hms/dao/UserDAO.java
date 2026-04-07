package com.hms.dao;

import com.hms.model.User;
import com.hms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User findByUsernameAndPassword(String username, String password) throws SQLException {

        // In a real app you'd hash the password and compare hashes.
        // For now we compare directly — we'll upgrade this in Phase 5.
        String sql = """
            SELECT u.user_id, u.username, u.name, r.role_name
            FROM user u
            JOIN role r ON u.role_id = r.role_id
            WHERE u.username = ? AND u.password_hash = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("role_name")
                );
            }
        }
        return null; // no match found
    }
}