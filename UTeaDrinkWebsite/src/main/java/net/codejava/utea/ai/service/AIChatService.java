package net.codejava.utea.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codejava.utea.ai.entity.AIChatMessage;
import net.codejava.utea.ai.entity.AIChatSession;
import net.codejava.utea.ai.entity.enums.MessageRole;
import net.codejava.utea.ai.repository.AIChatMessageRepository;
import net.codejava.utea.ai.repository.AIChatSessionRepository;
import net.codejava.utea.common.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatService {

    private final AIChatSessionRepository sessionRepo;
    private final AIChatMessageRepository messageRepo;
    private final GeminiService geminiService;
    private final AIContextService contextService;

    private static final int MAX_HISTORY_MESSAGES = 10; // Giới hạn history để tiết kiệm token

    /**
     * Chat với AI (cho user đã đăng nhập)
     */
    @Transactional
    public String chat(User user, String message, Long sessionId) {
        log.info("💬 User {} chatting with AI", user.getEmail());

        // Lấy hoặc tạo session
        AIChatSession session;
        if (sessionId != null) {
            session = sessionRepo.findById(sessionId)
                    .orElseGet(() -> createNewSession(user, null));
        } else {
            session = sessionRepo.findFirstByUserAndIsActiveTrueOrderByUpdatedAtDesc(user)
                    .orElseGet(() -> createNewSession(user, null));
        }

        return processChatMessage(session, message, user.getId());
    }

    /**
     * Chat với AI (cho guest)
     */
    @Transactional
    public String chatAsGuest(String guestSessionId, String message, Long sessionId) {
        log.info("💬 Guest {} chatting with AI", guestSessionId);

        AIChatSession session;
        if (sessionId != null) {
            session = sessionRepo.findById(sessionId)
                    .orElseGet(() -> createNewSession(null, guestSessionId));
        } else {
            session = sessionRepo.findFirstByGuestSessionIdAndIsActiveTrueOrderByUpdatedAtDesc(guestSessionId)
                    .orElseGet(() -> createNewSession(null, guestSessionId));
        }

        return processChatMessage(session, message, null);
    }

    private AIChatSession createNewSession(User user, String guestSessionId) {
        AIChatSession session = AIChatSession.builder()
                .user(user)
                .guestSessionId(guestSessionId)
                .title("Cuộc trò chuyện mới")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        return sessionRepo.save(session);
    }

    private String processChatMessage(AIChatSession session, String userMessage, Long userId) {
        log.info("💬 Processing message for session {}", session.getId());
        
        // Save user message
        AIChatMessage userMsg = AIChatMessage.builder()
                .session(session)
                .role(MessageRole.USER)
                .content(userMessage)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepo.save(userMsg);
        log.info("💾 Saved user message");

        // Build context
        String systemContext = contextService.buildDynamicContext(userMessage, userId);

        // Get conversation history
        List<AIChatMessage> history = messageRepo.findLatestBySessionId(
                session.getId(), 
                PageRequest.of(0, MAX_HISTORY_MESSAGES)
        );

        // Convert to Gemini format (reverse order - oldest first)
        List<GeminiService.ConversationMessage> geminiHistory = new ArrayList<>();
        for (int i = history.size() - 1; i >= 0; i--) {
            AIChatMessage msg = history.get(i);
            if (msg.getId().equals(userMsg.getId())) {
                continue; // Skip current message
            }
            geminiHistory.add(new GeminiService.ConversationMessage(
                    msg.getRole() == MessageRole.USER ? "user" : "model",
                    msg.getContent()
            ));
        }

        // Call Gemini
        log.info("🤖 Calling Gemini with {} history messages", geminiHistory.size());
        String aiReply;
        try {
            if (geminiHistory.isEmpty()) {
                aiReply = geminiService.chat(userMessage, systemContext);
            } else {
                aiReply = geminiService.chatWithHistory(geminiHistory, userMessage, systemContext);
            }
            log.info("✨ Gemini response received");
        } catch (Exception e) {
            log.error("❌ Gemini API error: {}", e.getMessage(), e);
            aiReply = "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ bộ phận hỗ trợ.";
        }

        // Save AI reply
        AIChatMessage aiMsg = AIChatMessage.builder()
                .session(session)
                .role(MessageRole.ASSISTANT)
                .content(aiReply)
                .contextData(systemContext)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepo.save(aiMsg);

        // Update session
        session.setUpdatedAt(LocalDateTime.now());
        if (session.getTitle().equals("Cuộc trò chuyện mới") && userMessage.length() > 5) {
            String title = userMessage.length() > 50 
                    ? userMessage.substring(0, 50) + "..." 
                    : userMessage;
            session.setTitle(title);
        }
        sessionRepo.save(session);

        return aiReply;
    }

    /**
     * Lấy lịch sử chat
     */
    public List<AIChatMessage> getHistory(Long sessionId) {
        return messageRepo.findBySession_IdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * Tạo session mới
     */
    @Transactional
    public AIChatSession createSession(User user, String guestSessionId) {
        return createNewSession(user, guestSessionId);
    }
}

