package com.eduvos.chat.model;

/**
 * An immutable user record. The plaintext password is never stored; we keep a
 * salted SHA-256 hash and the salt that produced it.
 */
public class User {

    private final String username;      // display name (original case)
    private final String passwordHash;  // Base64 SHA-256(salt + password)
    private final String salt;          // Base64 random salt

    public User(String username, String passwordHash, String salt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt()         { return salt; }
}
