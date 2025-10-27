package net.codejava.utea.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "sizes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Size {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=20)
    private String code; // S, M, L

    @Column(nullable=false, length=50, columnDefinition = "NVARCHAR(100)")
    private String name; // Nhỏ, Vừa, Lớn

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal extraPrice; // +0, +5k, +10k

    @Column(length=20)
    private String status = "ACTIVE";
}
