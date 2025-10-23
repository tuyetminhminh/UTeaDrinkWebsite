package net.codejava.utea.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import net.codejava.utea.common.base.Auditable;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop extends Auditable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, columnDefinition = "NVARCHAR(200)")
	private String name;
	
	@Column(columnDefinition = "NVARCHAR(400)")
	private String address;
	
	@Column(length = 20)
	private String phone;
	
	@Column(length = 20)
	private String status = "OPEN";
}