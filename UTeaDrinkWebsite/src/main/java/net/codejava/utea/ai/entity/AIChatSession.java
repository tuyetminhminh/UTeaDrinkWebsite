package net.codejava.utea.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lưu phiên chat với AI của user
 */
@Entity
@Table(name = "ai_chat_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User đang chat (có thể null nếu là guest)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Session ID cho guest (cookie/localStorage)
     */
    @Column(name = "guest_session_id", columnDefinition = "NVARCHAR(100)")
    private String guestSessionId;

    /**
     * Tiêu đề conversation (tự động tạo từ tin nhắn đầu tiên)
     */
    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<AIChatMessage> messages = new ArrayList<>();

    /**
     * Trạng thái active
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}

