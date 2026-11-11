package com.eduvos.chat.servlet;

import com.eduvos.chat.store.UserStore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles authentication (HTTP POST /login).
 *
 * Valid credentials create an authenticated session and redirect to the home
 * page; invalid credentials redisplay the login form with an error.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (UserStore.getInstance().authenticate(username, password)) {
            // Credentials OK - mark the session as authenticated.
            HttpSession session = request.getSession(true);
            session.setAttribute("username", username.trim());
            response.sendRedirect("home.jsp");
        } else {
            request.setAttribute("error", "Invalid username or password.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}
