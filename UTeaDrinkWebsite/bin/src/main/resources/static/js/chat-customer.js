let stompClient = null;
let conversationId = null;
const customerId = Number(document.body.dataset.userid);

// ==========================
// 🔌 KẾT NỐI WEBSOCKET
// ==========================
function connectSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, async () => {
        console.log("✅ WebSocket connected");
        await openChat();
    });
}

// ==========================
// MỞ HOẶC TẠO CUỘC TRÒ CHUYỆN
// ==========================
async function openChat() {
    try {
        const res = await fetch("/api/chat/open", { method: "POST" });
        conversationId = await res.json();
        console.log("Conversation ID:", conversationId);

        // tải tin nhắn cũ
        const msgRes = await fetch(`/api/chat/${conversationId}/messages`);
        const msgs = await msgRes.json();
        const box = document.getElementById("chat-box");
        box.innerHTML = "";
        msgs.reverse().forEach(showMessage);

        // đăng ký nhận tin nhắn realtime
        stompClient.subscribe(`/topic/conversations/${conversationId}`, (msg) => {
            const data = JSON.parse(msg.body);
            showMessage(data);
        });

        document.getElementById("message-input").disabled = false;
        document.getElementById("send-btn").disabled = false;

    } catch (e) {
        console.error("❌ Lỗi mở chat:", e);
    }
}

// ==========================
// GỬI TIN NHẮN
// ==========================
document.getElementById("send-btn").addEventListener("click", () => {
    const input = document.getElementById("message-input");
    const content = input.value.trim();
    if (!content || !conversationId) return;

    stompClient.send("/app/chat.send", {}, JSON.stringify({
        conversationId,
        senderId: customerId,
        content
    }));
    input.value = "";
});

// ==========================
// HIỂN THỊ TIN NHẮN
// ==========================
function showMessage(msg) {
    const box = document.getElementById("chat-box");
    const wrap = document.createElement("div");
    wrap.className = "my-2";
    wrap.style.textAlign = msg.senderId === customerId ? "right" : "left";

    const bubble = document.createElement("div");
    bubble.className = `d-inline-block px-3 py-2 rounded text-white bg-${msg.senderId === customerId ? "success" : "secondary"}`;
    bubble.style.maxWidth = "75%";
    bubble.textContent = msg.content;

    wrap.appendChild(bubble);
    box.appendChild(wrap);
    box.scrollTop = box.scrollHeight;
}

connectSocket();
