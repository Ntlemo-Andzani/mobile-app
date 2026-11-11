/*
 * Camera page client.
 *
 * - Uses the MediaDevices getUserMedia API to show a live camera preview.
 * - Captures a still frame onto a <canvas> and turns it into a JPEG data-URL.
 * - Sends that image over the SAME /chat WebSocket as an image message, so it
 *   appears in everyone's Conversations view in real time.
 *
 * Note: browsers only grant camera access in a secure context (HTTPS) or on
 * localhost.
 */
(function () {
    "use strict";

    var video    = document.getElementById("video");
    var canvas   = document.getElementById("canvas");
    var preview  = document.getElementById("preview");
    var gallery  = document.getElementById("gallery");
    var statusEl = document.getElementById("status");

    var startBtn   = document.getElementById("start");
    var captureBtn = document.getElementById("capture");
    var retakeBtn  = document.getElementById("retake");
    var sendBtn    = document.getElementById("send");

    var stream   = null;   // active MediaStream
    var captured = null;   // last captured data-URL
    var socket;

    // ---- WebSocket (shared chat endpoint) --------------------------------
    function wsUrl() {
        var scheme = (location.protocol === "https:") ? "wss:" : "ws:";
        return scheme + "//" + location.host + contextPath() + "/chat";
    }
    function contextPath() {
        var parts = location.pathname.split("/");
        return (parts.length > 2) ? "/" + parts[1] : "";
    }
    function connect() {
        socket = new WebSocket(wsUrl());
        socket.onopen  = function () { setStatus("Ready"); };
        socket.onclose = function () { setStatus("Disconnected - reconnecting…"); setTimeout(connect, 2000); };
        socket.onerror = function () { setStatus("Connection error"); };
    }
    function setStatus(text) { statusEl.textContent = text; }

    // ---- camera ----------------------------------------------------------
    startBtn.addEventListener("click", function () {
        navigator.mediaDevices.getUserMedia({ video: { facingMode: "user" }, audio: false })
            .then(function (s) {
                stream = s;
                video.srcObject = stream;
                video.classList.remove("hidden");
                preview.classList.add("hidden");
                captureBtn.disabled = false;
                setStatus("Camera on");
            })
            .catch(function (err) {
                setStatus("Could not access camera: " + err.message);
            });
    });

    captureBtn.addEventListener("click", function () {
        // Scale down to max width 640 to keep the message small.
        var maxW  = 640;
        var scale = (video.videoWidth > maxW) ? maxW / video.videoWidth : 1;
        canvas.width  = Math.round(video.videoWidth  * scale);
        canvas.height = Math.round(video.videoHeight * scale);

        var ctx = canvas.getContext("2d");
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

        captured = canvas.toDataURL("image/jpeg", 0.7);
        preview.src = captured;

        preview.classList.remove("hidden");
        video.classList.add("hidden");
        sendBtn.disabled   = false;
        retakeBtn.disabled = false;
        setStatus("Photo captured - send it to the chat?");
    });

    retakeBtn.addEventListener("click", function () {
        captured = null;
        preview.classList.add("hidden");
        video.classList.remove("hidden");
        sendBtn.disabled   = true;
        retakeBtn.disabled = true;
        setStatus("Camera on");
    });

    sendBtn.addEventListener("click", function () {
        if (!captured) { return; }
        if (!socket || socket.readyState !== WebSocket.OPEN) {
            setStatus("Not connected");
            return;
        }
        socket.send(JSON.stringify({ type: "image", content: captured }));
        addThumb(captured);
        setStatus("Sent to chat ✓");

        // Reset back to the live preview for the next shot.
        captured = null;
        preview.classList.add("hidden");
        video.classList.remove("hidden");
        sendBtn.disabled   = true;
        retakeBtn.disabled = true;
    });

    function addThumb(src) {
        var img = document.createElement("img");
        img.className = "thumb";
        img.src = src;
        img.alt = "Sent photo";
        gallery.insertBefore(img, gallery.firstChild);
    }

    // Release the camera when leaving the page.
    window.addEventListener("beforeunload", function () {
        if (stream) {
            stream.getTracks().forEach(function (t) { t.stop(); });
        }
    });

    connect();
})();
