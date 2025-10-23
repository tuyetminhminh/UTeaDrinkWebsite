package net.codejava.utea.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiver_name", columnDefinition = "NVARCHAR(200)")
    private String receiverName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "line", columnDefinition = "NVARCHAR(400)")
    private String line;          // số nhà / đường

    @Column(name = "ward", columnDefinition = "NVARCHAR(150)")
    private String ward;

    @Column(name = "district", columnDefinition = "NVARCHAR(150)")
    private String district;

    @Column(name = "province", columnDefinition = "NVARCHAR(150)")
    private String province;

    @Column(name = "is_default")
    private boolean isDefault;

    /* owner */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
