package net.codejava.utea.chat.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.chat.dto.ChatMessageDto;
import net.codejava.utea.chat.entity.Conversation;
import net.codejava.utea.chat.entity.Message;
import net.codejava.utea.chat.repository.ConversationRepository;
import net.codejava.utea.chat.repository.MessageRepository;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

//====== DTO trả về FE để hiển thị (tránh lazy load) ======
record ChatMessageResponse(Long id, Long senderId, String senderName, String content, String sentAt) {
}

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

	private final UserRepository userRepo;
	private final SimpMessagingTemplate template;
	private final ConversationRepository conversationRepo;
	private final MessageRepository messageRepo;

	private static String displayNameOf(User u) {
        if (u == null) return "Unknown";
        if (u.getFullName() != null && !u.getFullName().isBlank()) return u.getFullName();
        // nếu muốn fallback thêm:
        // if (a.getCustomer()!=null && a.getCustomer().getFullName()!=null && !a.getCustomer().getFullName().isBlank())
        //     return a.getCustomer().getFullName();
        return u.getUsername(); // cuối cùng dùng username
    }
	
	// Client gửi: /app/chat.send/{conversationId}
	@MessageMapping("/chat.send/{conversationId}")
    public void handleMessage(@DestinationVariable Long conversationId,
                              ChatMessageDto inbound) {

        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

        User sender = userRepo.findById(inbound.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + inbound.getSenderId()));

        Message m = new Message();
        m.setConversation(conv);
        m.setSender(sender);
        m.setContent(inbound.getContent());
        m.setSentAt(LocalDateTime.now());
        m.setRead(false);

        Message saved = messageRepo.save(m);

        var out = new ChatMessageResponse(
                saved.getId(),
                sender.getId(),
                displayNameOf(sender),
                saved.getContent(),
                saved.getSentAt() != null ? saved.getSentAt().toString() : ""
        );

        // Phát về topic mà 2 bên subscribe
        template.convertAndSend("/topic/" + conversationId, out);
    }

	// HTTP: tải 50 tin gần nhất để hiển thị khi mở chat
	@GetMapping("/chat/history/{conversationId}")
	@ResponseBody
	public List<ChatMessageResponse> history(@PathVariable Long conversationId) {
		var list = messageRepo.findTop50ByConversation_IdOrderBySentAtDesc(conversationId);
		Collections.reverse(list); // để cũ -> mới
		return list.stream()
                .map(m -> new ChatMessageResponse(
                        m.getId(),
                        m.getSender().getId(),
                        displayNameOf(m.getSender()),
                        m.getContent(),
                        m.getSentAt() != null ? m.getSentAt().toString() : ""
                ))
                .toList();
	}
}
