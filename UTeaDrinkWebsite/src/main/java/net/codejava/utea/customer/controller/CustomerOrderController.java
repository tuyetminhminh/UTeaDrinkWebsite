package net.codejava.utea.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.customer.service.impl.MyOrderQueryService;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.order.view.CustomerOrderItemView;
import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;
import net.codejava.utea.shipping.entity.ShipAssignment;
import net.codejava.utea.shipping.repository.ShipAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/customer/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final MyOrderQueryService queryService;
    private final OrderRepository orderRepo;
    private final ReviewRepository reviewRepo;
    private final ShipAssignmentRepository shipAssignmentRepo;
    private final ObjectMapper objectMapper;

    private User currentUser(CustomUserDetails cud){
        if (cud == null) throw new RuntimeException("Chưa đăng nhập");
        User u = new User(); u.setId(cud.getId()); return u;
    }

    // Danh sách theo item (tách từng sản phẩm)
    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails cud,
                       @RequestParam(defaultValue = "ALL") String filter,
                       @RequestParam(defaultValue = "newest") String sort,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "7") int size,
                       Model model) {

        String f;
        switch (filter) {
            case "NEW","CONFIRMED","PREPARING","DELIVERING","DELIVERED","CANCELED" -> f = filter;
            default -> f = "ALL";
        }
        String s = "oldest".equalsIgnoreCase(sort) ? "oldest" : "newest";


        var u = currentUser(cud);
        Page<CustomerOrderItemView> p = queryService.listItems(u, f, s, page, size);

        model.addAttribute("items", p);
        model.addAttribute("filter", f);
        model.addAttribute("sort", s);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "customer/orders";
    }

    // Chi tiết đơn hàng
    @GetMapping("/{orderCode}")
    @Transactional(readOnly = true)
    public String detail(@AuthenticationPrincipal CustomUserDetails cud,
                         @PathVariable String orderCode,
                         Model model) {
        try {
            var u = currentUser(cud);
            Order order = orderRepo.findByOrderCode(orderCode).orElseThrow(
                    () -> new RuntimeException("Không tìm thấy đơn hàng")
            );
            
            // Kiểm tra quyền xem
            if (!order.getUser().getId().equals(u.getId())) {
                throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
            }
            
            // ===== FORCE LOAD TẤT CẢ LAZY ENTITIES =====
            
            // 1. Load Shop
            try {
                if (order.getShop() != null) {
                    order.getShop().getId();
                    order.getShop().getName();
                }
            } catch (Exception e) {
                // Shop bị xóa hoặc lỗi, ignore
            }
            
            // 2. Load Shipping Address
            try {
                if (order.getShippingAddress() != null) {
                    order.getShippingAddress().getId();
                    order.getShippingAddress().getReceiverName();
                    order.getShippingAddress().getPhone();
                    order.getShippingAddress().getLine();
                    order.getShippingAddress().getWard();
                    order.getShippingAddress().getDistrict();
                    order.getShippingAddress().getProvince();
                }
            } catch (Exception e) {
                // Address bị xóa hoặc lỗi, ignore
            }
            
            // 3. Load Order Items + Products + Variants + Images
            try {
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    for (var item : order.getItems()) {
                        try {
                            item.getId();
                            item.getQuantity();
                            item.getUnitPrice();
                            item.getLineTotal();
                            
                            // ===== LOAD TOPPING & NOTE =====
                            item.getToppingsJson();
                            item.getNote();
                            
                            // Load Product
                            if (item.getProduct() != null) {
                                item.getProduct().getId();
                                item.getProduct().getName();
                                
                                // Load Images
                                if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                                    item.getProduct().getImages().get(0).getId();
                                    item.getProduct().getImages().get(0).getUrl();
                                }
                            }
                            
                            // Load Variant
                            if (item.getVariant() != null) {
                                item.getVariant().getId();
                                item.getVariant().getSize();
                            }
                        } catch (Exception itemEx) {
                            // Item bị lỗi, skip
                        }
                    }
                }
            } catch (Exception e) {
                // Items bị lỗi, ignore
            }
            
            // 4. Load Payment
            try {
                if (order.getPayment() != null) {
                    order.getPayment().getId();
                    order.getPayment().getMethod();
                    order.getPayment().getStatus();
                }
            } catch (Exception e) {
                // Payment bị lỗi, ignore
            }
            
            // 5. Load Status Histories (QUAN TRỌNG: phải load từng phần tử)
            try {
                if (order.getStatusHistories() != null && !order.getStatusHistories().isEmpty()) {
                    for (var history : order.getStatusHistories()) {
                        try {
                            history.getId();
                            history.getStatus();
                            history.getChangedAt();
                            history.getNote();
                        } catch (Exception histEx) {
                            // History item bị lỗi, skip
                        }
                    }
                }
            } catch (Exception e) {
                // StatusHistories bị lỗi, ignore
            }
            
            // ===== LẤY THÔNG TIN GIAO HÀNG =====
            
            Optional<ShipAssignment> shipOpt = Optional.empty();
            try {
                shipOpt = shipAssignmentRepo.findByOrderId(order.getId());
                
                // Force load shipper info nếu có
                if (shipOpt.isPresent()) {
                    ShipAssignment ship = shipOpt.get();
                    ship.getId();
                    ship.getStatus();
                    ship.getNote();
                    ship.getAssignedAt();
                    ship.getPickedUpAt();
                    ship.getDeliveredAt();
                    
                    if (ship.getShipper() != null) {
                        ship.getShipper().getId();
                        ship.getShipper().getFullName();
                    }
                }
            } catch (Exception e) {
                // ShipAssignment bị lỗi, ignore
            }
            
            // ===== PARSE THÔNG TIN HỦY ĐƠN VÀ GIAO HÀNG =====
            
            String cancelReason = null;
            String canceledBy = null;
            String deliveryNote = null;
            String proofImageUrl = null;
            String failureNote = null;
            
            if (order.getStatus() == OrderStatus.CANCELED) {
                // Xác định ai hủy và lý do
                if (shipOpt.isPresent() && "FAILED".equals(shipOpt.get().getStatus())) {
                    // Shipper báo không giao được
                    canceledBy = "SHIPPER";
                    try {
                        String note = shipOpt.get().getNote();
                        if (note != null && note.trim().startsWith("{")) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> noteData = objectMapper.readValue(note, Map.class);
                            String reasonCode = (String) noteData.getOrDefault("failureReason", "");
                            cancelReason = translateFailureReason(reasonCode);
                            failureNote = (String) noteData.getOrDefault("note", "");
                        } else {
                            cancelReason = "Shipper không giao được hàng";
                        }
                    } catch (Exception e) {
                        cancelReason = "Shipper không giao được hàng";
                    }
                } else if (order.getStatusHistories() != null && !order.getStatusHistories().isEmpty()) {
                    // ✅ FIX: Có bất kỳ OrderStatusHistory nào → Manager đã can thiệp và hủy đơn
                    // (Customer hủy thì KHÔNG tạo history, chỉ Manager tạo history khi thay đổi status)
                    canceledBy = "MANAGER";
                    cancelReason = "Cửa hàng hủy đơn";
                } else {
                    // Không có ShipAssignment và không có StatusHistory → Khách hàng hủy
                    canceledBy = "CUSTOMER";
                    cancelReason = "Khách hàng hủy đơn";
                }
            } else if (order.getStatus() == OrderStatus.DELIVERED && shipOpt.isPresent()) {
                // Parse thông tin giao hàng thành công
                try {
                    String note = shipOpt.get().getNote();
                    if (note != null && note.trim().startsWith("{")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> noteData = objectMapper.readValue(note, Map.class);
                        deliveryNote = (String) noteData.getOrDefault("deliveryNote", "");
                        proofImageUrl = (String) noteData.getOrDefault("proofImage", "");
                    } else {
                        deliveryNote = note;
                    }
                } catch (Exception e) {
                    deliveryNote = shipOpt.map(ShipAssignment::getNote).orElse(null);
                }
            }
            
            // ===== ĐƯA DỮ LIỆU VÀO MODEL =====
            
            model.addAttribute("order", order);
            model.addAttribute("shipAssignment", shipOpt.orElse(null));
            model.addAttribute("cancelReason", cancelReason);
            model.addAttribute("canceledBy", canceledBy);
            model.addAttribute("failureNote", failureNote);
            model.addAttribute("deliveryNote", deliveryNote);
            model.addAttribute("proofImageUrl", proofImageUrl);
            
            return "customer/order-detail";
            
        } catch (Exception e) {
            // Log lỗi để debug
            e.printStackTrace();
            model.addAttribute("errorMessage", "Không thể tải thông tin đơn hàng: " + e.getMessage());
            return "error/error";
        }
    }
    
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

    // Hủy đơn nếu là chủ đơn và còn NEW (chỉ NEW mới được hủy)
    @PostMapping("/{orderCode}/cancel")
    public String cancel(@AuthenticationPrincipal CustomUserDetails cud,
                         @PathVariable String orderCode) {

        var u = currentUser(cud);
        Order o = orderRepo.findByOrderCode(orderCode).orElseThrow();
        if (!o.getUser().getId().equals(u.getId())) {
            throw new RuntimeException("Không có quyền hủy đơn này.");
        }

        // Chỉ cho phép hủy khi trạng thái NEW
        if (o.getStatus() == OrderStatus.NEW) {
            o.setStatus(OrderStatus.CANCELED);
            orderRepo.save(o);
        } else {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng ở trạng thái Mới.");
        }
        return "redirect:/customer/orders";
    }

    // Tạo/cập nhật review theo từng item
    @PostMapping("/review")
    public String review(@AuthenticationPrincipal CustomUserDetails cud,
                         @RequestParam Long orderItemId,
                         @RequestParam Long productId,
                         @RequestParam String orderCode,
                         @RequestParam Integer rating,
                         @RequestParam String content) {

        var u = currentUser(cud);

        // upsert: tìm review theo (userId + orderItemId)
        Review r = reviewRepo.findAll().stream()
                .filter(x -> x.getOrderItemId()!=null && x.getOrderItemId().equals(orderItemId)
                        && x.getUser()!=null && x.getUser().getId().equals(u.getId()))
                .findFirst().orElse(null);

        if (r == null) {
            var product = new net.codejava.utea.catalog.entity.Product();
            product.setId(productId);

            r = Review.builder()
                    .user(u)
                    .product(product)
                    .orderItemId(orderItemId)
                    .rating(rating)
                    .content(content)
                    // tuỳ chính sách: PENDING / APPROVED
                    .status(ReviewStatus.APPROVED)
                    .build();
        } else {
            r.setRating(rating);
            r.setContent(content);
            r.setStatus(ReviewStatus.APPROVED);
        }
        reviewRepo.save(r);
        return "redirect:/customer/orders";
    }
}
