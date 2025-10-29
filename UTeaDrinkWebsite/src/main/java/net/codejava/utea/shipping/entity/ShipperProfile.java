package net.codejava.utea.shipping.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.codejava.utea.common.entity.User;

// ShipperProfile (hồ sơ tài xế giao hàng)
@Entity
@Table(name = "shipper_profiles", indexes = @Index(name = "ix_shipper_user", columnList = "user_id", unique = true))
@Getter
@Setter
public class ShipperProfile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(length = 50)
	private String vehicleType; // bike, car...

	@Column(length = 50)
	private String licenseNumber; // GPLX
	
	@Column(length = 200)
	private String note;
}
