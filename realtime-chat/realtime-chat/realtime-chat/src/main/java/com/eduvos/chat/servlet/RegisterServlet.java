package com.eduvos.chat.servlet;

import com.eduvos.chat.db.ChatDatabaseHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles account creation (HTTP POST /register).
 *
 * On success the user is logged in automatically (username stored in the HTTP
 * session) and redirected to the home page. On failure the registration form
 * is redisplayed with an error message.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final String USERNAME_PATTERN = "[A-Za-z0-9_]{3,20}";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm  = request.getParameter("confirm");

        // --- validation ---------------------------------------------------
        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            fail(request, response, "Username and password are required.");
            return;
        }
        if (!username.trim().matches(USERNAME_PATTERN)) {
            fail(request, response,
                 "Username must be 3-20 characters: letters, numbers or underscore.");
            return;
        }
        if (password.length() < 6) {
            fail(request, response, "Password must be at least 6 characters.");
            return;
        }
        if (confirm != null && !password.equals(confirm)) {
            fail(request, response, "Passwords do not match.");
            return;
        }

        // --- create the account -------------------------------------------
        boolean created = ChatDatabaseHelper.getInstance().registerUser(username, password);
        if (!created) {
            fail(request, response, "That username is already taken.");
            return;
        }

        // Auto-login: establish the session and go to the home page.
        HttpSession session = request.getSession(true);
        session.setAttribute("username", username.trim());
        response.sendRedirect("home.jsp");
    }

    /** Show the blank registration form for a direct GET. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("register.jsp").forward(request, response);
    }

    private void fail(HttpServletRequest request, HttpServletResponse response,
                      String message) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("register.jsp").forward(request, response);
    }
}
