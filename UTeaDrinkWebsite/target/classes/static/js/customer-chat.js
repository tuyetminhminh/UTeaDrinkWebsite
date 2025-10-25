document.addEventListener("DOMContentLoaded", async () => {
    const chatBox = document.querySelector("#chat-box");
    const msgForm = document.querySelector("#msg-form");
    const msgInput = document.querySelector("#msg-input");
    const chatTitle = document.querySelector("#chat-title");

    let stompClient = null;
    let convId = null;

    // 1️⃣ Bắt đầu chat với manager đầu tiên
    const startConversation = async () => {
        const res = await fetch("/api/chat/customer/start", { method: "POST" });
        convId = await res.json();
        chatTitle.textContent = "Trò chuyện với Quản lý";
    };

    // 2️⃣ Hàm tiện ích lấy cookie
    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(";").shift();
    }

    // 3️⃣ Kết nối WebSocket (đã fix token + cookie)
    const connectSocket = () => {
        // ⚙️ Cho phép gửi cookie kèm handshake (bắt buộc cho JWT)
        const socket = new SockJS("/ws", null, { withCredentials: true });
        stompClient = Stomp.over(socket);

        // 🔐 Lấy token JWT trong cookie
        const token = getCookie("UTEA_TOKEN");
        console.log("🧩 WebSocket token (customer) =", token);

        // 🛰️ Gửi token qua header khi CONNECT
        stompClient.connect(
            { Authorization: token ? `Bearer ${token}` : "" },
            (frame) => {
                console.log("✅ WebSocket connected (customer):", frame);

                // Khi kết nối thành công -> subscribe topic chat riêng
                stompClient.subscribe(`/topic/chat.${convId}`, msg => {
                    const body = JSON.parse(msg.body);
                    showMessage(body);
                });
            },
            (error) => {
                console.error("❌ WebSocket connect error (customer):", error);
            }
        );
    };

    // 4️⃣ Hiển thị tin nhắn trong khung chat
    const showMessage = (m) => {
        const div = document.createElement("div");
        div.className = "mb-2";
        div.innerHTML = `<b>${m.senderName}:</b> ${m.content}`;
        chatBox.appendChild(div);
        chatBox.scrollTop = chatBox.scrollHeight;
    };

    // 5️⃣ Gửi tin nhắn qua WebSocket
    msgForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const content = msgInput.value.trim();
        if (!content || !convId || !stompClient || !stompClient.connected) return;

        // Gửi nội dung chat
        stompClient.send(
            `/app/chat.send.${convId}`,
            {},
            JSON.stringify({ content })
        );

        // Xóa ô nhập
        msgInput.value = "";
    });

    // 🚀 Khởi động luồng chat
    await startConversation();
    connectSocket();
});
