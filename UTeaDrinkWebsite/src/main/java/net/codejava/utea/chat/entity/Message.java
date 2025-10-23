package net.codejava.utea.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = { @Index(name = "ix_msg_conv", columnList = "conversation_id"),
		@Index(name = "ix_msg_sender", columnList = "sender_id"),
		@Index(name = "ix_msg_sent", columnList = "sent_at") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conversation_id", nullable = false)
	private Conversation conversation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
	private String content;

	@Column(name = "image_url", length = 500)
	private String imageUrl; // nếu gửi ảnh qua Cloudinary

	@Column(name = "sent_at")
	private LocalDateTime sentAt = LocalDateTime.now();

	@Column(name = "is_read", nullable = false)
	private boolean read = false;

	@PrePersist
	void onCreate() {
		if (sentAt == null)
			sentAt = LocalDateTime.now();
	}
}
