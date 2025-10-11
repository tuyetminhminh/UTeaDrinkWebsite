package net.codejava.utea.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "unit_name", nullable = false, unique = true, length = 50)
    private String unitName;

    @Column(length = 200)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 20)
    private String status = "ACTIVE";

    /* ===================== 🔁 QUAN HỆ NGƯỢC ===================== */
    // Một đơn vị có thể gắn với nhiều sản phẩm
    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;
}
