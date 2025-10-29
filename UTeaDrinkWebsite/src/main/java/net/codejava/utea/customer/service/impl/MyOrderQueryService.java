package net.codejava.utea.customer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.OrderItem;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.order.view.CustomerOrderItemView;
import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.repository.ReviewRepository;
import net.codejava.utea.shipping.entity.ShipAssignment;
import net.codejava.utea.shipping.repository.ShipAssignmentRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyOrderQueryService {

    private final OrderRepository orderRepo;
    private final ReviewRepository reviewRepo;
    private final ShipAssignmentRepository shipAssignmentRepo;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<CustomerOrderItemView> listItems(User user,
                                                 String filter,   // NEW | SHIPPING | DELIVERED_NO_REVIEW | REVIEWED | ALL
                                                 String sort,     // newest | oldest
                                                 int page, int size) {

        // 1) Lấy orders của user (mặc định repo trả desc theo createdAt)
        List<Order> orders = Optional.ofNullable(orderRepo.findByUserOrderByCreatedAtDesc(user))
                .orElseGet(ArrayList::new);

        // 2) Lấy tất cả review của user -> map theo orderItemId
        Map<Long, Review> reviewByOi = reviewRepo.findAll().stream()
                .filter(r -> r.getOrderItemId() != null
                        && r.getUser() != null
                        && Objects.equals(r.getUser().getId(), user.getId()))
                .collect(Collectors.toMap(Review::getOrderItemId, r -> r, (a, b) -> a));

        // 3) Build view list (chú ý fallback null an toàn)
        List<CustomerOrderItemView> all = new ArrayList<>();
        for (Order o : orders) {
            var orderedAt = Optional.ofNullable(o.getCreatedAt()).orElse(LocalDateTime.MIN);

            List<OrderItem> items = Optional.ofNullable(o.getItems()).orElse(List.of());
            for (OrderItem oi : items) {
                var p = oi.getProduct();
                var v = oi.getVariant();

                // Hình ảnh an toàn
                String imageUrl = "/images/no-image.png";
                try {
                    if (p != null && p.getImages() != null && !p.getImages().isEmpty()
                            && p.getImages().get(0) != null && p.getImages().get(0).getUrl() != null) {
                        imageUrl = p.getImages().get(0).getUrl();
                    }
                } catch (Exception ignore) { /* fallback giữ no-image */ }

                // Giá trị tiền an toàn
                BigDecimal unitPrice = Optional.ofNullable(oi.getUnitPrice()).orElse(BigDecimal.ZERO);
                BigDecimal lineTotal = Optional.ofNullable(oi.getLineTotal()).orElse(
                        unitPrice.multiply(BigDecimal.valueOf(Optional.ofNullable(oi.getQuantity()).orElse(0)))
                );

                Review rv = reviewByOi.get(oi.getId());

                // Xác định lý do hủy và ai hủy (nếu đơn bị CANCELED)
                String cancelReason = null;
                String canceledBy = null;
                if (o.getStatus() == net.codejava.utea.order.entity.enums.OrderStatus.CANCELED) {
                    // Kiểm tra xem có ShipAssignment không
                    Optional<ShipAssignment> shipOpt = shipAssignmentRepo.findByOrderId(o.getId());
                    
                    if (shipOpt.isPresent() && "FAILED".equals(shipOpt.get().getStatus())) {
                        // 1. Shipper báo không giao được
                        canceledBy = "SHIPPER";
                        try {
                            if (shipOpt.get().getNote() != null && shipOpt.get().getNote().startsWith("{")) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> noteData = objectMapper.readValue(shipOpt.get().getNote(), Map.class);
                                String reasonCode = (String) noteData.getOrDefault("failureReason", "");
                                cancelReason = translateFailureReason(reasonCode);
                            } else {
                                cancelReason = "Shipper không giao được hàng";
                            }
                        } catch (Exception e) {
                            cancelReason = "Shipper không giao được hàng";
                        }
                    } else if (o.getStatusHistories() != null && !o.getStatusHistories().isEmpty()) {
                        // ✅ FIX: Có bất kỳ OrderStatusHistory nào → Manager đã can thiệp và hủy đơn
                        // (Customer hủy thì KHÔNG tạo history, chỉ Manager tạo history khi thay đổi status)
                        canceledBy = "MANAGER";
                        cancelReason = "Cửa hàng hủy đơn";
                    } else {
                        // Không có ShipAssignment và không có StatusHistory → Khách hàng hủy
                        canceledBy = "CUSTOMER";
                        cancelReason = "Khách hàng hủy đơn";
                    }
                }

                all.add(CustomerOrderItemView.builder()
                        .orderItemId(oi.getId())
                        .orderCode(Optional.ofNullable(o.getOrderCode()).orElse(""))
                        .productId(p != null ? p.getId() : null)
                        .productName(p != null ? p.getName() : "(Sản phẩm)")
                        .productImageUrl(imageUrl)
                        .sizeLabel(v != null && v.getSize() != null ? String.valueOf(v.getSize()) : null)
                        .quantity(Optional.ofNullable(oi.getQuantity()).orElse(0))
                        .unitPrice(unitPrice)
                        .lineTotal(lineTotal)
                        .orderStatus(o.getStatus())
                        .orderedAt(orderedAt)
                        // Thông tin đơn hàng
                        .orderTotal(o.getTotal())
                        .orderSubtotal(o.getSubtotal())
                        .orderShippingFee(o.getShippingFee())
                        .orderDiscount(o.getDiscount())
                        .reviewId(rv == null ? null : rv.getId())
                        .rating(rv == null ? null : rv.getRating())
                        .reviewContent(rv == null ? null : rv.getContent())
                        .cancelReason(cancelReason)
                        .canceledBy(canceledBy)
                        .build());
            }
        }

        // 4) lọc theo filter (đúng theo trạng thái đơn)
        String stateFilter = Optional.ofNullable(filter).orElse("ALL");
        List<CustomerOrderItemView> filtered = all.stream().filter(v -> {
            switch (stateFilter) {
                case "NEW":         return v.getOrderStatus() == net.codejava.utea.order.entity.enums.OrderStatus.NEW;
                case "CONFIRMED":   return v.getOrderStatus() == net.codejava.utea.order.entity.enums.OrderStatus.CONFIRMED;
                case "PREPARING":   return v.getOrderStatus() == net.codejava.utea.order.entity.enums.OrderStatus.PREPARING;
                case "DELIVERING":  return v.getOrderStatus() == net.codejava.utea.order.entity.enums.OrderStatus.DELIVERING;
                case "DELIVERED":   return v.getOrderStatus() == net.codejava.utea.order.entity.enums.OrderStatus.DELIVERED;
                case "CANCELED":    return v.getOrderStatus() == net.codejava.utea.order.entity.enums.OrderStatus.CANCELED;
                case "ALL":
                default:            return true;
            }
        }).collect(Collectors.toList());

        // 5) Sort an toàn với null (fallback LocalDateTime.MIN) + tie-breaker theo orderCode
        Comparator<CustomerOrderItemView> byTime =
                Comparator.comparing(v -> Optional.ofNullable(v.getOrderedAt()).orElse(LocalDateTime.MIN));
        if (!"oldest".equalsIgnoreCase(sort)) {
            byTime = byTime.reversed(); // mặc định newest
        }
        Comparator<CustomerOrderItemView> byCode =
                Comparator.comparing(v -> Optional.ofNullable(v.getOrderCode()).orElse(""));
        filtered.sort(byTime.thenComparing(byCode));

        // 6) Paginate thủ công
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<CustomerOrderItemView> pageList = filtered.subList(from, to);

        return new PageImpl<>(pageList, PageRequest.of(page, size), filtered.size());
    }

    /**
     * Chuyển đổi mã lý do hủy sang tiếng Việt
     */
    private String translateFailureReason(String reasonCode) {
        if (reasonCode == null || reasonCode.isEmpty()) {
            return "Không rõ lý do";
        }
        return switch (reasonCode) {
            case "CUSTOMER_NOT_REACHABLE" -> "Không liên lạc được với khách";
            case "CUSTOMER_REFUSED" -> "Khách từ chối nhận hàng";
            case "WRONG_ADDRESS" -> "Địa chỉ sai";
            case "CUSTOMER_RESCHEDULED" -> "Khách hẹn lại";
            case "OTHER" -> "Lý do khác";
            default -> reasonCode;
        };
    }
}
