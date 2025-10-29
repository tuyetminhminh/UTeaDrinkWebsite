package net.codejava.utea.shipper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.payment.entity.enums.PaymentStatus;
import net.codejava.utea.shipper.dto.AvailableOrderDTO;
import net.codejava.utea.shipper.dto.MyOrderDTO;
import net.codejava.utea.shipper.dto.ShipperStatsDTO;
import net.codejava.utea.shipping.entity.ShipAssignment;
import net.codejava.utea.shipping.repository.ShipAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShipperOrderService {

    private final OrderRepository orderRepo;
    private final ShipAssignmentRepository shipAssignmentRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== AVAILABLE ORDERS (PREPARING) ====================
    
    /**
     * Lấy danh sách đơn hàng khả dụng (PREPARING) chưa có shipper
     */
    @Transactional(readOnly = true)
    public List<AvailableOrderDTO> getAvailableOrders() {
        List<Order> preparingOrders = orderRepo.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.PREPARING)
                .filter(order -> shipAssignmentRepo.findByOrderId(order.getId()).isEmpty())
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return preparingOrders.stream()
                .map(this::convertToAvailableDTO)
                .collect(Collectors.toList());
    }

    // ==================== MY ORDERS (Đơn đã nhận) ====================
    
    /**
     * Lấy danh sách đơn đang giao
     */
    @Transactional(readOnly = true)
    public List<MyOrderDTO> getDeliveringOrders(Long shipperId) {
        return shipAssignmentRepo.findByShipperIdAndStatus(shipperId, "DELIVERING").stream()
                .map(this::convertToMyOrderDTO)
                .sorted(Comparator.comparing(MyOrderDTO::getAssignedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách đơn đã giao
     */
    @Transactional(readOnly = true)
    public List<MyOrderDTO> getDeliveredOrders(Long shipperId, int limit) {
        return shipAssignmentRepo.findByShipperIdAndStatus(shipperId, "DELIVERED").stream()
                .map(this::convertToMyOrderDTO)
                .sorted(Comparator.comparing(MyOrderDTO::getDeliveredAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách đơn đã nhận nhưng chưa lấy hàng (status=ASSIGNED)
     */
    @Transactional(readOnly = true)
    public List<MyOrderDTO> getAssignedOrders(Long shipperId) {
        return shipAssignmentRepo.findByShipperId(shipperId).stream()
                .filter(a -> "ASSIGNED".equals(a.getStatus()))
                .map(this::convertToMyOrderDTO)
                .sorted(Comparator.comparing(MyOrderDTO::getAssignedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách đơn đã hủy (bao gồm đơn shipper báo cáo không giao được)
     */
    @Transactional(readOnly = true)
    public List<MyOrderDTO> getCanceledOrders(Long shipperId, int limit) {
        return shipAssignmentRepo.findByShipperId(shipperId).stream()
                .filter(a -> "FAILED".equals(a.getStatus()) || 
                            (a.getOrder() != null && a.getOrder().getStatus() == OrderStatus.CANCELED))
                .map(this::convertToMyOrderDTO)
                .sorted(Comparator.comparing(MyOrderDTO::getAssignedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== ACTIONS ====================
    
    /**
     * Shipper nhận đơn (chưa lấy hàng)
     */
    @Transactional
    public MyOrderDTO acceptOrder(Long shipperId, Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ giao");
        }

        // Kiểm tra đã có shipper chưa
        if (shipAssignmentRepo.findByOrderId(orderId).isPresent()) {
            throw new RuntimeException("Đơn hàng đã được shipper khác nhận");
        }

        // Kiểm tra shipper đã có đơn chưa hoàn thành hay chưa
        List<ShipAssignment> uncompleted = shipAssignmentRepo.findByShipperId(shipperId).stream()
                .filter(a -> "ASSIGNED".equals(a.getStatus()) || "DELIVERING".equals(a.getStatus()))
                .collect(Collectors.toList());
        
        if (!uncompleted.isEmpty()) {
            throw new RuntimeException("Bạn vẫn còn đơn hàng chưa hoàn thành. Vui lòng giao xong đơn hiện tại trước khi nhận đơn mới!");
        }

        User shipper = new User();
        shipper.setId(shipperId);

        // Tạo assignment với status ASSIGNED (chưa lấy hàng)
        ShipAssignment assignment = ShipAssignment.builder()
                .order(order)
                .shipper(shipper)
                .status("ASSIGNED") // Đã nhận nhưng chưa lấy hàng
                .assignedAt(LocalDateTime.now())
                .build();

        shipAssignmentRepo.save(assignment);

        // Order vẫn giữ PREPARING (chưa lấy hàng)
        // Sẽ chuyển sang DELIVERING khi confirmPickup()

        return convertToMyOrderDTO(assignment);
    }

    /**
     * Xác nhận đã lấy hàng (ASSIGNED → DELIVERING)
     */
    @Transactional
    public void confirmPickup(Long shipperId, Long orderId) {
        ShipAssignment assignment = shipAssignmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin giao hàng"));

        if (!assignment.getShipper().getId().equals(shipperId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật đơn hàng này");
        }

        if (!"ASSIGNED".equals(assignment.getStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ lấy hàng");
        }

        // Cập nhật thời gian lấy hàng
        assignment.setPickedUpAt(LocalDateTime.now());
        assignment.setStatus("DELIVERING"); // Chuyển sang đang giao
        shipAssignmentRepo.save(assignment);

        // Cập nhật trạng thái Order
        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.DELIVERING);
        orderRepo.save(order);
    }

    /**
     * Hoàn thành giao hàng (với ghi chú và ảnh)
     */
    @Transactional
    public void completeDelivery(Long shipperId, Long orderId, String deliveryNote, String proofImageUrl) {
        ShipAssignment assignment = shipAssignmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin giao hàng"));

        if (!assignment.getShipper().getId().equals(shipperId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật đơn hàng này");
        }

        // Tạo JSON note
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("deliveryNote", deliveryNote != null ? deliveryNote : "Đã giao hàng thành công");
        if (proofImageUrl != null && !proofImageUrl.isEmpty()) {
            noteData.put("proofImage", proofImageUrl);
        }
        noteData.put("completedAt", LocalDateTime.now().toString());

        try {
            String noteJson = objectMapper.writeValueAsString(noteData);
            assignment.setNote(noteJson);
        } catch (JsonProcessingException e) {
            assignment.setNote(deliveryNote);
        }

        assignment.setDeliveredAt(LocalDateTime.now());
        assignment.setStatus("DELIVERED");
        shipAssignmentRepo.save(assignment);

        // Cập nhật trạng thái đơn hàng
        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
        orderRepo.save(order);
    }

    /**
     * Báo cáo không giao được hàng (Khách từ chối, không liên hệ được, sai địa chỉ...)
     * → Chuyển đơn về CANCELED và lưu lý do
     */
    @Transactional
    public void reportFailedDelivery(Long shipperId, Long orderId, String failureReason, String note) {
        ShipAssignment assignment = shipAssignmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin giao hàng"));

        if (!assignment.getShipper().getId().equals(shipperId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật đơn hàng này");
        }

        if (!"DELIVERING".equals(assignment.getStatus())) {
            throw new RuntimeException("Chỉ có thể báo cáo thất bại khi đang giao hàng");
        }

        // Tạo JSON note với thông tin thất bại
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("failureReason", failureReason != null ? failureReason : "UNKNOWN");
        noteData.put("note", note != null ? note : "Không giao được hàng");
        noteData.put("failedAt", LocalDateTime.now().toString());
        noteData.put("reportedBy", "SHIPPER");

        try {
            String noteJson = objectMapper.writeValueAsString(noteData);
            assignment.setNote(noteJson);
        } catch (JsonProcessingException e) {
            assignment.setNote("FAILED: " + failureReason + " - " + note);
        }

        assignment.setStatus("FAILED"); // Đánh dấu assignment là FAILED
        shipAssignmentRepo.save(assignment);

        // Cập nhật đơn hàng về CANCELED
        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.CANCELED);
        orderRepo.save(order);
    }

    // ==================== STATISTICS ====================
    
    /**
     * Thống kê shipper
     */
    @Transactional(readOnly = true)
    public ShipperStatsDTO getShipperStats(Long shipperId) {
        List<ShipAssignment> allAssignments = shipAssignmentRepo.findByShipperId(shipperId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);

        // Hôm nay
        List<ShipAssignment> todayDelivered = allAssignments.stream()
                .filter(a -> a.getDeliveredAt() != null && a.getDeliveredAt().isAfter(startOfToday))
                .collect(Collectors.toList());

        // Tuần này
        List<ShipAssignment> weekDelivered = allAssignments.stream()
                .filter(a -> a.getDeliveredAt() != null && a.getDeliveredAt().isAfter(startOfWeek))
                .collect(Collectors.toList());

        // Tháng này
        List<ShipAssignment> monthDelivered = allAssignments.stream()
                .filter(a -> a.getDeliveredAt() != null && a.getDeliveredAt().isAfter(startOfMonth))
                .collect(Collectors.toList());

        // Tổng
        List<ShipAssignment> completed = allAssignments.stream()
                .filter(a -> "DELIVERED".equals(a.getStatus()))
                .collect(Collectors.toList());

        return ShipperStatsDTO.builder()
                .todayOrders(todayDelivered.size())
                .todayEarnings(calculateEarnings(todayDelivered))
                .weekOrders(weekDelivered.size())
                .weekEarnings(calculateEarnings(weekDelivered))
                .monthOrders(monthDelivered.size())
                .monthEarnings(calculateEarnings(monthDelivered))
                .totalOrders(allAssignments.size())
                .totalEarnings(calculateEarnings(allAssignments))
                .deliveringCount((int) allAssignments.stream().filter(a -> "DELIVERING".equals(a.getStatus())).count())
                .availableCount((int) orderRepo.findAll().stream()
                        .filter(o -> o.getStatus() == OrderStatus.PREPARING)
                        .filter(o -> shipAssignmentRepo.findByOrderId(o.getId()).isEmpty())
                        .count())
                .completedOrders(completed.size())
                .canceledOrders(0) // TODO: implement if needed
                .build();
    }

    // ==================== HELPER METHODS ====================
    
    private AvailableOrderDTO convertToAvailableDTO(Order order) {
        int itemCount = order.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
        
        // Tính tiền thu hộ: nếu đã thanh toán online (payment.status = PAID) thì = 0, không thì = total
        boolean isPaidOnline = order.getPayment() != null 
                && order.getPayment().getStatus() == PaymentStatus.PAID;
        BigDecimal collectAmount = isPaidOnline ? BigDecimal.ZERO : order.getTotal();
        
        // orderStatus để hiển thị trên UI: nếu đã thanh toán online thì show "PAID", không thì show status hiện tại
        String displayStatus = isPaidOnline ? "PAID" : order.getStatus().name();

        return AvailableOrderDTO.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .shopId(order.getShop().getId())
                .shopName(order.getShop().getName())
                .shopAddress(order.getShop().getAddress())
                .customerName(order.getUser().getFullName())
                .customerPhone(order.getShippingAddress() != null ? order.getShippingAddress().getPhone() : "")
                .deliveryAddress(formatShortAddress(order))
                .district(order.getShippingAddress() != null ? order.getShippingAddress().getDistrict() : "")
                .ward(order.getShippingAddress() != null ? order.getShippingAddress().getWard() : "")
                .itemCount(itemCount)
                .total(order.getTotal())
                .shippingFee(order.getShippingFee())
                .collectAmount(collectAmount)
                .paymentMethod(order.getPayment() != null ? order.getPayment().getMethod().name() : "COD")
                .orderStatus(displayStatus)
                .createdAt(order.getCreatedAt())
                .timeAgo(calculateTimeAgo(order.getCreatedAt()))
                .estimatedDistance("~2-3 km") // TODO: calculate actual distance
                .build();
    }

    private MyOrderDTO convertToMyOrderDTO(ShipAssignment assignment) {
        Order order = assignment.getOrder();
        int itemCount = order.getItems().stream().mapToInt(item -> item.getQuantity()).sum();

        // Parse note JSON
        String deliveryNote = "";
        String proofImageUrl = "";
        String failureReason = "";
        String failureNote = "";
        
        try {
            if (assignment.getNote() != null && assignment.getNote().startsWith("{")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> noteData = objectMapper.readValue(assignment.getNote(), Map.class);
                
                // Parse delivery success info
                deliveryNote = (String) noteData.getOrDefault("deliveryNote", "");
                proofImageUrl = (String) noteData.getOrDefault("proofImage", "");
                
                // Parse failure info (if assignment status is FAILED)
                if ("FAILED".equals(assignment.getStatus())) {
                    failureReason = (String) noteData.getOrDefault("failureReason", "");
                    failureNote = (String) noteData.getOrDefault("note", "");
                }
            } else {
                deliveryNote = assignment.getNote();
            }
        } catch (Exception e) {
            deliveryNote = assignment.getNote();
        }
        
        // Tính tiền thu hộ: nếu đã thanh toán online (payment.status = PAID) thì = 0, không thì = total
        boolean isPaidOnline = order.getPayment() != null 
                && order.getPayment().getStatus() == PaymentStatus.PAID;
        BigDecimal collectAmount = isPaidOnline ? BigDecimal.ZERO : order.getTotal();
        
        // orderStatus để hiển thị trên UI: nếu đã thanh toán online thì show "PAID", không thì show status hiện tại
        String displayStatus = isPaidOnline ? "PAID" : order.getStatus().name();

        return MyOrderDTO.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .status(assignment.getStatus())
                .shopName(order.getShop().getName())
                .shopPhone(order.getShop().getPhone())
                .shopAddress(order.getShop().getAddress())
                .customerName(order.getUser().getFullName())
                .customerPhone(order.getShippingAddress() != null ? order.getShippingAddress().getPhone() : "")
                .deliveryAddress(formatShortAddress(order))
                .fullAddress(formatFullAddress(order))
                .itemCount(itemCount)
                .total(order.getTotal())
                .shippingFee(order.getShippingFee())
                .collectAmount(collectAmount)
                .orderStatus(displayStatus)
                .assignedAt(assignment.getAssignedAt())
                .pickedUpAt(assignment.getPickedUpAt())
                .deliveredAt(assignment.getDeliveredAt())
                .deliveryNote(deliveryNote)
                .proofImageUrl(proofImageUrl)
                .failureReason(failureReason)
                .failureNote(failureNote)
                .build();
    }

    private String formatShortAddress(Order order) {
        if (order.getShippingAddress() == null) return "";
        return order.getShippingAddress().getLine();
    }

    private String formatFullAddress(Order order) {
        if (order.getShippingAddress() == null) return "";
        var addr = order.getShippingAddress();
        return String.format("%s, %s, %s, %s", 
                addr.getLine(), addr.getWard(), addr.getDistrict(), addr.getProvince());
    }

    private String calculateTimeAgo(LocalDateTime time) {
        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        if (minutes < 60) return minutes + " phút trước";
        
        long hours = ChronoUnit.HOURS.between(time, LocalDateTime.now());
        if (hours < 24) return hours + " giờ trước";
        
        long days = ChronoUnit.DAYS.between(time, LocalDateTime.now());
        return days + " ngày trước";
    }

    private BigDecimal calculateEarnings(List<ShipAssignment> assignments) {
        return assignments.stream()
                .map(a -> a.getOrder().getShippingFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

