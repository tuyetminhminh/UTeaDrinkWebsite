// src/main/resources/static/js/chat/customer-chat.js
(function(){
    let stomp, conversationId = null;
    const $msgs = document.getElementById('messages');
    const $input = document.getElementById('messageInput');
    const $send = document.getElementById('sendBtn');

    function escapeHtml(s){ return (s||'').replace(/[&<>"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c])); }
    function append(msg){
        const me = (msg.senderId === window.CURRENT_USER_ID);
        const el = document.createElement('div');
        el.className = 'msg' + (me ? ' me':'');
        el.innerHTML = `<div class="bubble"><div>${escapeHtml(msg.content||'')}</div>
      <small class="text-muted">${new Date(msg.sentAt).toLocaleTimeString()}</small></div>`;
        $msgs.appendChild(el);
        $msgs.scrollTop = $msgs.scrollHeight;
    }

    async function ensureConversation(){
        const res = await fetch('/api/chat/customer/conversation');
        const conv = await res.json();
        conversationId = conv.id;
        const hx = await fetch(`/api/chat/history?conversationId=${conversationId}`);
        (await hx.json()).forEach(append);
        subscribe();
    }

    function subscribe(){
        stomp.subscribe(`/topic/conversation.${conversationId}`, (frame)=>{
            append(JSON.parse(frame.body));
        });
    }

    function connect(){
        const sock = new SockJS('/ws');
        stomp = Stomp.over(sock);
        stomp.connect({}, async ()=>{
            await ensureConversation();
            $send.onclick = send;
            $input.addEventListener('keydown', e=>{ if(e.key==='Enter') send(); });
        });
    }

    function send(){
        const text = $input.value.trim();
        if(!text) return;
        stomp.send('/app/chat.send', {}, JSON.stringify({ conversationId, content: text }));
        $input.value = '';
    }

    connect();
})();
