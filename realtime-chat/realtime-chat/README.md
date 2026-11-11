# ChatStart — Real-Time Messaging Application

A real-time chat web application built with the **Java WebSocket API (JSR 356)**,
**Java Servlets**, **JSP**, and **client-side JavaScript**. Users register and log
in, chat live with everyone online, and capture photos from their device camera to
share straight into the conversation.

---

## Technology stack

| Layer            | Technology                                            |
|------------------|-------------------------------------------------------|
| Real-time comms  | Java WebSocket server/client endpoints (JSR 356)      |
| HTTP / routing   | Java Servlets + a servlet Filter                      |
| Views            | JSP (with EL for the welcome message)                 |
| Client logic     | Vanilla JavaScript (WebSocket API, MediaDevices API)  |
| JSON             | Gson                                                  |
| Build            | Maven (`war` packaging)                               |
| Runtime          | Apache Tomcat 9 (Servlet 4.0 / WebSocket 1.1)         |

---

## How each requirement is met

### User Authentication
- **Registration** — `RegisterServlet` (`POST /register`) validates input, then
  `UserStore` creates the account with a **salted SHA-256** password hash.
- **Login** — `LoginServlet` (`POST /login`) verifies credentials and starts an
  authenticated `HttpSession`.
- `AuthFilter` blocks the home, conversations and camera pages unless the user is
  logged in. `LogoutServlet` (`GET /logout`) ends the session.

### Home Page (`home.jsp`)
- Shows a personalised welcome message (`Welcome, <username>!`) via JSP EL.
- Navigation tiles link to the **Conversations** and **Camera** pages.

### Conversations Page (`conversations.jsp` + `chat.js`)
- Authenticated users exchange messages in real time.
- `ChatEndpoint` is the **WebSocket server endpoint** (`@ServerEndpoint("/chat")`)
  that receives each message and **broadcasts it to all connected clients**.

### Camera Page (`camera.jsp` + `camera.js`)
- Uses the browser `getUserMedia` API for a live preview, captures a still frame to
  a `<canvas>`, and sends it over the same `/chat` WebSocket as an image message —
  so it appears in everyone's Conversations view instantly.

### Client-Side Interaction
- `chat.js` opens and manages the WebSocket, sends messages, and updates the UI
  live as messages arrive (auto-reconnect included).
- Servlets handle registration, login and routing between pages.

---

## Project structure

```
realtime-chat/
├── pom.xml
└── src/main/
    ├── java/com/eduvos/chat/
    │   ├── model/User.java                     # user record (username + salted hash)
    │   ├── store/UserStore.java                # in-memory account store (singleton)
    │   ├── servlet/RegisterServlet.java        # POST /register
    │   ├── servlet/LoginServlet.java           # POST /login
    │   ├── servlet/LogoutServlet.java          # GET  /logout
    │   ├── filter/AuthFilter.java              # protects authenticated pages
    │   └── websocket/
    │       ├── ChatEndpoint.java               # @ServerEndpoint("/chat") — broadcast
    │       ├── ChatMessage.java                # JSON message model
    │       └── HttpSessionConfigurator.java    # shares the login session with the socket
    └── webapp/
        ├── WEB-INF/web.xml
        ├── login.jsp  register.jsp  home.jsp  conversations.jsp  camera.jsp
        ├── css/style.css
        └── js/chat.js  js/camera.js
```

---

## Build & run

### Option A — Maven + Tomcat
1. Install JDK 8+ and Apache **Tomcat 9**.
2. Build the WAR:
   ```
   mvn clean package
   ```
   This produces `target/realtime-chat.war`.
3. Copy `realtime-chat.war` into Tomcat's `webapps/` folder and start Tomcat.
4. Open **http://localhost:8080/realtime-chat/**

### Option B — IDE (NetBeans / IntelliJ / Eclipse)
1. Open the folder as a **Maven project**.
2. Add a Tomcat 9 server and **Run** the project.
3. The browser opens at the application's context root.

### Try it
1. Register an account, then log in.
2. Open **Conversations** in two browser tabs (or two devices) — messages appear in
   both instantly.
3. Open **Camera**, click *Start camera* → *Capture* → *Send to chat*. The photo
   shows up in the Conversations view.

> **Camera note:** browsers only grant camera access over **HTTPS** or on
> **localhost**. On `http://localhost:8080` it works out of the box; over a plain
> `http://<LAN-ip>` address it will not — use localhost or enable HTTPS.

---

## Running on Tomcat 10+ (Jakarta EE)

Tomcat 10 renamed the APIs from `javax.*` to `jakarta.*`. This project targets
**Tomcat 9** (`javax.*`). To run on Tomcat 10+:

1. In `pom.xml`, change the API dependencies to the Jakarta versions:
   - `javax.servlet:javax.servlet-api` → `jakarta.servlet:jakarta.servlet-api` (6.x)
   - `javax.websocket:javax.websocket-api` → `jakarta.websocket:jakarta.websocket-api` (2.x)
   - `javax.servlet.jsp:javax.servlet.jsp-api` → `jakarta.servlet.jsp:jakarta.servlet.jsp-api`
2. In every `.java` file, replace imports `javax.servlet.*` / `javax.websocket.*`
   with `jakarta.servlet.*` / `jakarta.websocket.*`.
3. In `web.xml`, update the namespace to the Jakarta schema (version `5.0`).

---

## Notes & possible extensions
- Accounts are stored **in memory** and reset when the server restarts — this keeps
  the project self-contained. Swap `UserStore` for a JDBC/JPA store to persist them.
- For production, replace single-pass SHA-256 with a slow hash such as **bcrypt** or
  **PBKDF2**, and serve the app over HTTPS.
- Messages are broadcast to everyone (a single public room). Private rooms could be
  added by grouping WebSocket `Session`s by a room id.
