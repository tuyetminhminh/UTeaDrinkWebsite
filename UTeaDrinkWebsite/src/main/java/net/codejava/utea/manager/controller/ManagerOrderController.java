package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.manager.dto.OrderManagementDTO;
import net.codejava.utea.manager.dto.ShipperAssignmentDTO;
import net.codejava.utea.manager.service.ExcelExportService;
import net.codejava.utea.manager.service.OrderManagementService;
import net.codejava.utea.manager.service.ShipperAssignmentService;
import net.codejava.utea.order.entity.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/manager/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerOrderController {

    private final OrderManagementService orderService;
    private final ShipperAssignmentService shipperService;
    private final ExcelExportService excelExportService;

    // ==================== HELPER METHODS ====================
    
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new RuntimeException("Invalid authentication principal");
    }

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang quản lý đơn hàng
     */
    @GetMapping
    public String orderManagement(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate toDate,
            Model model) {
        User currentUser = getCurrentUser(authentication);
        
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║         ORDER MANAGEMENT FILTER REQUEST             ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║ Page: " + page + " | Size: " + size);
        System.out.println("║ Status: " + (status != null ? status : "(all)"));
        System.out.println("║ FromDate: " + (fromDate != null ? fromDate : "(none)"));
        System.out.println("║ ToDate: " + (toDate != null ? toDate : "(none)"));
        System.out.println("╚══════════════════════════════════════════════════════╝");
        
        Pageable pageable = PageRequest.of(page, size);
        
        // Step 1: Get all orders (or by status)
        List<OrderManagementDTO> allOrders;
        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                allOrders = orderService.getOrdersByStatus(currentUser.getId(), orderStatus);
                System.out.println("→ Filtered by status '" + status + "': " + allOrders.size() + " orders");
            } catch (IllegalArgumentException e) {
                System.err.println("→ Invalid status: " + status);
                allOrders = orderService.getAllOrders(currentUser.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            }
        } else {
            allOrders = orderService.getAllOrders(currentUser.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            System.out.println("→ All orders (no status filter): " + allOrders.size() + " orders");
        }
        
        // Step 2: Filter by date range if provided
        if (fromDate != null && toDate != null) {
            // Create start and end of day for inclusive filtering
            java.time.LocalDateTime startOfDay = fromDate.atStartOfDay();
            java.time.LocalDateTime endOfDay = toDate.atTime(23, 59, 59, 999999999);
            
            System.out.println("→ Filtering from " + startOfDay + " to " + endOfDay);
            
            List<OrderManagementDTO> beforeFilter = new java.util.ArrayList<>(allOrders);
            allOrders = allOrders.stream()
                    .filter(order -> {
                        java.time.LocalDateTime createdAt = order.getCreatedAt();
                        boolean inRange = !createdAt.isBefore(startOfDay) && !createdAt.isAfter(endOfDay);
                        if (!inRange && beforeFilter.size() <= 10) { // Only log first few for debugging
                            System.out.println("  ✗ Order #" + order.getId() + " excluded: " + createdAt);
                        }
                        return inRange;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            System.out.println("→ After date filter: " + allOrders.size() + " orders (removed " + (beforeFilter.size() - allOrders.size()) + ")");
        } else if (fromDate != null) {
            // Only fromDate provided - filter from that date onwards
            java.time.LocalDateTime startOfDay = fromDate.atStartOfDay();
            allOrders = allOrders.stream()
                    .filter(order -> !order.getCreatedAt().isBefore(startOfDay))
                    .collect(java.util.stream.Collectors.toList());
            System.out.println("→ After 'from date' filter: " + allOrders.size() + " orders");
        } else if (toDate != null) {
            // Only toDate provided - filter up to that date
            java.time.LocalDateTime endOfDay = toDate.atTime(23, 59, 59, 999999999);
            allOrders = allOrders.stream()
                    .filter(order -> !order.getCreatedAt().isAfter(endOfDay))
                    .collect(java.util.stream.Collectors.toList());
            System.out.println("→ After 'to date' filter: " + allOrders.size() + " orders");
        }
        
        // Step 3: Apply pagination
        int start = Math.min((int) pageable.getOffset(), allOrders.size());
        int end = Math.min((start + pageable.getPageSize()), allOrders.size());
        List<OrderManagementDTO> pageContent = start < allOrders.size() 
                ? allOrders.subList(start, end) 
                : java.util.Collections.emptyList();
        
        Page<OrderManagementDTO> orders = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, allOrders.size());
        
        System.out.println("→ Final result: Page " + (page + 1) + "/" + orders.getTotalPages() + 
                          " with " + pageContent.size() + " orders (total: " + allOrders.size() + ")");
        System.out.println("=== END FILTER ===\n");
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "manager/order-management";
    }

    /**
     * Trang chi tiết đơn hàng
     */
    @GetMapping("/{orderId}")
    public String orderDetail(
            Authentication authentication,
            @PathVariable Long orderId,
            Model model) {
        User currentUser = getCurrentUser(authentication);
        OrderManagementDTO order = orderService.getOrderById(currentUser.getId(), orderId);
        
        model.addAttribute("order", order);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "manager/order-detail";
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API: Get all orders with optional status filter
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Page<OrderManagementDTO>> getOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        User currentUser = getCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<OrderManagementDTO> orders;
        if (status != null && !status.isEmpty()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<OrderManagementDTO> orderList = orderService.getOrdersByStatus(currentUser.getId(), orderStatus);
            orders = new org.springframework.data.domain.PageImpl<>(orderList, pageable, orderList.size());
        } else {
            orders = orderService.getAllOrders(currentUser.getId(), pageable);
        }
        
        return ResponseEntity.ok(orders);
    }

    /**
     * API: Get single order details
     */
    @GetMapping("/api/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getOrderById(
            Authentication authentication,
            @PathVariable Long orderId) {
        try {
            User currentUser = getCurrentUser(authentication);
            OrderManagementDTO order = orderService.getOrderById(currentUser.getId(), orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy số lượng đơn hàng đang giao
     */
    @GetMapping("/api/delivering-count")
    @ResponseBody
    public ResponseEntity<?> getDeliveringCount(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<OrderManagementDTO> deliveringOrders = orderService.getOrdersByStatus(
                    currentUser.getId(), OrderStatus.DELIVERING);

            return ResponseEntity.ok(java.util.Map.of(
                "count", deliveringOrders.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy tổng số đơn hàng (TẤT CẢ, không phải chỉ hôm nay)
     */
    @GetMapping("/api/total-count")
    @ResponseBody
    public ResponseEntity<?> getTotalOrderCount(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            // Lấy tất cả orders (page size 1 để chỉ lấy count)
            Pageable pageable = PageRequest.of(0, 1);
            Page<OrderManagementDTO> orders = orderService.getAllOrders(currentUser.getId(), pageable);
            
            return ResponseEntity.ok(java.util.Map.of(
                "total", orders.getTotalElements()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Đếm số lượng đơn hàng theo trạng thái (cho voice notification)
     */
    @GetMapping("/api/count")
    @ResponseBody
    public ResponseEntity<Integer> getOrderCountByStatus(
            Authentication authentication,
            @RequestParam(required = false) String status) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            if (status != null && !status.isEmpty()) {
                // Count by specific status
                try {
                    OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                    List<OrderManagementDTO> orders = orderService.getOrdersByStatus(
                            currentUser.getId(), orderStatus);
                    return ResponseEntity.ok(orders.size());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                // Count all orders
                Pageable pageable = PageRequest.of(0, 1);
                Page<OrderManagementDTO> orders = orderService.getAllOrders(currentUser.getId(), pageable);
                return ResponseEntity.ok((int) orders.getTotalElements());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Lấy đơn hàng NEW mới nhất (cho voice notification với tên khách hàng)
     */
    @GetMapping("/api/latest-new-order")
    @ResponseBody
    public ResponseEntity<?> getLatestNewOrder(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Lấy đơn NEW mới nhất
            OrderManagementDTO latestOrder = orderService.getLatestNewOrder(currentUser.getId());
            
            // Đếm tổng số đơn NEW
            List<OrderManagementDTO> newOrders = orderService.getOrdersByStatus(
                    currentUser.getId(), OrderStatus.NEW);
            int count = newOrders.size();
            
            if (latestOrder != null) {
                return ResponseEntity.ok(java.util.Map.of(
                    "count", count,
                    "customerName", latestOrder.getCustomerName() != null ? latestOrder.getCustomerName() : "Khách hàng",
                    "orderCode", latestOrder.getOrderCode() != null ? latestOrder.getOrderCode() : ""
                ));
            } else {
                return ResponseEntity.ok(java.util.Map.of(
                    "count", 0,
                    "customerName", "",
                    "orderCode", ""
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Lấy phân bố trạng thái đơn hàng cho pie chart
     */
    @GetMapping("/api/status-distribution")
    @ResponseBody
    public ResponseEntity<?> getStatusDistribution(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            java.util.List<String> labels = new java.util.ArrayList<>();
            java.util.List<Integer> values = new java.util.ArrayList<>();

            for (OrderStatus status : OrderStatus.values()) {
                List<OrderManagementDTO> orders = orderService.getOrdersByStatus(currentUser.getId(), status);
                if (!orders.isEmpty()) {
                    labels.add(getStatusTextVietnamese(status));
                    values.add(orders.size());
                }
            }

            return ResponseEntity.ok(java.util.Map.of(
                "labels", labels,
                "values", values
            ));
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
            Authentication authentication,
            @PathVariable Long orderId) {
        try {
            User currentUser = getCurrentUser(authentication);
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
            Authentication authentication,
            @PathVariable Long orderId) {
        try {
            User currentUser = getCurrentUser(authentication);
            OrderManagementDTO order = orderService.prepareOrder(currentUser.getId(), orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Bắt đầu giao hàng (DISABLED - Option 2: Shipper tự nhận)
     * Manager KHÔNG thể bắt đầu giao hàng. Shipper sẽ tự nhận từ PREPARING -> ASSIGNED -> DELIVERING
     */
    // @PutMapping("/api/{orderId}/deliver")
    // @ResponseBody
    // public ResponseEntity<?> deliverOrder(
    //         Authentication authentication,
    //         @PathVariable Long orderId) {
    //     try {
    //         User currentUser = getCurrentUser(authentication);
    //         OrderManagementDTO order = orderService.deliverOrder(currentUser.getId(), orderId);
    //         return ResponseEntity.ok(order);
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body(e.getMessage());
    //     }
    // }

    /**
     * API: Hoàn thành đơn hàng
     */
    @PutMapping("/api/{orderId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeOrder(
            Authentication authentication,
            @PathVariable Long orderId) {
        try {
            User currentUser = getCurrentUser(authentication);
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
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        try {
            User currentUser = getCurrentUser(authentication);
            OrderManagementDTO order = orderService.cancelOrder(currentUser.getId(), orderId, reason);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xử lý trả hàng
     */
    @PutMapping("/api/{orderId}/return")
    @ResponseBody
    public ResponseEntity<?> returnOrder(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        try {
            User currentUser = getCurrentUser(authentication);
            OrderManagementDTO order = orderService.returnOrder(currentUser.getId(), orderId, reason);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xử lý hoàn tiền
     */
    @PutMapping("/api/{orderId}/refund")
    @ResponseBody
    public ResponseEntity<?> refundOrder(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        try {
            User currentUser = getCurrentUser(authentication);
            OrderManagementDTO order = orderService.refundOrder(currentUser.getId(), orderId, reason);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Get current shipper of an order
     */
    @GetMapping("/api/{orderId}/shipper")
    @ResponseBody
    public ResponseEntity<?> getCurrentShipper(
            Authentication authentication,
            @PathVariable Long orderId) {
        try {
            User currentUser = getCurrentUser(authentication);
            ShipperAssignmentDTO assignment = shipperService.getAssignment(
                    currentUser.getId(), orderId);
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
    
    /**
     * API: Assign shipper to order (DISABLED - Option 2: Shipper tự nhận)
     * Manager KHÔNG thể assign shipper. Shipper tự nhận từ danh sách available orders
     */
    // @PostMapping("/api/{orderId}/assign-shipper")
    // @ResponseBody
    // public ResponseEntity<?> assignShipper(
    //         Authentication authentication,
    //         @PathVariable Long orderId,
    //         @RequestParam Long shipperId) {
    //     try {
    //         User currentUser = getCurrentUser(authentication);
    //         ShipperAssignmentDTO assignment = shipperService.assignShipper(
    //                 currentUser.getId(), orderId, shipperId);
    //         return ResponseEntity.ok(assignment);
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body(e.getMessage());
    //     }
    // }

    /**
     * API: Update shipper assignment (DISABLED - Option 2: Shipper tự nhận)
     * Manager KHÔNG thể thay đổi shipper assignment
     */
    // @PutMapping("/api/{orderId}/update-shipper")
    // @ResponseBody
    // public ResponseEntity<?> updateShipper(
    //         Authentication authentication,
    //         @PathVariable Long orderId,
    //         @RequestParam Long newShipperId) {
    //     try {
    //         User currentUser = getCurrentUser(authentication);
    //         ShipperAssignmentDTO assignment = shipperService.changeShipper(
    //                 currentUser.getId(), orderId, newShipperId);
    //         return ResponseEntity.ok(assignment);
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body(e.getMessage());
    //     }
    // }

    /**
     * API: Get available shippers (DISABLED - Option 2: Shipper tự nhận)
     * Không cần lấy danh sách shipper vì manager không assign
     */
    // @GetMapping("/api/shippers/available")
    // @ResponseBody
    // public ResponseEntity<?> getAvailableShippers(Authentication authentication) {
    //     try {
    //         List<ShipperAssignmentDTO> shippers = shipperService.getAvailableShippers();
    //         return ResponseEntity.ok(shippers);
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body(e.getMessage());
    //     }
    // }

    // ==================== EXCEL EXPORT ====================

    /**
     * Export orders to Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate toDate) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<OrderManagementDTO> orders;

            // Get orders by status or all
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                orders = orderService.getOrdersByStatus(currentUser.getId(), OrderStatus.valueOf(status));
            } else {
                Page<OrderManagementDTO> page = orderService.getAllOrders(
                        currentUser.getId(), PageRequest.of(0, Integer.MAX_VALUE));
                orders = page.getContent();
            }
            
            // Filter by date range if provided
            if (fromDate != null || toDate != null) {
                java.time.LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : java.time.LocalDateTime.MIN;
                java.time.LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : java.time.LocalDateTime.MAX;
                
                orders = orders.stream()
                        .filter(order -> {
                            java.time.LocalDateTime createdAt = order.getCreatedAt();
                            return !createdAt.isBefore(fromDateTime) && !createdAt.isAfter(toDateTime);
                        })
                        .collect(java.util.stream.Collectors.toList());
            }

            byte[] excelBytes = excelExportService.exportOrdersToExcel(orders);

            // Generate filename with current date and filters
            String dateRange = "";
            if (fromDate != null && toDate != null) {
                dateRange = "_" + fromDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + 
                           "_" + toDate.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            } else if (fromDate != null) {
                dateRange = "_tu" + fromDate.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            } else if (toDate != null) {
                dateRange = "_den" + toDate.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            }
            
            String filename = "DonHang" + dateRange + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== HELPER METHODS ====================

    private String getStatusTextVietnamese(OrderStatus status) {
        return switch (status) {
            case NEW -> "Mới";
            case PAID -> "Đã thanh toán";       // ✅ thêm dòng này
            case CONFIRMED -> "Đã xác nhận";
            case PREPARING -> "Đang chuẩn bị";
            case DELIVERING -> "Đang giao";
            case DELIVERED -> "Đã giao";
            case CANCELED -> "Đã huỷ";
            case RETURNED -> "Đã trả hàng";
            case REFUNDED -> "Đã hoàn tiền";
        };
    }

}
