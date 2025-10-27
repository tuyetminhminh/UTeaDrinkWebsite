package net.codejava.utea.order.view;

import lombok.*;
import net.codejava.utea.order.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class CustomerOrderItemView {
    private Long orderItemId;
    private String orderCode;

    private Long productId;
    private String productName;
    private String productImageUrl;
    private String sizeLabel;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    private OrderStatus orderStatus;           // enum gốc
    private String orderStatusAsText;
    private LocalDateTime orderedAt;

    private Long reviewId;
    private Integer rating;
    private String reviewContent;

    // ======= convenience flags cho view (tránh biểu thức dài) =======
    public boolean isCancelableByCustomer() {
        return orderStatus == OrderStatus.NEW || orderStatus == OrderStatus.CONFIRMED;
    }
    public boolean isDeliveredNoReview() {
        return orderStatus == OrderStatus.DELIVERED && reviewId == null;
    }
    public boolean isDeliveredReviewed() {
        return orderStatus == OrderStatus.DELIVERED && reviewId != null;
    }
}
