package com.eduvos.chat.websocket;

import com.google.gson.Gson;

import com.eduvos.chat.db.ChatDatabaseHelper;
import com.eduvos.chat.model.Message;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The JSR 356 WebSocket server endpoint for the chat.
 *
 * Responsibilities:
 *   - track every open connection,
 *   - receive a message from one client,
 *   - broadcast it to ALL connected clients (including the sender).
 *
 * The endpoint is reachable at ws(s)://host/<context>/chat. The
 * {@link HttpSessionConfigurator} supplies the authenticated username for each
 * connection, which the server stamps onto outgoing messages so a client can
 * never spoof another user's name.
 */
@ServerEndpoint(value = "/chat", configurator = HttpSessionConfigurator.class)
public class ChatEndpoint {

    /** All live sessions. synchronizedSet + explicit sync on iteration = thread-safe. */
    private static final Set<Session> sessions =
            Collections.synchronizedSet(new HashSet<Session>());

    private static final Gson gson = new Gson();
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final ChatDatabaseHelper db = ChatDatabaseHelper.getInstance();

    @OnOpen
    public void onOpen(Session session) {
        // Base64-encoded camera photos are larger than the default 8 KB text
        // buffer, so raise the incoming-message limit for this session.
        session.setMaxTextMessageBufferSize(2 * 1024 * 1024); // 2 MB

        String username = username(session);
        // Persist a non-null name so onClose can still name the user.
        session.getUserProperties().put("username", username);

        sessions.add(session);

        // Replay recent chat history (loaded from the database) to the new client only.
        List<Message> history = db.getRecentMessages(50);
        for (Message m : history) {
            sendTo(session, new ChatMessage(m.getType(), m.getSender(),
                                            m.getContent(), m.getFormattedTime()));
        }

        broadcast(new ChatMessage("system", null, username + " joined the chat", now()));
    }

    @OnMessage
    public void onMessage(String raw, Session session) {
        String username = username(session);

        ChatMessage incoming;
        try {
            incoming = gson.fromJson(raw, ChatMessage.class);
        } catch (Exception malformed) {
            // Fall back to treating the raw payload as plain text.
            incoming = new ChatMessage("text", null, raw, null);
        }
        if (incoming == null || incoming.getContent() == null
                || incoming.getContent().isEmpty()) {
            return;
        }

        String type = (incoming.getType() == null) ? "text" : incoming.getType();
        // The server decides the sender and timestamp - the client's values are ignored.
        ChatMessage outgoing =
                new ChatMessage(type, username, incoming.getContent(), now());

        // Persist the message to the database, then broadcast it to everyone.
        db.saveMessage(new Message(username, type, incoming.getContent(),
                                   System.currentTimeMillis()));
        broadcast(outgoing);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        broadcast(new ChatMessage("system", null, username(session) + " left the chat", now()));
    }

    @OnError
    public void onError(Session session, Throwable t) {
        // Keep the server healthy; just report the problem.
        System.err.println("[ChatEndpoint] error: " + t.getMessage());
    }

    // ---- helpers ---------------------------------------------------------

    /** Sends one message to every open connection. */
    private void broadcast(ChatMessage message) {
        String json = gson.toJson(message);
        synchronized (sessions) {
            for (Session s : sessions) {
                if (s.isOpen()) {
                    try {
                        // Basic (blocking) remote inside the lock serialises sends,
                        // which avoids concurrent-write errors on a single session.
                        s.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        System.err.println("[ChatEndpoint] send failed: " + e.getMessage());
                    }
                }
            }
        }
    }

    /** Sends one message to a single session (used to replay history on connect). */
    private void sendTo(Session session, ChatMessage message) {
        if (!session.isOpen()) {
            return;
        }
        try {
            session.getBasicRemote().sendText(gson.toJson(message));
        } catch (IOException e) {
            System.err.println("[ChatEndpoint] history send failed: " + e.getMessage());
        }
    }

    private String username(Session session) {
        Object u = session.getUserProperties().get("username");
        return (u == null) ? "Guest" : u.toString();
    }

    private String now() {
        return LocalTime.now().format(TIME);
    }
}
