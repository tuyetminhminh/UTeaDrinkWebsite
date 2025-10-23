package net.codejava.utea.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.chat.entity.enums.ConversationScope;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.manager.entity.Shop;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conversations", indexes = { @Index(name = "ix_conv_customer", columnList = "customer_id"),
		@Index(name = "ix_conv_admin", columnList = "admin_id"), @Index(name = "ix_conv_shop", columnList = "shop_id"),
		@Index(name = "ix_conv_lastmsg", columnList = "last_message_at") },
		// 1 khách + 1 admin + 1 shop + 1 scope -> chỉ 1 hội thoại
		uniqueConstraints = @UniqueConstraint(name = "uk_conv_parties", columnNames = { "customer_id", "admin_id",
				"shop_id", "scope" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** người hỏi (khách) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private User customer;

	/** người trả lời (admin/CS hoặc manager) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id", nullable = false)
	private User admin;

	/** phạm vi hội thoại */
	@Enumerated(EnumType.STRING)
	@Column(length = 10, nullable = false)
	private ConversationScope scope = ConversationScope.SYSTEM;

	/** nếu scope = SHOP thì shop != null */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id")
	private Shop shop;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "last_message_at")
	private LocalDateTime lastMessageAt;

	@OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sentAt ASC")
	private List<Message> messages;
}
