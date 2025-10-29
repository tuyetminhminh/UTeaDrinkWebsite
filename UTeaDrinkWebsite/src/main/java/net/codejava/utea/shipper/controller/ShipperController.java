package net.codejava.utea.shipper.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.media.service.CloudinaryService;
import net.codejava.utea.shipper.dto.AvailableOrderDTO;
import net.codejava.utea.shipper.dto.MyOrderDTO;
import net.codejava.utea.shipper.dto.ShipperStatsDTO;
import net.codejava.utea.shipper.service.ShipperOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shipper")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SHIPPER')")
public class ShipperController {

    private final ShipperOrderService shipperOrderService;
    private final CloudinaryService cloudinaryService;


    // ==================== VIEWS ====================

    /**
     * Trang chủ shipper
     */
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal CustomUserDetails cud, Model model) {
        if (cud == null) {
            return "redirect:/auth/login";
        }

        Long shipperId = cud.getUser().getId();
        String shipperName = cud.getUser().getFullName();

        // Lấy thống kê
        ShipperStatsDTO stats = shipperOrderService.getShipperStats(shipperId);
        
        // Lấy danh sách đơn đang giao
        List<MyOrderDTO> deliveringOrders = shipperOrderService.getDeliveringOrders(shipperId);
        
        // Lấy danh sách đơn đã hủy (10 đơn gần nhất)
        List<MyOrderDTO> canceledOrders = shipperOrderService.getCanceledOrders(shipperId, 10);
        
        model.addAttribute("stats", stats);
        model.addAttribute("shipperName", shipperName);
        model.addAttribute("shipperId", shipperId);
        model.addAttribute("deliveringOrders", deliveringOrders);
        model.addAttribute("canceledOrders", canceledOrders);

        return "shipper/shipper-home";
    }

    /**
     * Danh sách đơn hàng khả dụng (PREPARING, chưa có shipper)
     */
    @GetMapping("/available-orders")
    public String availableOrders(@AuthenticationPrincipal CustomUserDetails cud, Model model) {
        Long shipperId = cud.getUser().getId();
        
        List<AvailableOrderDTO> orders = shipperOrderService.getAvailableOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("shipperId", shipperId);
        
        return "shipper/shipper-available-orders";
    }

    /**
     * Đơn hàng của tôi (đang giao + đã giao gần đây + đã hủy)
     */
    @GetMapping("/my-orders")
    public String myOrders(@AuthenticationPrincipal CustomUserDetails cud, Model model) {
        Long shipperId = cud.getUser().getId();

        List<MyOrderDTO> deliveringOrders = shipperOrderService.getDeliveringOrders(shipperId);
        List<MyOrderDTO> assignedOrders = shipperOrderService.getAssignedOrders(shipperId);

        model.addAttribute("deliveringOrders", deliveringOrders);
        model.addAttribute("assignedOrders", assignedOrders);
        model.addAttribute("shipperId", shipperId);

        return "shipper/shipper-my-orders";
    }

    /**
     * Lịch sử giao hàng
     */
    @GetMapping("/history")
    public String history(@AuthenticationPrincipal CustomUserDetails cud, Model model) {
        Long shipperId = cud.getUser().getId();

        // Lấy danh sách đơn đã giao
        List<MyOrderDTO> deliveredOrders = shipperOrderService.getDeliveredOrders(shipperId, 50);
        
        // Lấy danh sách đơn đã hủy
        List<MyOrderDTO> canceledOrders = shipperOrderService.getCanceledOrders(shipperId, 50);
        
        // Lấy thống kê
        ShipperStatsDTO stats = shipperOrderService.getShipperStats(shipperId);

        model.addAttribute("history", deliveredOrders); // Template dùng biến 'history'
        model.addAttribute("canceledOrders", canceledOrders);
        model.addAttribute("stats", stats);
        model.addAttribute("shipperId", shipperId);

        return "shipper/shipper-history";
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API: Lấy danh sách đơn hàng khả dụng
     */
    @GetMapping("/api/available-orders")
    @ResponseBody
    public ResponseEntity<List<AvailableOrderDTO>> getAvailableOrders() {
        try {
            List<AvailableOrderDTO> orders = shipperOrderService.getAvailableOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Lấy chi tiết đơn hàng
     */
    @GetMapping("/api/orders/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getOrderDetail(@PathVariable Long orderId, @AuthenticationPrincipal CustomUserDetails cud) {
        try {
            Long shipperId = cud.getUser().getId();
            MyOrderDTO order = shipperOrderService.getDeliveringOrders(shipperId).stream()
                    .filter(o -> o.getOrderId().equals(orderId))
                    .findFirst()
                    .orElse(null);
            
            if (order == null) {
                order = shipperOrderService.getAssignedOrders(shipperId).stream()
                        .filter(o -> o.getOrderId().equals(orderId))
                        .findFirst()
                        .orElse(null);
            }
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy danh sách đơn đang giao
     */
    @GetMapping("/api/delivering-orders")
    @ResponseBody
    public ResponseEntity<?> getDeliveringOrders(@AuthenticationPrincipal CustomUserDetails cud) {
        try {
            Long shipperId = cud.getUser().getId();
            List<MyOrderDTO> orders = shipperOrderService.getDeliveringOrders(shipperId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Lấy danh sách đơn đã nhận nhưng chưa lấy hàng
     */
    @GetMapping("/api/assigned-orders")
    @ResponseBody
    public ResponseEntity<?> getAssignedOrders(@AuthenticationPrincipal CustomUserDetails cud) {
        try {
            Long shipperId = cud.getUser().getId();
            List<MyOrderDTO> orders = shipperOrderService.getAssignedOrders(shipperId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Nhận đơn hàng
     */
    @PostMapping("/api/orders/{orderId}/accept")
    @ResponseBody
    public ResponseEntity<?> acceptOrder(@PathVariable Long orderId, @AuthenticationPrincipal CustomUserDetails cud) {
        try {
            Long shipperId = cud.getUser().getId();
            shipperOrderService.acceptOrder(shipperId, orderId);
            return ResponseEntity.ok("Đã xác nhận nhận đơn hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xác nhận đã lấy hàng (ASSIGNED → DELIVERING)
     */
    @PostMapping("/api/orders/{orderId}/confirm-pickup")
    @ResponseBody
    public ResponseEntity<?> confirmPickup(@PathVariable Long orderId, @AuthenticationPrincipal CustomUserDetails cud) {
        try {
            Long shipperId = cud.getUser().getId();
            shipperOrderService.confirmPickup(shipperId, orderId);
            return ResponseEntity.ok("Đã xác nhận lấy hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Hoàn thành giao hàng
     */
    @PostMapping("/api/orders/{orderId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeDelivery(
            @PathVariable Long orderId,
            @RequestParam(required = false) String deliveryNote,
            @RequestParam(required = false) String proofImageUrl,
            @AuthenticationPrincipal CustomUserDetails cud) {
        try {
            Long shipperId = cud.getUser().getId();
            shipperOrderService.completeDelivery(shipperId, orderId, deliveryNote, proofImageUrl);
            return ResponseEntity.ok("Đã hoàn thành giao hàng");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ API MỚI: Báo cáo không giao được hàng
     */
    @PostMapping("/api/orders/{orderId}/failed")
    @ResponseBody
    public ResponseEntity<?> reportFailedDelivery(
            @PathVariable Long orderId,
            @RequestParam String failureReason,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal CustomUserDetails cud) {
        try {
            Long shipperId = cud.getUser().getId();
            shipperOrderService.reportFailedDelivery(shipperId, orderId, failureReason, note);
            return ResponseEntity.ok("Đã báo cáo không giao được hàng");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ API: Upload ảnh bằng chứng giao hàng
     */
    @PostMapping("/api/upload-proof-image")
    @ResponseBody
    public ResponseEntity<?> uploadProofImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(required = false) Long orderId,
            @AuthenticationPrincipal CustomUserDetails cud) {
        try {
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chưa có ảnh để upload"));
            }

            Long shipperId = cud.getUser().getId();
            
            // Upload lên Cloudinary
            String folder = "delivery-proofs/shipper-" + shipperId;
            String publicId = "proof_order_" + (orderId != null ? orderId : System.currentTimeMillis());
            
            Map<String, Object> uploadResult = cloudinaryService.upload(image, folder, publicId);
            String imageUrl = (String) uploadResult.get("secure_url");
            
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = (String) uploadResult.get("url");
            }
            
            // Trả về JSON với imageUrl
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("publicId", uploadResult.get("public_id"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi upload ảnh: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * API: Lấy thống kê shipper
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<?> getStats(@AuthenticationPrincipal CustomUserDetails cud) {
        try {
            Long shipperId = cud.getUser().getId();
            ShipperStatsDTO stats = shipperOrderService.getShipperStats(shipperId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy danh sách đơn đã giao
     */
    @GetMapping("/api/delivered-orders")
    @ResponseBody
    public ResponseEntity<?> getDeliveredOrders(@AuthenticationPrincipal CustomUserDetails cud) {
        try {
            Long shipperId = cud.getUser().getId();
            List<MyOrderDTO> orders = shipperOrderService.getDeliveredOrders(shipperId, 50);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy danh sách đơn đã hủy
     */
    @GetMapping("/api/canceled-orders")
    @ResponseBody
    public ResponseEntity<?> getCanceledOrders(@AuthenticationPrincipal CustomUserDetails cud) {
        try {
            Long shipperId = cud.getUser().getId();
            List<MyOrderDTO> orders = shipperOrderService.getCanceledOrders(shipperId, 50);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

