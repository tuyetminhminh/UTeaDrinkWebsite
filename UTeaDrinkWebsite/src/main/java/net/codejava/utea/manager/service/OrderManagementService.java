package net.codejava.utea.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.dto.OrderItemDTO;
import net.codejava.utea.manager.dto.OrderManagementDTO;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.OrderItem;
import net.codejava.utea.order.entity.OrderStatusHistory;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.order.repository.OrderStatusHistoryRepository;
import net.codejava.utea.shipping.entity.ShipAssignment;
import net.codejava.utea.shipping.repository.ShipAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderManagementService {

    private final OrderRepository orderRepo;
    private final OrderStatusHistoryRepository statusHistoryRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final ShipAssignmentRepository shipAssignmentRepo;
    private final ObjectMapper objectMapper;

    // ==================== ORDER MANAGEMENT ====================

    /**
     * Lấy tất cả đơn hàng của shop (phân trang)
     */
    @Transactional(readOnly = true)
    public Page<OrderManagementDTO> getAllOrders(Long managerId, Pageable pageable) {
        Shop shop = getShopByManagerId(managerId);

        // Lấy TẤT CẢ orders của shop này (không phân trang trước)
        List<Order> allShopOrders = orderRepo.findAll().stream()
                .filter(order -> order.getShop() != null && order.getShop().getId().equals(shop.getId()))
                .collect(Collectors.toList());

        // Áp dụng pagination TRÊN kết quả đã filter
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allShopOrders.size());
        List<Order> pageContent = allShopOrders.subList(start, end);

        // Convert to Page
        return new org.springframework.data.domain.PageImpl<>(
                pageContent.stream().map(this::convertToDTO).collect(Collectors.toList()),
                pageable,
                allShopOrders.size()
        );
    }

    /**
     * Lấy đơn hàng theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<OrderManagementDTO> getOrdersByStatus(Long managerId, OrderStatus status) {
        Shop shop = getShopByManagerId(managerId);

        List<Order> orders = orderRepo.findAll();

        return orders.stream()
                .filter(order -> order.getShop() != null
                        && order.getShop().getId().equals(shop.getId())
                        && order.getStatus() == status)
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt())) // Sort DESC by createdAt
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy đơn hàng NEW mới nhất (cho voice notification)
     */
    @Transactional(readOnly = true)
    public OrderManagementDTO getLatestNewOrder(Long managerId) {
        Shop shop = getShopByManagerId(managerId);

        return orderRepo.findAll().stream()
                .filter(order -> order.getShop() != null
                        && order.getShop().getId().equals(shop.getId())
                        && order.getStatus() == OrderStatus.NEW)
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt())) // Mới nhất trước
                .findFirst()
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    @Transactional(readOnly = true)
    public OrderManagementDTO getOrderById(Long managerId, Long orderId) {
        Shop shop = getShopByManagerId(managerId);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Kiểm tra đơn hàng có thuộc shop của manager không
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền truy cập đơn hàng này");
        }

        return convertToDTO(order);
    }

    /**
     * Xác nhận đơn hàng (NEW -> CONFIRMED)
     */
    @Transactional
    public OrderManagementDTO confirmOrder(Long managerId, Long orderId) {
        return updateOrderStatus(managerId, orderId, OrderStatus.CONFIRMED, "Đơn hàng đã được xác nhận");
    }

    /**
     * Chuẩn bị đơn hàng (CONFIRMED -> PREPARING)
     */
    @Transactional
    public OrderManagementDTO prepareOrder(Long managerId, Long orderId) {
        return updateOrderStatus(managerId, orderId, OrderStatus.PREPARING, "Đang chuẩn bị đơn hàng");
    }

    /**
     * Bắt đầu giao hàng (PREPARING -> DELIVERING)
     */
    @Transactional
    public OrderManagementDTO deliverOrder(Long managerId, Long orderId) {
        return updateOrderStatus(managerId, orderId, OrderStatus.DELIVERING, "Đơn hàng đang được giao");
    }

    /**
     * Hoàn thành đơn hàng (DELIVERING -> DELIVERED)
     */
    @Transactional
    public OrderManagementDTO completeOrder(Long managerId, Long orderId) {
        return updateOrderStatus(managerId, orderId, OrderStatus.DELIVERED, "Đơn hàng đã được giao thành công");
    }

    /**
     * Hủy đơn hàng
     */
    @Transactional
    public OrderManagementDTO cancelOrder(Long managerId, Long orderId, String reason) {
        Shop shop = getShopByManagerId(managerId);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Kiểm tra đơn hàng có thuộc shop của manager không
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền hủy đơn hàng này");
        }

        // Chỉ cho phép hủy đơn ở trạng thái NEW hoặc CONFIRMED
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELED);
        order = orderRepo.save(order);

        // Lưu lịch sử
        addStatusHistory(order, OrderStatus.CANCELED, reason != null ? reason : "Đơn hàng đã bị hủy");

        return convertToDTO(order);
    }

    /**
     * Xử lý trả hàng
     */
    @Transactional
    public OrderManagementDTO returnOrder(Long managerId, Long orderId, String reason) {
        return updateOrderStatus(managerId, orderId, OrderStatus.RETURNED,
                reason != null ? reason : "Đơn hàng được trả lại");
    }

    /**
     * Xử lý hoàn tiền
     */
    @Transactional
    public OrderManagementDTO refundOrder(Long managerId, Long orderId, String reason) {
        return updateOrderStatus(managerId, orderId, OrderStatus.REFUNDED,
                reason != null ? reason : "Đơn hàng đã được hoàn tiền");
    }

    // ==================== HELPER METHODS ====================

    private Shop getShopByManagerId(Long managerId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));
        return shopManager.getShop();
    }

    @Transactional
    private OrderManagementDTO updateOrderStatus(Long managerId, Long orderId, OrderStatus newStatus, String note) {
        Shop shop = getShopByManagerId(managerId);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Kiểm tra đơn hàng có thuộc shop của manager không
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền cập nhật đơn hàng này");
        }

        order.setStatus(newStatus);
        order = orderRepo.save(order);

        // Lưu lịch sử thay đổi trạng thái
        addStatusHistory(order, newStatus, note);

        return convertToDTO(order);
    }

    private void addStatusHistory(Order order, OrderStatus status, String note) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .note(note)
                .changedAt(LocalDateTime.now())
                .build();
        statusHistoryRepo.save(history);
    }

    private OrderManagementDTO convertToDTO(Order order) {
        ShipAssignment shipAssignment = shipAssignmentRepo.findByOrderId(order.getId()).orElse(null);

        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());

        int totalItemCount = order.getItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        // Parse delivery note and proof image from ShipAssignment
        String deliveryNote = "";
        String proofImageUrl = "";
        if (shipAssignment != null && shipAssignment.getNote() != null) {
            try {
                if (shipAssignment.getNote().startsWith("{")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> noteData = objectMapper.readValue(shipAssignment.getNote(), Map.class);
                    deliveryNote = (String) noteData.getOrDefault("deliveryNote", "");
                    proofImageUrl = (String) noteData.getOrDefault("proofImage", "");
                } else {
                    deliveryNote = shipAssignment.getNote();
                }
            } catch (Exception e) {
                deliveryNote = shipAssignment.getNote();
            }
        }

        return OrderManagementDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUser().getId())
                .customerName(order.getUser().getFullName())
                .customerPhone(order.getShippingAddress() != null ? order.getShippingAddress().getPhone() : "")
                .shopId(order.getShop().getId())
                .shopName(order.getShop().getName())
                .status(order.getStatus().name())
                .shippingAddress(formatAddress(order))
                .receiverName(order.getShippingAddress() != null ? order.getShippingAddress().getReceiverName() : "")
                .receiverPhone(order.getShippingAddress() != null ? order.getShippingAddress().getPhone() : "")
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .voucherCode(order.getVoucherCode())
                .paymentMethod(order.getPayment() != null ? order.getPayment().getMethod().name() : "")
                .paymentStatus(order.getPayment() != null ? order.getPayment().getStatus().name() : "")
                .items(itemDTOs)
                .itemCount(totalItemCount)
                .shipperId(shipAssignment != null ? shipAssignment.getShipper().getId() : null)
                .shipperName(shipAssignment != null ? shipAssignment.getShipper().getFullName() : null)
                .deliveryNote(deliveryNote)
                .proofImageUrl(proofImageUrl)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private String formatAddress(Order order) {
        if (order.getShippingAddress() == null) return "";

        var addr = order.getShippingAddress();
        return String.format("%s, %s, %s, %s",
                addr.getLine(), addr.getWard(), addr.getDistrict(), addr.getProvince());
    }

    private OrderItemDTO convertItemToDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImage(item.getProduct().getMainImageUrl())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantSize(item.getVariant() != null ? item.getVariant().getSize().name() : "")
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .toppingsJson(item.getToppingsJson())
                .note(item.getNote())
                .build();
    }
}

