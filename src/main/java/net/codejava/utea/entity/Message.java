package net.codejava.utea.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ===================== 🔗 QUAN HỆ ===================== */

    // Liên kết tới Conversation (nhiều message thuộc 1 conversation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // Liên kết tới người gửi (Account)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Account sender;

    /* ===================== 💬 NỘI DUNG ===================== */

    @Column(nullable = false, columnDefinition = "nvarchar(max)")
    private String content;

    /* ===================== 🕓 THỜI GIAN + TRẠNG THÁI ===================== */

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    /* ===================== ⚙️ HOOK ===================== */

    @PrePersist
    protected void onCreate() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }
}