package com.eduvos.chat.store;

import com.eduvos.chat.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory user store (a thread-safe singleton).
 *
 * Accounts live only for the lifetime of the server process - they reset on
 * restart. That keeps the assessment self-contained (no database to set up).
 * For a production system you would swap this for a JDBC / JPA-backed store and
 * use a slow password hash such as bcrypt or PBKDF2 instead of a single
 * SHA-256 pass.
 */
public class UserStore {

    private static final UserStore INSTANCE = new UserStore();

    /** key = lowercase username (case-insensitive uniqueness), value = User */
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private UserStore() { }

    public static UserStore getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new account. Returns false if the username is already taken or
     * the input is blank.
     */
    public synchronized boolean register(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        username = username.trim();
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }
        String key = username.toLowerCase();
        if (users.containsKey(key)) {
            return false; // already registered
        }
        String salt = newSalt();
        String hash = hash(password, salt);
        users.put(key, new User(username, hash, salt));
        return true;
    }

    /** Verifies a username/password pair against the stored salted hash. */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        User user = users.get(username.trim().toLowerCase());
        if (user == null) {
            return false;
        }
        String attempt = hash(password, user.getSalt());
        return constantTimeEquals(attempt, user.getPasswordHash());
    }

    public boolean exists(String username) {
        return username != null && users.containsKey(username.trim().toLowerCase());
    }

    // ---- helpers ---------------------------------------------------------

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

    /** Compares two strings without leaking timing information. */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
