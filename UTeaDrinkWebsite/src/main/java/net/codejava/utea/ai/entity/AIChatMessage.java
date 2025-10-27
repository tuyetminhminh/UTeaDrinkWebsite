package net.codejava.utea.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.ai.entity.enums.MessageRole;

import java.time.LocalDateTime;

/**
 * Tin nhắn trong conversation với AI
 */
@Entity
@Table(name = "ai_chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AIChatSession session;

    /**
     * Role: USER hoặc ASSISTANT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MessageRole role;

    /**
     * Nội dung tin nhắn
     */
    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    /**
     * Context đã cung cấp cho AI (JSON)
     */
    @Column(name = "context_data", columnDefinition = "NVARCHAR(MAX)")
    private String contextData;

    /**
     * Token count (để tracking cost)
     */
    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

