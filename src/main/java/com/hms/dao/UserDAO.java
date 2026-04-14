package com.hms.dao;

import com.hms.model.User;
import com.hms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        // We fetch by username only — password check happens in Java, not SQL
        String sql = """
        SELECT u.user_id, u.username, u.name, u.password_hash, r.role_name
        FROM user u
        JOIN role r ON u.role_id = r.role_id
        WHERE u.username = ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("role_name"),
                        rs.getString("password_hash")  // needed for BCrypt check
                );
            }
        }
        return null;
    }
}