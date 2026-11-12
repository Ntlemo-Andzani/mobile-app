package com.eduvos.chat.db;

import com.eduvos.chat.model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for the {@code messages} table.
 *
 * Persists chat messages and reads back the recent history. Like {@link UserDao}
 * it holds only persistence logic and borrows connections from
 * {@link ChatDatabaseHelper}.
 */
public class MessageDao {

    private final ChatDatabaseHelper db = ChatDatabaseHelper.getInstance();

    /**
     * Inserts a message row. On success the generated id is written back onto
     * the supplied object.
     *
     * @return true if the row was inserted.
     */
    public boolean insert(Message message) {
        String sql = "INSERT INTO messages (sender, type, content, created_at) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, message.getSender());
            ps.setString(2, message.getType());
            ps.setString(3, message.getContent());
            ps.setLong(4, message.getCreatedAt() == 0
                    ? System.currentTimeMillis() : message.getCreatedAt());

            if (ps.executeUpdate() == 1) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        message.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns the most recent messages, oldest-first so they display in order.
     *
     * @param limit maximum number of messages to return.
     */
    public List<Message> findRecent(int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender, type, content, created_at "
                   + "FROM messages ORDER BY id DESC LIMIT ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Message(
                            rs.getInt("id"),
                            rs.getString("sender"),
                            rs.getString("type"),
                            rs.getString("content"),
                            rs.getLong("created_at")));
                }
            }
            Collections.reverse(messages); // newest-first -> oldest-first
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}
