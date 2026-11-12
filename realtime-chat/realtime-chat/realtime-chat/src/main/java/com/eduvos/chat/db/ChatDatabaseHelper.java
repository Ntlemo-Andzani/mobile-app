package com.eduvos.chat.db;

import com.eduvos.chat.model.Message;
import com.eduvos.chat.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Central database access object (DAO) for the chat application.
 *
 * Responsibilities:
 *   - owns the JDBC connection details and creates the schema on first use,
 *   - stores and authenticates users (passwords are salted + SHA-256 hashed),
 *   - stores chat messages and reads back the recent history.
 *
 * Backed by SQLite (a single file, no server to install), accessed with the
 * org.xerial:sqlite-jdbc driver. All SQL uses PreparedStatements, so user input
 * can never be interpreted as SQL (no injection).
 *
 * Implemented as a thread-safe singleton so the whole application shares one
 * helper: use {@link #getInstance()}.
 *
 * To use a different database (e.g. MySQL) only {@link #DB_URL} and the driver
 * dependency need to change - the SQL is standard.
 */
public class ChatDatabaseHelper {

    /** SQLite database file, created in the user's home directory if absent. */
    private static final String DB_URL =
            "jdbc:sqlite:" + System.getProperty("user.home") + "/chatapp.db";

    private static final ChatDatabaseHelper INSTANCE = new ChatDatabaseHelper();

    private final SecureRandom random = new SecureRandom();

    /** Private constructor: load the driver and build the schema once. */
    private ChatDatabaseHelper() {
        try {
            // Explicit load for older containers; JDBC 4 would auto-register it.
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found on classpath", e);
        }
        createTables();
    }

    public static ChatDatabaseHelper getInstance() {
        return INSTANCE;
    }

    /** Opens a new JDBC connection to the database. */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ---- schema ----------------------------------------------------------

    /** Creates the {@code users} and {@code messages} tables if they do not exist. */
    private void createTables() {
        String usersSql =
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  username      TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                "  password_hash TEXT NOT NULL, " +
                "  salt          TEXT NOT NULL, " +
                "  created_at    INTEGER NOT NULL" +
                ")";

        String messagesSql =
                "CREATE TABLE IF NOT EXISTS messages (" +
                "  id         INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  sender     TEXT NOT NULL, " +
                "  type       TEXT NOT NULL, " +
                "  content    TEXT NOT NULL, " +
                "  created_at INTEGER NOT NULL" +
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(usersSql);
            stmt.execute(messagesSql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create database schema", e);
        }
    }

    // ---- user operations -------------------------------------------------

    /** @return true if an account with this username already exists. */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ? COLLATE NOCASE";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registers a new user. The password is salted and hashed before storage.
     *
     * @return true on success, false if the username is taken or input is blank.
     */
    public boolean registerUser(String username, String password) {
        if (username == null || password == null
                || username.trim().isEmpty() || password.isEmpty()) {
            return false;
        }
        username = username.trim();
        if (usernameExists(username)) {
            return false;
        }

        String salt = newSalt();
        String hash = hash(password, salt);

        String sql = "INSERT INTO users (username, password_hash, salt, created_at) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setLong(4, System.currentTimeMillis());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Verifies a username/password pair against the stored salted hash. */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        User user = getUser(username);
        if (user == null) {
            return false;
        }
        String attempt = hash(password, user.getSalt());
        return constantTimeEquals(attempt, user.getPasswordHash());
    }

    /** @return the user with this username, or null if none exists. */
    public User getUser(String username) {
        String sql = "SELECT id, username, password_hash, salt, created_at "
                   + "FROM users WHERE username = ? COLLATE NOCASE";
        try (Connection conn = getConnection();
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

    // ---- message operations ----------------------------------------------

    /**
     * Saves a chat message. On success the generated id is written back onto the
     * supplied object.
     *
     * @return true if the row was inserted.
     */
    public boolean saveMessage(Message message) {
        if (message == null || message.getContent() == null) {
            return false;
        }
        String sql = "INSERT INTO messages (sender, type, content, created_at) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, message.getSender());
            ps.setString(2, message.getType());
            ps.setString(3, message.getContent());
            ps.setLong(4, message.getCreatedAt());

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
     * Reads the most recent messages, returned oldest-first so they can be shown
     * in chat order.
     *
     * @param limit maximum number of messages to return.
     */
    public List<Message> getRecentMessages(int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender, type, content, created_at "
                   + "FROM messages ORDER BY id DESC LIMIT ?";
        try (Connection conn = getConnection();
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

    // ---- password hashing helpers ----------------------------------------

    private String newSalt() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /** Timing-safe comparison of two hashes. */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
