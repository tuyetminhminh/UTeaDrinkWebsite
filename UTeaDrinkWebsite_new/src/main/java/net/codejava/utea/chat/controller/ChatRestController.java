// net/codejava/utea/chat/controller/ChatRestController.java
package net.codejava.utea.chat.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.chat.dto.ConversationView;
import net.codejava.utea.chat.dto.MessageView;
import net.codejava.utea.chat.service.ChatService;
import net.codejava.utea.common.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;   // << thêm import

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    // ❗ Sửa: chỉ trả id để tránh lazy-init error khi open-in-view=false
    @GetMapping("/customer/conversation")
    public Map<String, Object> getOrCreateForCustomer(@AuthenticationPrincipal CustomUserDetails me){
        var c = chatService.getOrCreateCustomerToManager(me.getUser());
        return Map.of("id", c.getId()); // <-- chỉ trả id
    }

    @GetMapping("/manager/conversations")
    public List<ConversationView> managerConversations(@AuthenticationPrincipal CustomUserDetails me){
        return chatService.listForManager(me.getId());
    }

    @GetMapping("/customer/conversations")
    public List<ConversationView> customerConversations(@AuthenticationPrincipal CustomUserDetails me){
        return chatService.listForCustomer(me.getId());
    }

    @GetMapping("/history")
    public List<MessageView> history(@RequestParam Long conversationId,
                                     @RequestParam(defaultValue = "50") int limit){
        return chatService.loadLatestMessages(conversationId, limit);
    }
}
