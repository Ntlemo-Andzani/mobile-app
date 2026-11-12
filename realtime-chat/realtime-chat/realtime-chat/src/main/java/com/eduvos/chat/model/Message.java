package com.eduvos.chat.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Entity (POJO) that represents one row in the {@code messages} table.
 *
 * A message can be plain text or an image (stored as a data-URL). The fields
 * line up with the JSON exchanged over the WebSocket in Deliverable 1, so a
 * persisted {@code Message} can be turned straight back into a chat message.
 */
public class Message {

    /** Allowed values for {@link #type}. */
    public static final String TYPE_TEXT  = "text";
    public static final String TYPE_IMAGE = "image";

    private int    id;         // primary key (auto-increment)
    private String sender;     // author's username
    private String type;       // TYPE_TEXT or TYPE_IMAGE
    private String content;    // message text, or a data-URL for an image
    private long   createdAt;  // epoch milliseconds

    /** No-arg constructor - used when mapping a row from the database. */
    public Message() {
    }

    /** New message that does not yet have a database id. */
    public Message(String sender, String type, String content, long createdAt) {
        this.sender = sender;
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
    }

    /** Full constructor - used when reading an existing row. */
    public Message(int id, String sender, String type, String content, long createdAt) {
        this.id = id;
        this.sender = sender;
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
    }

    // ---- getters / setters ----------------------------------------------

    public int getId()                 { return id; }
    public void setId(int id)          { this.id = id; }

    public String getSender()                { return sender; }
    public void setSender(String sender)     { this.sender = sender; }

    public String getType()                  { return type; }
    public void setType(String type)         { this.type = type; }

    public String getContent()               { return content; }
    public void setContent(String content)   { this.content = content; }

    public long getCreatedAt()         { return createdAt; }
    public void setCreatedAt(long ts)  { this.createdAt = ts; }

    /** @return the creation time formatted as HH:mm in the server's time zone. */
    public String getFormattedTime() {
        return DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(createdAt));
    }

    /** @return true if this message carries an image rather than text. */
    public boolean isImage() {
        return TYPE_IMAGE.equals(type);
    }

    @Override
    public String toString() {
        return "Message{id=" + id + ", sender='" + sender + "', type='" + type
                + "', time=" + getFormattedTime() + "}";
    }
}
