package com.eduvos.chat.db;

import com.eduvos.chat.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for the {@code users} table.
 *
 * A DAO contains only persistence logic - no business rules. It gets its
 * connections from {@link ChatDatabaseHelper} (which owns the connection details
 * and the schema) and uses PreparedStatements throughout.
 */
public class UserDao {

    private final ChatDatabaseHelper db = ChatDatabaseHelper.getInstance();

    /**
     * Inserts a user row. On success the generated id is written back onto the
     * supplied object.
     *
     * @return true if the row was inserted.
     */
    public boolean insert(User user) {
        String sql = "INSERT INTO users (username, password_hash, salt, created_at) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setLong(4, user.getCreatedAt() == 0
                    ? System.currentTimeMillis() : user.getCreatedAt());

            if (ps.executeUpdate() == 1) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** @return the user with this username (case-insensitive), or null. */
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, salt, created_at "
                   + "FROM users WHERE username = ? COLLATE NOCASE";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("salt"),
                            rs.getLong("created_at"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** @return true if an account with this username already exists. */
    public boolean exists(String username) {
        return findByUsername(username) != null;
    }
}
