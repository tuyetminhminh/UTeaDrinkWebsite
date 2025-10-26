package net.codejava.utea.shipping.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.shipping.entity.enums.ShipmentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments", indexes = { @Index(name = "ix_ship_order", columnList = "order_id", unique = true),
		@Index(name = "ix_ship_shop", columnList = "shop_id"),
		@Index(name = "ix_ship_shipper", columnList = "shipper_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** đơn nào */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	/** thuộc shop nào (để lọc theo phạm vi shop) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	/** shipper được gán (có thể null lúc mới tạo) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shipper_id")
	private ShipperProfile shipper;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private ShipmentStatus status = ShipmentStatus.ASSIGNED;

	/** ghi chú & bằng chứng giao hàng (POD) */
	@Column(columnDefinition = "NVARCHAR(500)")
	private String note;

	@Column(name = "pod_image_url", length = 500)
	private String podImageUrl;

	@Column(name = "assigned_at")
	private LocalDateTime assignedAt = LocalDateTime.now();

	@Column(name = "picked_at")
	private LocalDateTime pickedAt;

	@Column(name = "delivered_at")
	private LocalDateTime deliveredAt;

	@Column(name = "failed_at")
	private LocalDateTime failedAt;
}
