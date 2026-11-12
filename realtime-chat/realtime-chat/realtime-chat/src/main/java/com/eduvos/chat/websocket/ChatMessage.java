package com.eduvos.chat.websocket;

/**
 * The wire format exchanged over the WebSocket, serialised to/from JSON by Gson.
 *
 * type    - "text" | "image" | "system"
 * sender  - the author's username (stamped by the server, never trusted from the client)
 * content - message text, or a data-URL for an image
 * time    - HH:mm the server received the message
 */
public class ChatMessage {

    private String type;
    private String sender;
    private String content;
    private String time;

    public ChatMessage() { }

    public ChatMessage(String type, String sender, String content, String time) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.time = time;
    }

    public String getType()                 { return type; }
    public void   setType(String type)      { this.type = type; }

    public String getSender()               { return sender; }
    public void   setSender(String sender)  { this.sender = sender; }

    public String getContent()              { return content; }
    public void   setContent(String content){ this.content = content; }

    public String getTime()                 { return time; }
    public void   setTime(String time)      { this.time = time; }
}
