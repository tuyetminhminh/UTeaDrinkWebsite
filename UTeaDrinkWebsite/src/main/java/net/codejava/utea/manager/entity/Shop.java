package net.codejava.utea.manager.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import net.codejava.utea.catalog.entity.Product;
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

    @NotBlank
    @Column(nullable = false, columnDefinition = "NVARCHAR(200)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(400)")
    private String address;

    @Size(max = 20)
    @Pattern(regexp = "^$|[0-9+(). \\-]{6,20}", message = "Số điện thoại không hợp lệ")
    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String status = "OPEN";

    @OneToMany(mappedBy = "shop", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REFRESH, CascadeType.DETACH })
    private List<Product> products = new ArrayList<>();

    @Transient
    private String tempManagerName;

    public String getManagerName() {
        return tempManagerName;
    }

    public void setManagerName(String tempManagerName) {
        this.tempManagerName = tempManagerName;
    }

    /** Chặn xoá nếu còn sản phẩm (không cần câu lệnh SQL). */
    @PreRemove
    private void preRemove() {
        if (products != null && !products.isEmpty()) {
            throw new IllegalStateException("Không thể xoá: cửa hàng còn sản phẩm.");
        }
    }
}