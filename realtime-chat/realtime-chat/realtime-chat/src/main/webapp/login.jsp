<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login &middot; ChatStart</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="auth-bg">
    <div class="card">
        <div class="brand big">&#9889; ChatStart</div>
        <h1>Welcome back</h1>
        <p class="muted">Sign in to start chatting.</p>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="login" method="post">
            <label for="username">Username</label>
            <input type="text" id="username" name="username" required autofocus>

            <label for="password">Password</label>
            <input type="password" id="password" name="password" required>

            <button type="submit">Log in</button>
        </form>

        <p class="muted">No account yet? <a href="register.jsp">Create one</a></p>
    </div>
</body>
</html>
