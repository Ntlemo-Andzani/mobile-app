package com.eduvos.chat.websocket;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Bridges the HTTP world and the WebSocket world.
 *
 * During the WebSocket handshake the browser sends its JSESSIONID cookie, so we
 * can reach the same HttpSession the servlets created at login. We copy the
 * logged-in username into the endpoint's user properties, where the
 * {@link ChatEndpoint} can read it. This is how the WebSocket knows *who* each
 * connection belongs to without trusting anything the client sends.
 */
public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {

        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if (httpSession != null) {
            Object username = httpSession.getAttribute("username");
            config.getUserProperties().put("username", username);
        }
    }
}
