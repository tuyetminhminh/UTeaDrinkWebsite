package net.codejava.utea.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_bans", indexes = {
    @Index(name = "ix_chatban_user", columnList = "user_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người dùng bị khóa chat
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Bị khóa cho đến khi
    @Column(name = "banned_until", nullable = false)
    private LocalDateTime bannedUntil;

    // Lý do khóa (nếu cần)
    @Column(name = "reason", columnDefinition = "NVARCHAR(255)")
    private String reason;

    @Column(name = "banned_at")
    @Builder.Default
    private LocalDateTime bannedAt = LocalDateTime.now();
}   