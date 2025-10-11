package net.codejava.utea.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "conversations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"admin_id", "customer_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ===================== 🔗 QUAN HỆ ===================== */

    // ADMIN phía tham chiếu (nhiều conversation có thể thuộc 1 admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Account admin;

    // CUSTOMER phía tham chiếu (nhiều conversation có thể thuộc 1 customer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Account customer;

    /* ===================== 🕓 THỜI GIAN ===================== */

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /* ===================== 💬 LIÊN KẾT MESSAGE ===================== */

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;
}
