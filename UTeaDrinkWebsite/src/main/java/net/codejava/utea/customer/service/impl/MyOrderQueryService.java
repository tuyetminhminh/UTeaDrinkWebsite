package net.codejava.utea.customer.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.OrderItem;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.order.view.CustomerOrderItemView;
import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.repository.ReviewRepository;
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
                        .reviewId(rv == null ? null : rv.getId())
                        .rating(rv == null ? null : rv.getRating())
                        .reviewContent(rv == null ? null : rv.getContent())
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
}
