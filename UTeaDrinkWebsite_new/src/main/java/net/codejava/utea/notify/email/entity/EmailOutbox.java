package net.codejava.utea.notify.email.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.notify.email.entity.enums.EmailStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_outbox", indexes = { @Index(name = "ix_email_status", columnList = "status"),
		@Index(name = "ix_email_next", columnList = "next_retry_at") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailOutbox {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 300)
	private String toEmail;

	@Column(length = 200, columnDefinition = "NVARCHAR(200)")
	private String subject;

	@Column(columnDefinition = "NVARCHAR(MAX)")
	private String bodyHtml;

	@Column(columnDefinition = "NVARCHAR(MAX)")
	private String bodyText;

	@Enumerated(EnumType.STRING)
	@Column(length = 10, nullable = false)
	private EmailStatus status = EmailStatus.PENDING;

	private Integer retryCount = 0;

	@Column(name = "next_retry_at")
	private LocalDateTime nextRetryAt;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@Column(name = "error_message", columnDefinition = "NVARCHAR(500)")
	private String errorMessage;
}
