package net.codejava.utea.shipping.entity;

import jakarta.persistence.*;
import lombok.*;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.order.entity.Order;

import java.time.LocalDateTime;

@Entity
@Table(name = "ship_assignments", indexes = {
        @Index(name = "ix_ship_order", columnList = "order_id"),
        @Index(name = "ix_ship_shipper", columnList = "shipper_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", nullable = false)
    private User shipper; // shipper (User có role SHIPPER)

    @Column(length = 20)
    @Builder.Default
    private String status = "ASSIGNED"; // ASSIGNED, PICKED_UP, DELIVERING, DELIVERED, FAILED

    @Column(name = "assigned_at")
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    private String note;
}
