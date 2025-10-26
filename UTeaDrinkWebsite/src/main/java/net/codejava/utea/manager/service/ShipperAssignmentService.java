package net.codejava.utea.manager.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.manager.dto.ShipperAssignmentDTO;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.shipping.entity.ShipAssignment;
import net.codejava.utea.shipping.entity.ShipperProfile;
import net.codejava.utea.shipping.repository.ShipAssignmentRepository;
import net.codejava.utea.shipping.repository.ShipperProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShipperAssignmentService {

    private final ShipAssignmentRepository shipAssignmentRepo;
    private final ShipperProfileRepository shipperProfileRepo;
    private final OrderRepository orderRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final UserRepository userRepo;

    // ==================== SHIPPER ASSIGNMENT ====================

    /**
     * Phân công shipper cho đơn hàng
     */
    @Transactional
    public ShipperAssignmentDTO assignShipper(Long managerId, Long orderId, Long shipperId) {
        Shop shop = getShopByManagerId(managerId);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Kiểm tra đơn hàng có thuộc shop của manager không
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền phân công shipper cho đơn hàng này");
        }

        // Kiểm tra đơn hàng có thể giao không
        if (order.getStatus() != OrderStatus.PREPARING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Đơn hàng không ở trạng thái cho phép phân công shipper");
        }

        User shipper = userRepo.findById(shipperId)
                .orElseThrow(() -> new RuntimeException("Shipper không tồn tại"));

        // Kiểm tra shipper có profile không
        if (!shipperProfileRepo.existsByUserId(shipperId)) {
            throw new RuntimeException("Shipper chưa được kích hoạt");
        }

        // Kiểm tra xem đơn hàng đã được phân công chưa
        ShipAssignment existingAssignment = shipAssignmentRepo.findByOrderId(orderId).orElse(null);
        
        if (existingAssignment != null) {
            // Cập nhật shipper mới
            existingAssignment.setShipper(shipper);
            existingAssignment.setAssignedAt(LocalDateTime.now());
            existingAssignment.setStatus("ASSIGNED");
            existingAssignment = shipAssignmentRepo.save(existingAssignment);
            return convertToDTO(existingAssignment);
        }

        // Tạo phân công mới
        ShipAssignment assignment = ShipAssignment.builder()
                .order(order)
                .shipper(shipper)
                .status("ASSIGNED")
                .assignedAt(LocalDateTime.now())
                .build();

        assignment = shipAssignmentRepo.save(assignment);

        // Cập nhật trạng thái đơn hàng sang DELIVERING
        order.setStatus(OrderStatus.DELIVERING);
        orderRepo.save(order);

        return convertToDTO(assignment);
    }

    /**
     * Đổi shipper cho đơn hàng
     */
    @Transactional
    public ShipperAssignmentDTO changeShipper(Long managerId, Long orderId, Long newShipperId) {
        return assignShipper(managerId, orderId, newShipperId);
    }

    /**
     * Lấy thông tin phân công shipper của đơn hàng
     */
    public ShipperAssignmentDTO getAssignment(Long managerId, Long orderId) {
        Shop shop = getShopByManagerId(managerId);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Kiểm tra đơn hàng có thuộc shop của manager không
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền truy cập thông tin đơn hàng này");
        }

        ShipAssignment assignment = shipAssignmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng chưa được phân công shipper"));

        return convertToDTO(assignment);
    }

    /**
     * Lấy danh sách shipper khả dụng
     */
    public List<ShipperAssignmentDTO> getAvailableShippers() {
        // Lấy tất cả shipper profile
        List<ShipperProfile> profiles = shipperProfileRepo.findAll();

        return profiles.stream()
                .map(profile -> {
                    // Đếm số đơn hàng đang giao của shipper
                    int activeOrders = shipAssignmentRepo
                            .findByShipperIdAndStatus(profile.getUser().getId(), "DELIVERING")
                            .size();

                    return ShipperAssignmentDTO.builder()
                            .shipperId(profile.getUser().getId())
                            .shipperName(profile.getUser().getFullName())
                            .shipperPhone(profile.getUser().getEmail()) // hoặc phone nếu có
                            .vehicleType(profile.getVehicleType())
                            .note("Đang giao " + activeOrders + " đơn")
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật trạng thái phân công
     */
    @Transactional
    public ShipperAssignmentDTO updateAssignmentStatus(Long managerId, Long assignmentId, String status) {
        Shop shop = getShopByManagerId(managerId);

        ShipAssignment assignment = shipAssignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Phân công không tồn tại"));

        // Kiểm tra phân công có thuộc shop của manager không
        if (!assignment.getOrder().getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền cập nhật phân công này");
        }

        assignment.setStatus(status);

        switch (status) {
            case "PICKED_UP":
                assignment.setPickedUpAt(LocalDateTime.now());
                break;
            case "DELIVERED":
                assignment.setDeliveredAt(LocalDateTime.now());
                // Cập nhật trạng thái đơn hàng
                assignment.getOrder().setStatus(OrderStatus.DELIVERED);
                orderRepo.save(assignment.getOrder());
                break;
            case "FAILED":
                // Có thể cập nhật trạng thái đơn hàng thành RETURNED
                assignment.getOrder().setStatus(OrderStatus.RETURNED);
                orderRepo.save(assignment.getOrder());
                break;
        }

        assignment = shipAssignmentRepo.save(assignment);

        return convertToDTO(assignment);
    }

    /**
     * Hủy phân công shipper
     */
    @Transactional
    public void cancelAssignment(Long managerId, Long orderId) {
        Shop shop = getShopByManagerId(managerId);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Kiểm tra đơn hàng có thuộc shop của manager không
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền hủy phân công cho đơn hàng này");
        }

        ShipAssignment assignment = shipAssignmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng chưa được phân công shipper"));

        // Chỉ cho phép hủy nếu shipper chưa lấy hàng
        if (!assignment.getStatus().equals("ASSIGNED")) {
            throw new RuntimeException("Không thể hủy phân công khi shipper đã lấy hàng");
        }

        shipAssignmentRepo.delete(assignment);

        // Cập nhật lại trạng thái đơn hàng
        order.setStatus(OrderStatus.PREPARING);
        orderRepo.save(order);
    }

    // ==================== HELPER METHODS ====================

    private Shop getShopByManagerId(Long managerId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));
        return shopManager.getShop();
    }

    private ShipperAssignmentDTO convertToDTO(ShipAssignment assignment) {
        ShipperProfile profile = shipperProfileRepo.findByUserId(assignment.getShipper().getId()).orElse(null);

        return ShipperAssignmentDTO.builder()
                .id(assignment.getId())
                .orderId(assignment.getOrder().getId())
                .orderCode(assignment.getOrder().getOrderCode())
                .shipperId(assignment.getShipper().getId())
                .shipperName(assignment.getShipper().getFullName())
                .shipperPhone(assignment.getShipper().getEmail()) // hoặc phone nếu có
                .vehicleType(profile != null ? profile.getVehicleType() : "")
                .status(assignment.getStatus())
                .assignedAt(assignment.getAssignedAt())
                .pickedUpAt(assignment.getPickedUpAt())
                .deliveredAt(assignment.getDeliveredAt())
                .note(assignment.getNote())
                .build();
    }
}

