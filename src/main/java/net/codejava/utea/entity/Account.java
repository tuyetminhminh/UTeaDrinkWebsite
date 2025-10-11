package net.codejava.utea.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String role; // ADMIN | CUSTOMER | SELLER | SHIPPER

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /* ==========================================================
       🔁 QUAN HỆ NGƯỢC (One-to-Many / One-to-One)
       ========================================================== */

    // 1️⃣ Account → Customer (1-1)
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private Customer customer;

    // 2️⃣ Account → Conversations (admin hoặc customer)
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Conversation> adminConversations;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Conversation> customerConversations;

    // 3️⃣ Account → Messages (người gửi)
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Message> sentMessages;

    // 4️⃣ Account → PasswordResetOtp (nhiều OTP)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<PasswordResetOtp> otps;


}
