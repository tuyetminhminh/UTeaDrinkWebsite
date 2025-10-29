package net.codejava.utea.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codejava.utea.ai.dto.ChatRequest;
import net.codejava.utea.ai.dto.ChatResponse;
import net.codejava.utea.ai.entity.AIChatMessage;
import net.codejava.utea.ai.service.AIChatService;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.service.CustomUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIChatController {

    private final AIChatService aiChatService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Endpoint chat với AI
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            Authentication authentication) {

        try {
            log.info("📨 Received chat request: {}", request.getMessage());
            
            if (request.getMessage() == null || request.getMessage().isBlank()) {
                return ResponseEntity.badRequest().body(
                        ChatResponse.builder()
                                .success(false)
                                .error("Tin nhắn không được để trống")
                                .build()
                );
            }

            String aiReply;
            Long sessionId = request.getSessionId();

            if (authentication != null && authentication.isAuthenticated()) {
                // User đã đăng nhập
                log.info("👤 Authenticated user: {}", authentication.getName());
                var userDetails = userDetailsService.loadByPrincipalName(authentication.getName());
                User user = userDetails.getUser();

                aiReply = aiChatService.chat(user, request.getMessage(), sessionId);

            } else {
                // Guest user
                log.info("👻 Guest user");
                String guestId = request.getGuestSessionId();
                if (guestId == null || guestId.isBlank()) {
                    guestId = "guest-" + System.currentTimeMillis();
                    log.info("🆕 Created new guest ID: {}", guestId);
                }

                aiReply = aiChatService.chatAsGuest(guestId, request.getMessage(), sessionId);
            }

            log.info("✅ AI replied successfully");
            return ResponseEntity.ok(
                    ChatResponse.builder()
                            .success(true)
                            .reply(aiReply)
                            .sessionId(sessionId)
                            .build()
            );

        } catch (Exception e) {
            log.error("❌ Error in AI chat: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    ChatResponse.builder()
                            .success(false)
                            .error("Có lỗi xảy ra. Vui lòng thử lại sau.")
                            .build()
            );
        }
    }

    /**
     * Lấy lịch sử chat
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable Long sessionId) {
        try {
            List<AIChatMessage> messages = aiChatService.getHistory(sessionId);
            
            List<Map<String, Object>> messageList = messages.stream()
                    .map(msg -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", msg.getId());
                        map.put("role", msg.getRole().toString());
                        map.put("content", msg.getContent());
                        map.put("createdAt", msg.getCreatedAt().toString());
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", messageList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting chat history: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Không thể lấy lịch sử chat");
            return ResponseEntity.status(500).body(error);
        }
    }
}

