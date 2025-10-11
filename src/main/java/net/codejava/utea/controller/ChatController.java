package net.codejava.utea.controller;

import net.codejava.utea.dto.ChatMessageDTO;
import net.codejava.utea.entity.Conversation;
import net.codejava.utea.entity.Message;
import net.codejava.utea.service.ChatService;
import net.codejava.utea.entity.Account;
import net.codejava.utea.repository.AccountRepository;
import net.codejava.utea.service.CustomUserDetails;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AccountRepository accountRepository;

    public ChatController(ChatService chatService,
                          SimpMessagingTemplate messagingTemplate,
                          AccountRepository accountRepository) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/chat")
    public String chatPage(@RequestParam(value = "customerId", required = false) Long customerId,
                           Model model,
                           Authentication authentication) {

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = user.getId();
        Account currentAcc = accountRepository.findById(currentUserId).orElseThrow();

        String role = currentAcc.getRole().toUpperCase();

        // -------------------- CUSTOMER --------------------
        if ("CUSTOMER".equals(role)) {
            // Lấy admin mặc định (có thể lấy admin đầu tiên trong DB)
            Account admin = accountRepository.findFirstByRole("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy admin"));

            Conversation conv = chatService.getOrCreateConversation(admin.getId(), currentAcc.getId());

            model.addAttribute("conversationId", conv.getId());
            model.addAttribute("messages", chatService.getMessages(conv.getId()));
            model.addAttribute("currentUserId", currentUserId);
            model.addAttribute("isAdmin", false);

            return "chat/chat";
        }


        // -------------------- ADMIN --------------------
        else if ("ADMIN".equals(role)) {
            List<Account> customers = accountRepository.findByRole("CUSTOMER");
            model.addAttribute("customers", customers);
            model.addAttribute("isAdmin", true);
            model.addAttribute("currentUserId", currentUserId);

            if (customerId != null) {
                Account cust = accountRepository.findById(customerId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy customer id=" + customerId));

                Conversation conv = chatService.getOrCreateConversation(currentAcc.getId(), cust.getId());

                model.addAttribute("conversationId", conv.getId());
                model.addAttribute("messages", chatService.getMessages(conv.getId()));
                model.addAttribute("activeCustomer", cust);
            }

            return "chat/chat-inbox"; // view admin
        }

        // -------------------- ROLE KHÁC --------------------
        else {
            throw new RuntimeException("Role không hợp lệ: " + currentAcc.getRole());
        }
    }

    @MessageMapping("/chat.send/{conversationId}")
    public void processMessage(@DestinationVariable Long conversationId, ChatMessageDTO chatMsg) {
        Message saved = chatService.saveMessage(conversationId, chatMsg.getSenderId(), chatMsg.getContent());
        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, saved);
    }
}
