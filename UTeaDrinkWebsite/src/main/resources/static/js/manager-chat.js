// src/main/resources/static/js/chat/manager-chat.js
(function(){
    let stomp, conversationId = null;
    const $list = document.getElementById('convList');
    const $msgs = document.getElementById('messages');
    const $title = document.getElementById('chatTitle');
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

    async function loadConversations(){
        const res = await fetch('/api/chat/manager/conversations');
        const data = await res.json();
        $list.innerHTML = '';
        data.forEach(c=>{
            const el = document.createElement('div');
            el.className = 'item';
            el.dataset.id = c.id;
            el.innerHTML = `<div><strong>${escapeHtml(c.customerName||('KH-'+c.customerId))}</strong></div>
        <div class="text-muted small">${escapeHtml(c.lastSnippet||'')}</div>`;
            el.onclick = ()=> openConversation(c);
            $list.appendChild(el);
        });
    }

    async function openConversation(c){
        Array.from($list.children).forEach(i=>i.classList.remove('active'));
        const current = Array.from($list.children).find(i=> i.dataset.id==c.id);
        if(current) current.classList.add('active');

        conversationId = c.id;
        $title.textContent = `Chat với ${c.customerName || ('KH-'+c.customerId)}`;
        $msgs.innerHTML = '';
        $send.disabled = false;

        const hx = await fetch(`/api/chat/history?conversationId=${conversationId}`);
        (await hx.json()).forEach(append);

        if(window._convSub){ window._convSub.unsubscribe(); }
        window._convSub = stomp.subscribe(`/topic/conversation.${conversationId}`, (frame)=>{
            append(JSON.parse(frame.body));
        });
    }

    function connect(){
        const sock = new SockJS('/ws');
        stomp = Stomp.over(sock);
        stomp.connect({}, async ()=>{
            await loadConversations();
            $send.onclick = send;
            $input.addEventListener('keydown', e=>{ if(e.key==='Enter') send(); });
        });
    }

    function send(){
        const text = $input.value.trim();
        if(!text || !conversationId) return;
        stomp.send('/app/chat.send', {}, JSON.stringify({ conversationId, content: text }));
        $input.value = '';
    }

    connect();
})();
