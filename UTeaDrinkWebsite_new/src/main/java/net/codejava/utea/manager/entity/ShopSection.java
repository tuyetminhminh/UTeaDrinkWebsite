package net.codejava.utea.manager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "title", columnDefinition = "NVARCHAR(200)")
    private String title;

    @Column(name = "section_type", length = 50)
    private String sectionType; // e.g. FEATURED, TOP_SELLING, NEW_ARRIVALS

    @Column(name = "content_json", columnDefinition = "NVARCHAR(MAX)")
    private String contentJson; // store a small JSON describing products or criteria

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
