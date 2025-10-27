package net.codejava.utea.order.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.order.entity.enums.OrderStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history", indexes = { @Index(name = "ix_osh_order", columnList = "order_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private OrderStatus status;

	@Column(columnDefinition = "NVARCHAR(500)")
	private String note;

	@Column(name = "changed_at")
	private LocalDateTime changedAt = LocalDateTime.now();
}
