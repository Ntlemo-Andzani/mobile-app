/*
 * Conversations page client.
 *
 * - Opens a WebSocket to the server's /chat endpoint.
 * - Sends typed messages as JSON: { type: "text", content: "..." }.
 * - Receives broadcast messages (text / image / system) and renders them live.
 * - Auto-reconnects if the connection drops.
 */
(function () {
    "use strict";

    var messagesEl = document.getElementById("messages");
    var statusEl   = document.getElementById("status");
    var form       = document.getElementById("composer");
    var input      = document.getElementById("input");

    var socket;

    // Build ws:// or wss:// URL for <context>/chat from the current location.
    function wsUrl() {
        var scheme = (location.protocol === "https:") ? "wss:" : "ws:";
        return scheme + "//" + location.host + contextPath() + "/chat";
    }

    // "/realtime-chat/conversations.jsp" -> "/realtime-chat"; ROOT deploy -> "".
    function contextPath() {
        var parts = location.pathname.split("/");
        return (parts.length > 2) ? "/" + parts[1] : "";
    }

    function connect() {
        socket = new WebSocket(wsUrl());

        socket.onopen = function () {
            statusEl.textContent = "Connected";
            statusEl.classList.add("ok");
        };

        socket.onmessage = function (event) {
            var msg;
            try { msg = JSON.parse(event.data); }
            catch (e) { msg = { type: "text", sender: "?", content: event.data }; }
            render(msg);
        };

        socket.onclose = function () {
            statusEl.textContent = "Disconnected - reconnecting…";
            statusEl.classList.remove("ok");
            setTimeout(connect, 2000); // retry
        };

        socket.onerror = function () {
            statusEl.textContent = "Connection error";
        };
    }

    function render(msg) {
        var wrap = document.createElement("div");

        if (msg.type === "system") {
            wrap.className = "msg system";
            wrap.textContent = msg.content + (msg.time ? "  · " + msg.time : "");
            messagesEl.appendChild(wrap);
            return scrollToEnd();
        }

        var mine = (msg.sender === window.CHAT_USER);
        wrap.className = "msg " + (mine ? "mine" : "theirs");

        var meta = document.createElement("div");
        meta.className = "meta";
        meta.textContent = (mine ? "You" : msg.sender) + (msg.time ? "  · " + msg.time : "");
        wrap.appendChild(meta);

        if (msg.type === "image") {
            var img = document.createElement("img");
            img.className = "bubble-img";
            img.src = msg.content;              // data-URL from the camera page
            img.alt = "Shared photo";
            wrap.appendChild(img);
        } else {
            var bubble = document.createElement("div");
            bubble.className = "bubble";
            bubble.textContent = msg.content;   // textContent => no HTML injection
            wrap.appendChild(bubble);
        }

        messagesEl.appendChild(wrap);
        scrollToEnd();
    }

    function scrollToEnd() {
        messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    form.addEventListener("submit", function (e) {
        e.preventDefault();
        var text = input.value.trim();
        if (!text || !socket || socket.readyState !== WebSocket.OPEN) {
            return;
        }
        socket.send(JSON.stringify({ type: "text", content: text }));
        input.value = "";
        input.focus();
    });

    connect();
})();
