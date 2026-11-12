package com.eduvos.chat.model;

/**
 * Entity (POJO) that represents one row in the {@code users} table.
 *
 * The plaintext password is never stored - only a salted SHA-256 hash together
 * with the salt that produced it. Persistence and hashing are handled by
 * {@link com.eduvos.chat.db.ChatDatabaseHelper}; this class is just the data
 * carrier mapped to/from a {@code ResultSet}.
 */
public class User {

    private int    id;            // primary key (auto-increment)
    private String username;      // unique display name
    private String passwordHash;  // Base64 SHA-256(salt + password)
    private String salt;          // Base64 random salt
    private long   createdAt;     // epoch milliseconds the account was created

    /** No-arg constructor - used when mapping a row from the database. */
    public User() {
    }

    /**
     * Convenience constructor (kept compatible with Deliverable 1) for a new
     * account that does not yet have a database id.
     */
    public User(String username, String passwordHash, String salt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    /** Full constructor - used when reading an existing row. */
    public User(int id, String username, String passwordHash, String salt, long createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = createdAt;
    }

    // ---- getters / setters ----------------------------------------------

    public int getId()                 { return id; }
    public void setId(int id)          { this.id = id; }

    public String getUsername()                   { return username; }
    public void setUsername(String username)      { this.username = username; }

    public String getPasswordHash()               { return passwordHash; }
    public void setPasswordHash(String hash)      { this.passwordHash = hash; }

    public String getSalt()            { return salt; }
    public void setSalt(String salt)   { this.salt = salt; }

    public long getCreatedAt()         { return createdAt; }
    public void setCreatedAt(long ts)  { this.createdAt = ts; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "'}";
    }
}
