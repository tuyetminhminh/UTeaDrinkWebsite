package net.codejava.utea.chat.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.chat.entity.Conversation;
import net.codejava.utea.chat.repository.ConversationRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ChatPageController {

    private final ConversationRepository conversationRepo;

    // Inbox (ví dụ cho admin, tuỳ bạn lọc theo account)
    @GetMapping("/chat/inbox")
    public String inbox(Model model) {
        model.addAttribute("conversations", conversationRepo.findAll()); // TODO: filter theo người đăng nhập
        return "chat/chat-inbox";
    }

    // Trang chat Admin
    @GetMapping("/chat/admin/{id}")
    public String adminChat(@PathVariable Long id, Model model) {
        Conversation c = conversationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + id));
        model.addAttribute("conversation", c);
        return "chat/admin-chat";
    }

    // Trang chat Customer
    @GetMapping("/chat/customer/{id}")
    public String customerChat(@PathVariable Long id, Model model) {
        Conversation c = conversationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + id));
        model.addAttribute("conversation", c);
        return "chat/customer-chat";
    }
}
