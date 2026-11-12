<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register &middot; ChatStart</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="auth-bg">
    <div class="card">
        <div class="brand big">&#9889; ChatStart</div>
        <h1>Create your account</h1>
        <p class="muted">Pick a username and password to get started.</p>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="register" method="post">
            <label for="username">Username</label>
            <input type="text" id="username" name="username"
                   pattern="[A-Za-z0-9_]{3,20}"
                   title="3-20 characters: letters, numbers or underscore"
                   required autofocus>

            <label for="password">Password</label>
            <input type="password" id="password" name="password"
                   minlength="6" required>

            <label for="confirm">Confirm password</label>
            <input type="password" id="confirm" name="confirm"
                   minlength="6" required>

            <button type="submit">Create account</button>
        </form>

        <p class="muted">Already registered? <a href="login.jsp">Log in</a></p>
    </div>
</body>
</html>
