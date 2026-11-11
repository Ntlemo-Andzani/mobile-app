<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home &middot; ChatStart</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <nav class="topbar">
        <span class="brand">&#9889; ChatStart</span>
        <span class="spacer"></span>
        <span class="muted">Signed in as <strong>${sessionScope.username}</strong></span>
        <a class="btn-ghost" href="logout">Log out</a>
    </nav>

    <main class="home">
        <h1>Welcome, ${sessionScope.username}! &#128075;</h1>
        <p class="muted">What would you like to do?</p>

        <div class="tiles">
            <a class="tile" href="conversations.jsp">
                <div class="tile-icon">&#128172;</div>
                <h2>Conversations</h2>
                <p>Join the live chat and message other users in real time.</p>
            </a>

            <a class="tile" href="camera.jsp">
                <div class="tile-icon">&#128247;</div>
                <h2>Camera</h2>
                <p>Capture a photo with your device camera and share it to the chat.</p>
            </a>
        </div>
    </main>
</body>
</html>
