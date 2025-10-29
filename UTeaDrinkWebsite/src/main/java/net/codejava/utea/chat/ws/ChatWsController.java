// net/codejava/utea/chat/ws/ChatWsController.java
package net.codejava.utea.chat.ws;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.chat.dto.ChatMessagePayload;
import net.codejava.utea.chat.dto.MessageView;
import net.codejava.utea.chat.entity.Conversation;
import net.codejava.utea.chat.service.ChatService;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.common.service.CustomUserDetailsService; // 👈 dùng service của bạn
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

// ...
import net.codejava.utea.common.service.CustomUserDetailsService;
import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messaging;
    private final CustomUserDetailsService customUserDetailsService; // ✅

    @MessageMapping("/chat.send")
    public void handleSend(Principal principal, @Payload ChatMessagePayload payload) {
        if (principal == null) return;
        if (payload == null || payload.getContent() == null || payload.getContent().isBlank()) return;

        // principal.getName() = email hoặc username (tùy bạn set vào JWT)
        var me = customUserDetailsService.loadByPrincipalName(principal.getName());

        Long conversationId = payload.getConversationId();
        if (conversationId == null) {
            var c = chatService.getOrCreateCustomerToManager(me.getUser());
            conversationId = c.getId();
        }

        MessageView saved = chatService.sendMessage(
                conversationId, me.getId(), payload.getContent(), payload.getImageUrl());

        messaging.convertAndSend("/topic/conversation." + saved.getConversationId(), saved);
    }
}
