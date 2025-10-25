document.addEventListener("DOMContentLoaded", async () => {
    const convoList = document.querySelector("#conversation-list");
    const chatBox = document.querySelector("#chat-box");
    const msgForm = document.querySelector("#msg-form");
    const msgInput = document.querySelector("#msg-input");

    let stompClient = null;
    let currentConvId = null;

    // 1️⃣ Load danh sách hội thoại (API)
    const loadConversations = async () => {
        const res = await fetch("/api/chat/admin/conversations");
        const data = await res.json();
        convoList.innerHTML = "";
        data.forEach(c => {
            const li = document.createElement("li");
            li.className = "list-group-item list-group-item-action";
            li.textContent = `${c.customerName}`;
            li.addEventListener("click", () => openConversation(c.conversationId, c.customerName));
            convoList.appendChild(li);
        });
    };

    // 2️⃣ Kết nối WebSocket (đã fix - thêm cookie + token)
    const connectSocket = () => {
        // ⚙️ Cho phép SockJS gửi cookie JWT
        const socket = new SockJS("/ws", null, { withCredentials: true });
        stompClient = Stomp.over(socket);

        // ✅ Lấy JWT token từ cookie
        function getCookie(name) {
            const value = `; ${document.cookie}`;
            const parts = value.split(`; ${name}=`);
            if (parts.length === 2) return parts.pop().split(";").shift();
        }

        const token = getCookie("UTEA_TOKEN");
        console.log("🧩 WebSocket token (manager) =", token);

        // ✅ Gửi token qua header Authorization
        stompClient.connect(
            { Authorization: token ? `Bearer ${token}` : "" },
            (frame) => {
                console.log("✅ WebSocket connected (manager):", frame);
            },
            (error) => {
                console.error("❌ WebSocket connect error (manager):", error);
            }
        );
    };

    // 3️⃣ Mở cuộc hội thoại
    const openConversation = async (convId, customerName) => {
        currentConvId = convId;
        document.querySelector("#chat-title").textContent = `Đang chat với ${customerName}`;
        chatBox.innerHTML = "";

        const res = await fetch(`/api/chat/messages?conversationId=${convId}`);
        const msgs = await res.json();
        msgs.forEach(m => showMessage(m));

        // Hủy subscribe cũ nếu có
        if (stompClient && stompClient.connected) {
            stompClient.unsubscribe("sub");
            stompClient.subscribe(`/topic/chat.${convId}`, msg => {
                const body = JSON.parse(msg.body);
                showMessage(body);
            }, { id: "sub" });
        }
    };

    // 4️⃣ Hiển thị tin nhắn
    const showMessage = (m) => {
        const div = document.createElement("div");
        div.className = "mb-2";
        div.innerHTML = `<b>${m.senderName}:</b> ${m.content}`;
        chatBox.appendChild(div);
        chatBox.scrollTop = chatBox.scrollHeight;
    };

    // 5️⃣ Gửi tin nhắn
    msgForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const content = msgInput.value.trim();
        if (!content || !currentConvId) return;

        stompClient.send(
            `/app/chat.send.${currentConvId}`,
            {},
            JSON.stringify({ content })
        );

        msgInput.value = "";
    });

    // 🚀 Khởi chạy
    await loadConversations();
    connectSocket();
});
