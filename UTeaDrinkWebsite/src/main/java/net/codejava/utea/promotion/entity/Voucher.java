package net.codejava.utea.promotion.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.promotion.entity.enums.PromoScope;

import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers", indexes = { @Index(name = "ix_voucher_code", columnList = "code", unique = true),
		@Index(name = "ix_voucher_scope", columnList = "scope"),
		@Index(name = "ix_voucher_shop", columnList = "shop_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 40, nullable = false, unique = true)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(length = 10, nullable = false)
	private PromoScope scope = PromoScope.GLOBAL;

	/** null nếu GLOBAL */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id")
	private Shop shop;

	/** luật áp dụng tương tự Promotion (để gom logic) */
	@Column(name = "rule_json", columnDefinition = "NVARCHAR(MAX)")
	private String ruleJson;

	/** cờ thuận tiện: chỉ cho đơn đầu tiên / sinh nhật */
	private Boolean forFirstOrder;
	private Boolean forBirthday;

	@Column(name = "active_from")
	private LocalDateTime activeFrom;

	@Column(name = "active_to")
	private LocalDateTime activeTo;

	@Column(length = 20)
	private String status = "ACTIVE"; // ACTIVE/INACTIVE/EXHAUSTED

	/** giới hạn số lần dùng toàn hệ thống (null = không giới hạn) */
	private Integer usageLimit;

	/** đã dùng bao nhiêu lần */
	private Integer usedCount;
}
