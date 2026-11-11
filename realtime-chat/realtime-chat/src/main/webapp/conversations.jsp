<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Conversations &middot; ChatStart</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <nav class="topbar">
        <a class="brand" href="home.jsp">&#9889; ChatStart</a>
        <span class="spacer"></span>
        <a class="btn-ghost" href="camera.jsp">&#128247; Camera</a>
        <a class="btn-ghost" href="logout">Log out</a>
    </nav>

    <main class="chat">
        <div id="status" class="status">Connecting&hellip;</div>
        <div id="messages" class="messages"></div>

        <form id="composer" class="composer" autocomplete="off">
            <input id="input" type="text" placeholder="Type a message&hellip;" autocomplete="off">
            <button type="submit">Send</button>
        </form>
    </main>

    <script>
        // The authenticated username, injected by the server. Used only to tell
        // "my" messages from everyone else's when rendering.
        window.CHAT_USER = "${sessionScope.username}";
    </script>
    <script src="js/chat.js"></script>
</body>
</html>
