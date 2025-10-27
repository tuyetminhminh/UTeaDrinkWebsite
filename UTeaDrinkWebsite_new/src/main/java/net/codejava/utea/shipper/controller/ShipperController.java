package net.codejava.utea.shipper.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.shipper.dto.AvailableOrderDTO;
import net.codejava.utea.shipper.dto.MyOrderDTO;
import net.codejava.utea.shipper.dto.ShipperStatsDTO;
import net.codejava.utea.shipper.service.ShipperOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shipper")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SHIPPER')")
public class ShipperController {

    private final ShipperOrderService shipperService;

    // ==================== HELPER ====================
    
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new RuntimeException("Invalid authentication principal");
    }

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Dashboard / Trang chủ Shipper
     */
    @GetMapping({"/", "/home"})
    public String home(Authentication authentication, Model model) {
        User shipper = getCurrentUser(authentication);
        
        ShipperStatsDTO stats = shipperService.getShipperStats(shipper.getId());
        List<MyOrderDTO> deliveringOrders = shipperService.getDeliveringOrders(shipper.getId());
        
        model.addAttribute("stats", stats);
        model.addAttribute("deliveringOrders", deliveringOrders);
        model.addAttribute("shipperName", shipper.getFullName());
        model.addAttribute("shipperId", shipper.getId());
        
        return "shipper/shipper-home";
    }

    /**
     * Danh sách đơn khả dụng (PREPARING)
     */
    @GetMapping("/available-orders")
    public String availableOrders(Authentication authentication, Model model) {
        User shipper = getCurrentUser(authentication);
        List<AvailableOrderDTO> orders = shipperService.getAvailableOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("shipperId", shipper.getId());
        return "shipper/shipper-available-orders";
    }

    /**
     * Đơn của tôi (DELIVERING + DELIVERED)
     */
    @GetMapping("/my-orders")
    public String myOrders(Authentication authentication, Model model) {
        User shipper = getCurrentUser(authentication);
        
        List<MyOrderDTO> deliveringOrders = shipperService.getDeliveringOrders(shipper.getId());
        List<MyOrderDTO> deliveredOrders = shipperService.getDeliveredOrders(shipper.getId(), 20);
        
        model.addAttribute("deliveringOrders", deliveringOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("shipperId", shipper.getId());
        model.addAttribute("shipperName", shipper.getFullName());
        
        return "shipper/shipper-my-orders";
    }

    /**
     * Lịch sử giao hàng
     */
    @GetMapping("/history")
    public String history(Authentication authentication, Model model) {
        User shipper = getCurrentUser(authentication);
        
        ShipperStatsDTO stats = shipperService.getShipperStats(shipper.getId());
        List<MyOrderDTO> history = shipperService.getDeliveredOrders(shipper.getId(), 100);
        
        model.addAttribute("stats", stats);
        model.addAttribute("history", history);
        model.addAttribute("shipperId", shipper.getId());
        model.addAttribute("shipperName", shipper.getFullName());
        
        return "shipper/shipper-history";
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API: Lấy danh sách đơn đã nhận chưa lấy hàng (ASSIGNED)
     */
    @GetMapping("/api/assigned-orders")
    @ResponseBody
    public ResponseEntity<List<MyOrderDTO>> getAssignedOrders(Authentication authentication) {
        User shipper = getCurrentUser(authentication);
        List<MyOrderDTO> orders = shipperService.getAssignedOrders(shipper.getId());
        return ResponseEntity.ok(orders);
    }

    /**
     * API: Nhận đơn hàng (cho available-orders.html)
     */
    @PostMapping("/api/orders/{orderId}/accept")
    @ResponseBody
    public ResponseEntity<?> acceptOrderWithPath(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> payload) {
        try {
            User shipper = getCurrentUser(authentication);
            MyOrderDTO order = shipperService.acceptOrder(shipper.getId(), orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Nhận đơn hàng (legacy)
     */
    @PostMapping("/api/accept-order")
    @ResponseBody
    public ResponseEntity<?> acceptOrder(
            Authentication authentication,
            @RequestParam Long orderId) {
        try {
            User shipper = getCurrentUser(authentication);
            MyOrderDTO order = shipperService.acceptOrder(shipper.getId(), orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy danh sách đơn đang giao (DELIVERING)
     */
    @GetMapping("/api/my-orders/delivering")
    @ResponseBody
    public ResponseEntity<List<MyOrderDTO>> getMyDeliveringOrders(
            Authentication authentication,
            @RequestParam(required = false) Long shipperId) {
        try {
            User shipper = getCurrentUser(authentication);
            List<MyOrderDTO> orders = shipperService.getDeliveringOrders(shipper.getId());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Lấy danh sách đơn đã giao (DELIVERED)
     */
    @GetMapping("/api/my-orders/delivered")
    @ResponseBody
    public ResponseEntity<List<MyOrderDTO>> getMyDeliveredOrders(
            Authentication authentication,
            @RequestParam(required = false) Long shipperId) {
        try {
            User shipper = getCurrentUser(authentication);
            List<MyOrderDTO> orders = shipperService.getDeliveredOrders(shipper.getId(), 50);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Xác nhận đã lấy hàng (cho shipper-my-orders.html)
     */
    @PostMapping("/api/orders/{orderId}/confirm-pickup")
    @ResponseBody
    public ResponseEntity<?> confirmPickupWithPath(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> payload) {
        try {
            User shipper = getCurrentUser(authentication);
            shipperService.confirmPickup(shipper.getId(), orderId);
            return ResponseEntity.ok("Đã xác nhận lấy hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xác nhận đã lấy hàng (legacy)
     */
    @PostMapping("/api/confirm-pickup")
    @ResponseBody
    public ResponseEntity<?> confirmPickup(
            Authentication authentication,
            @RequestParam Long orderId) {
        try {
            User shipper = getCurrentUser(authentication);
            shipperService.confirmPickup(shipper.getId(), orderId);
            return ResponseEntity.ok("Đã xác nhận lấy hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Hoàn thành giao hàng (dùng cho shipper-my-orders.html)
     */
    @PostMapping("/api/orders/{orderId}/complete-delivery")
    @ResponseBody
    public ResponseEntity<?> completeDeliveryWithJson(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> payload) {
        try {
            User shipper = getCurrentUser(authentication);
            String deliveryNote = payload.get("deliveryNote");
            String proofImage = payload.get("proofImage");
            shipperService.completeDelivery(shipper.getId(), orderId, deliveryNote, proofImage);
            return ResponseEntity.ok("Đã hoàn thành giao hàng");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Hoàn thành giao hàng (legacy, dùng cho các trang khác)
     */
    @PostMapping("/api/complete-delivery")
    @ResponseBody
    public ResponseEntity<?> completeDelivery(
            Authentication authentication,
            @RequestParam Long orderId,
            @RequestParam(required = false) String deliveryNote,
            @RequestParam(required = false) String proofImageUrl) {
        try {
            User shipper = getCurrentUser(authentication);
            shipperService.completeDelivery(shipper.getId(), orderId, deliveryNote, proofImageUrl);
            return ResponseEntity.ok("Đã hoàn thành giao hàng");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy thống kê
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<?> getStats(Authentication authentication) {
        try {
            User shipper = getCurrentUser(authentication);
            ShipperStatsDTO stats = shipperService.getShipperStats(shipper.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy danh sách đơn khả dụng
     */
    @GetMapping("/api/available-orders")
    @ResponseBody
    public ResponseEntity<?> getAvailableOrders() {
        try {
            List<AvailableOrderDTO> orders = shipperService.getAvailableOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

