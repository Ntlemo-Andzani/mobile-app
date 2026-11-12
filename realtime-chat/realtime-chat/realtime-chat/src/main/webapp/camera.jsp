<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Camera &middot; ChatStart</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <nav class="topbar">
        <a class="brand" href="home.jsp">&#9889; ChatStart</a>
        <span class="spacer"></span>
        <a class="btn-ghost" href="conversations.jsp">&#128172; Conversations</a>
        <a class="btn-ghost" href="logout">Log out</a>
    </nav>

    <main class="camera">
        <h1>Camera</h1>
        <p class="muted">
            Capture a photo and share it straight into the conversation.
            The browser only allows camera access over HTTPS or on localhost.
        </p>

        <div id="status" class="status">Connecting&hellip;</div>

        <div class="stage">
            <video id="video" class="hidden" autoplay playsinline></video>
            <img id="preview" class="hidden" alt="Captured photo">
            <canvas id="canvas" class="hidden"></canvas>
        </div>

        <div class="controls">
            <button id="start">Start camera</button>
            <button id="capture" disabled>Capture</button>
            <button id="retake" disabled>Retake</button>
            <button id="send" disabled>Send to chat</button>
        </div>

        <h3>Recently sent</h3>
        <div id="gallery" class="gallery"></div>

        <p class="muted">
            <a href="conversations.jsp">&rarr; Open Conversations to see shared photos</a>
        </p>
    </main>

    <script src="js/camera.js"></script>
</body>
</html>
