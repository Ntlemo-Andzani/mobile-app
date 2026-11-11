package com.eduvos.chat.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Guards the authenticated pages. Any request for the home, conversations or
 * camera page without a logged-in session is redirected to the login page.
 *
 * This is the server-side gate that enforces "authenticated users only" for
 * the chat and camera features.
 */
@WebFilter(urlPatterns = {"/home.jsp", "/conversations.jsp", "/camera.jsp"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("username") != null;

        if (loggedIn) {
            chain.doFilter(req, res);                 // allow through
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}
