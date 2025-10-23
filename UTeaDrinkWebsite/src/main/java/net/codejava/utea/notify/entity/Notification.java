package net.codejava.utea.notify.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.notify.entity.enums.NotificationType;
import net.codejava.utea.common.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = { @Index(name = "ix_notif_user", columnList = "user_id"),
		@Index(name = "ix_notif_type", columnList = "type"),
		@Index(name = "ix_notif_created", columnList = "created_at") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private NotificationType type = NotificationType.SYSTEM;

	@Column(length = 200, columnDefinition = "NVARCHAR(200)")
	private String title;

	@Column(columnDefinition = "NVARCHAR(MAX)")
	private String content;

	/** payload phụ để deep-link, ví dụ {"orderId":123} */
	@Column(name = "meta_json", columnDefinition = "NVARCHAR(MAX)")
	private String metaJson;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "read_at")
	private LocalDateTime readAt;

	public boolean isRead() {
		return readAt != null;
	}
}
