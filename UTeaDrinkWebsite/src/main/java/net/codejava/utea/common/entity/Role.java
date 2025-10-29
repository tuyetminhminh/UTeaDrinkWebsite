package net.codejava.utea.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code; // ADMIN | MANAGER | CUSTOMER | SHIPPER
}
