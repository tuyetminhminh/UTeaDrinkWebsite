let stompClient = null;
let currentConversation = null;
const adminId = Number(document.body.dataset.userid);

// ===========================
// 🔌 KẾT NỐI WEBSOCKET
// ===========================
function connectSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        console.log("✅ WebSocket connected");
        loadConversations();
    });
}

// ===========================
// 📥 TẢI DANH SÁCH KHÁCH HÀNG
// ===========================
async function loadConversations() {
    try {
        const res = await fetch("/api/chat/admin/conversations");
        if (!res.ok) throw new Error("Lỗi tải danh sách hội thoại");
        const list = await res.json();

        const container = document.getElementById("conversation-list");
        container.innerHTML = "";

        if (list.length === 0) {
            container.innerHTML = `<li class="list-group-item text-muted">Chưa có khách hàng nào nhắn tin.</li>`;
            return;
        }

        list.forEach(c => {
            const li = document.createElement("li");
            li.className = "list-group-item list-group-item-action";
            li.style.cursor = "pointer";
            li.innerHTML = `
        <div class="fw-semibold">${c.customerName}</div>
        <small class="text-muted">${c.lastMessagePreview || "(Chưa có tin nhắn)"}</small>
      `;
            li.addEventListener("click", () => openConversation(c));
            container.appendChild(li);
        });
    } catch (e) {
        console.error(e);
    }
}

// ===========================
// 💬 MỞ HỘI THOẠI
// ===========================
async function openConversation(c) {
    currentConversation = c.conversationId;
    document.getElementById("chat-title").textContent = `💬 Đang chat với: ${c.customerName}`;
    document.getElementById("message-input").disabled = false;
    document.getElementById("send-btn").disabled = false;

    const res = await fetch(`/api/chat/${currentConversation}/messages`);
    const msgs = await res.json();

    const box = document.getElementById("chat-box");
    box.innerHTML = "";
    msgs.reverse().forEach(showMessage);

    stompClient.subscribe(`/topic/conversations/${currentConversation}`, (msg) => {
        const data = JSON.parse(msg.body);
        showMessage(data);
    });
}

// ===========================
// 📨 HIỂN THỊ TIN NHẮN
// ===========================
function showMessage(msg) {
    const box = document.getElementById("chat-box");
    const wrap = document.createElement("div");
    wrap.className = "my-2";
    wrap.style.textAlign = msg.senderId === adminId ? "right" : "left";

    const bubble = document.createElement("div");
    bubble.className = `d-inline-block px-3 py-2 rounded text-white bg-${msg.senderId === adminId ? "warning" : "secondary"}`;
    bubble.style.maxWidth = "75%";
    bubble.textContent = msg.content;

    wrap.appendChild(bubble);
    box.appendChild(wrap);
    box.scrollTop = box.scrollHeight;
}

// ===========================
// 🚀 GỬI TIN NHẮN
// ===========================
document.getElementById("send-btn").addEventListener("click", () => {
    const input = document.getElementById("message-input");
    const content = input.value.trim();
    if (!content || !currentConversation) return;

    stompClient.send("/app/chat.send", {}, JSON.stringify({
        conversationId: currentConversation,
        senderId: adminId,
        content
    }));
    input.value = "";
});

connectSocket();
