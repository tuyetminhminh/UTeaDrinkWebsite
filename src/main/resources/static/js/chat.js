let stompClient = null;

function connect() {
    const convInput = document.getElementById("conversationId");
    if (!convInput) {
        console.warn("❌ Không tìm thấy conversationId → Có thể admin chưa chọn khách hàng.");
        return;
    }

    const convId = convInput.value;
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log("✅ WebSocket connected: " + frame);

        // Subscribe theo convId
        stompClient.subscribe('/topic/chat/' + convId, function (message) {
            const msgObj = JSON.parse(message.body);
            showMessage(msgObj);
        });
    });
}

function showMessage(msg) {
    const chatBox = document.getElementById("chat-box");
    if (!chatBox) return;

    const div = document.createElement("div");
    div.classList.add("message");
    if (msg.sender.role === "ADMIN") {
        div.classList.add("admin");
    } else {
        div.classList.add("customer");
    }

    const bubble = document.createElement("div");
    bubble.classList.add("bubble");

    const sender = document.createElement("small");
    sender.classList.add("fw-bold");
    sender.innerText = msg.sender.displayName;

    const content = document.createElement("p");
    content.innerText = msg.content;

    bubble.appendChild(sender);
    bubble.appendChild(content);
    div.appendChild(bubble);

    chatBox.appendChild(div);
    chatBox.scrollTop = chatBox.scrollHeight; // luôn cuộn xuống cuối
}

document.addEventListener("DOMContentLoaded", function () {
    connect();

    const form = document.getElementById("chatForm");
    if (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault();

            const convId = document.getElementById("conversationId").value;
            const senderId = document.getElementById("currentUserId").value;
            const content = document.getElementById("messageInput").value.trim();

            if (content && stompClient) {
                stompClient.send("/app/chat.send/" + convId, {}, JSON.stringify({
                    conversationId: convId,
                    senderId: senderId,
                    content: content
                }));

                document.getElementById("messageInput").value = "";
            }
        });
    }
});
