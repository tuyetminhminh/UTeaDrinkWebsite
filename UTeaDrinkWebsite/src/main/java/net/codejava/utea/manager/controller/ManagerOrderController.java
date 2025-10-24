package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.manager.dto.OrderManagementDTO;
import net.codejava.utea.manager.dto.ShipperAssignmentDTO;
import net.codejava.utea.manager.service.OrderManagementService;
import net.codejava.utea.manager.service.ShipperAssignmentService;
import net.codejava.utea.order.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/manager/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerOrderController {

    private final OrderManagementService orderService;
    private final ShipperAssignmentService shipperService;

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang quản lý đơn hàng
     */
    @GetMapping
    public String orderManagement(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderManagementDTO> orders = orderService.getAllOrders(currentUser.getId(), pageable);
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "manager/order-management";
    }

    /**
     * Trang chi tiết đơn hàng
     */
    @GetMapping("/{orderId}")
    public String orderDetail(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId,
            Model model) {
        OrderManagementDTO order = orderService.getOrderById(currentUser.getId(), orderId);
        model.addAttribute("order", order);
        
        // Lấy thông tin shipper nếu có
        try {
            ShipperAssignmentDTO assignment = shipperService.getAssignment(currentUser.getId(), orderId);
            model.addAttribute("assignment", assignment);
        } catch (Exception e) {
            model.addAttribute("assignment", null);
        }
        
        return "manager/order-detail";
    }

    /**
     * Trang phân công shipper
     */
    @GetMapping("/{orderId}/assign-shipper")
    public String assignShipperPage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId,
            Model model) {
        OrderManagementDTO order = orderService.getOrderById(currentUser.getId(), orderId);
        List<ShipperAssignmentDTO> shippers = shipperService.getAvailableShippers();
        
        model.addAttribute("order", order);
        model.addAttribute("shippers", shippers);
        return "manager/order-assign-shipper";
    }

    // ==================== API ENDPOINTS - ORDER ====================

    /**
     * API: Lấy tất cả đơn hàng
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getAllOrders(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderManagementDTO> orders = orderService.getAllOrders(currentUser.getId(), pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy đơn hàng theo trạng thái
     */
    @GetMapping("/api/status/{status}")
    @ResponseBody
    public ResponseEntity<?> getOrdersByStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            List<OrderManagementDTO> orders = orderService.getOrdersByStatus(currentUser.getId(), orderStatus);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy chi tiết đơn hàng
     */
    @GetMapping("/api/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getOrderById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {
        try {
            OrderManagementDTO order = orderService.getOrderById(currentUser.getId(), orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xác nhận đơn hàng
     */
    @PutMapping("/api/{orderId}/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {
        try {
            OrderManagementDTO order = orderService.confirmOrder(currentUser.getId(), orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Chuẩn bị đơn hàng
     */
    @PutMapping("/api/{orderId}/prepare")
    @ResponseBody
    public ResponseEntity<?> prepareOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {
        try {
            OrderManagementDTO order = orderService.prepareOrder(currentUser.getId(), orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Bắt đầu giao hàng
     */
    @PutMapping("/api/{orderId}/deliver")
    @ResponseBody
    public ResponseEntity<?> deliverOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {
        try {
            OrderManagementDTO order = orderService.deliverOrder(currentUser.getId(), orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Hoàn thành đơn hàng
     */
    @PutMapping("/api/{orderId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {
        try {
            OrderManagementDTO order = orderService.completeOrder(currentUser.getId(), orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Hủy đơn hàng
     */
    @PutMapping("/api/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        try {
            OrderManagementDTO order = orderService.cancelOrder(currentUser.getId(), orderId, reason);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Trả hàng
     */
    @PutMapping("/api/{orderId}/return")
    @ResponseBody
    public ResponseEntity<?> returnOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        try {
            OrderManagementDTO order = orderService.returnOrder(currentUser.getId(), orderId, reason);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Hoàn tiền
     */
    @PutMapping("/api/{orderId}/refund")
    @ResponseBody
    public ResponseEntity<?> refundOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        try {
            OrderManagementDTO order = orderService.refundOrder(currentUser.getId(), orderId, reason);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== API ENDPOINTS - SHIPPER ASSIGNMENT ====================

    /**
     * API: Phân công shipper
     */
    @PostMapping("/api/{orderId}/assign-shipper")
    @ResponseBody
    public ResponseEntity<?> assignShipper(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId,
            @RequestParam Long shipperId) {
        try {
            ShipperAssignmentDTO assignment = shipperService.assignShipper(currentUser.getId(), orderId, shipperId);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Đổi shipper
     */
    @PutMapping("/api/{orderId}/change-shipper")
    @ResponseBody
    public ResponseEntity<?> changeShipper(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId,
            @RequestParam Long newShipperId) {
        try {
            ShipperAssignmentDTO assignment = shipperService.changeShipper(currentUser.getId(), orderId, newShipperId);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy thông tin phân công
     */
    @GetMapping("/api/{orderId}/assignment")
    @ResponseBody
    public ResponseEntity<?> getAssignment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {
        try {
            ShipperAssignmentDTO assignment = shipperService.getAssignment(currentUser.getId(), orderId);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy danh sách shipper khả dụng
     */
    @GetMapping("/api/shippers/available")
    @ResponseBody
    public ResponseEntity<?> getAvailableShippers() {
        try {
            List<ShipperAssignmentDTO> shippers = shipperService.getAvailableShippers();
            return ResponseEntity.ok(shippers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Cập nhật trạng thái phân công
     */
    @PutMapping("/api/assignments/{assignmentId}/status")
    @ResponseBody
    public ResponseEntity<?> updateAssignmentStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long assignmentId,
            @RequestParam String status) {
        try {
            ShipperAssignmentDTO assignment = shipperService.updateAssignmentStatus(currentUser.getId(), assignmentId, status);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Hủy phân công shipper
     */
    @DeleteMapping("/api/{orderId}/assignment")
    @ResponseBody
    public ResponseEntity<?> cancelAssignment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {
        try {
            shipperService.cancelAssignment(currentUser.getId(), orderId);
            return ResponseEntity.ok("Đã hủy phân công shipper");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

