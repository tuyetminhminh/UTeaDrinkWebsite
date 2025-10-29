package net.codejava.utea.order.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.Address;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.payment.entity.PaymentTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = { @Index(name = "ix_order_user", columnList = "user_id"),
		@Index(name = "ix_order_shop", columnList = "shop_id"),
		@Index(name = "ix_order_code", columnList = "orderCode", unique = true) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Mã hiển thị cho người dùng (ORD-2025-000123…) */
	@Column(length = 40, unique = true)
	private String orderCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private OrderStatus status = OrderStatus.NEW;

	/** snapshot địa chỉ giao tại thời điểm đặt */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "address_id")
	private Address shippingAddress;

	/** tiền hàng + phí ship + giảm giá */
	@Column(precision = 12, scale = 2, nullable = false)
	private BigDecimal subtotal;

	@Column(precision = 12, scale = 2, nullable = false)
	private BigDecimal shippingFee;

	@Column(precision = 12, scale = 2, nullable = false)
	private BigDecimal discount;

	@Column(precision = 12, scale = 2, nullable = false)
	private BigDecimal total;

	/** mã voucher áp dụng (nếu có) – chỉ lưu code */
	@Column(length = 50)
	private String voucherCode;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<OrderItem> items = new ArrayList<>();

	/** thanh toán (nếu có) */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_txn_id")
	private PaymentTransaction payment;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<OrderStatusHistory> statusHistories = new ArrayList<>();

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/** tiện ích */
	public void addItem(OrderItem item) {
		item.setOrder(this);
		this.items.add(item);
	}

	public void addHistory(OrderStatusHistory h) {
		h.setOrder(this);
		this.statusHistories.add(h);
	}
}
