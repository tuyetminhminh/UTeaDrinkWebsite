package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.manager.dto.VoucherManagementDTO;
import net.codejava.utea.manager.service.VoucherManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/manager/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerVoucherController {

    private final VoucherManagementService voucherService;

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
     * Trang quản lý voucher
     */
    @GetMapping
    public String voucherManagement(
            Authentication authentication,
            Model model) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<VoucherManagementDTO> vouchers = voucherService.getAllVouchers(currentUser.getId());
            model.addAttribute("vouchers", vouchers);
            model.addAttribute("hasShop", true);
        } catch (RuntimeException e) {
            // Manager chưa có shop
            model.addAttribute("hasShop", false);
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "manager/voucher-management";
    }

    /**
     * Trang tạo voucher mới
     */
    @GetMapping("/create")
    public String createVoucherPage() {
        return "manager/voucher-create";
    }

    /**
     * Trang chỉnh sửa voucher
     */
    @GetMapping("/{voucherId}/edit")
    public String editVoucherPage(
            Authentication authentication,
            @PathVariable Long voucherId,
            Model model) {
        User currentUser = getCurrentUser(authentication);
        VoucherManagementDTO voucher = voucherService.getVoucherById(currentUser.getId(), voucherId);
        model.addAttribute("voucher", voucher);
        return "manager/voucher-edit";
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API: Lấy tất cả voucher
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getAllVouchers(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<VoucherManagementDTO> vouchers = voucherService.getAllVouchers(currentUser.getId());
            return ResponseEntity.ok(vouchers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy chi tiết voucher
     */
    @GetMapping("/api/{voucherId}")
    @ResponseBody
    public ResponseEntity<?> getVoucherById(
            Authentication authentication,
            @PathVariable Long voucherId) {
        try {
            User currentUser = getCurrentUser(authentication);
            VoucherManagementDTO voucher = voucherService.getVoucherById(currentUser.getId(), voucherId);
            return ResponseEntity.ok(voucher);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Tạo voucher mới
     */
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createVoucher(
            Authentication authentication,
            @RequestBody VoucherManagementDTO voucherDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            VoucherManagementDTO createdVoucher = voucherService.createVoucher(currentUser.getId(), voucherDTO);
            return ResponseEntity.ok(createdVoucher);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Cập nhật voucher
     */
    @PutMapping("/api/{voucherId}")
    @ResponseBody
    public ResponseEntity<?> updateVoucher(
            Authentication authentication,
            @PathVariable Long voucherId,
            @RequestBody VoucherManagementDTO voucherDTO) {
        try {
            User currentUser = getCurrentUser(authentication);
            VoucherManagementDTO updatedVoucher = voucherService.updateVoucher(currentUser.getId(), voucherId, voucherDTO);
            return ResponseEntity.ok(updatedVoucher);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xóa voucher
     */
    @DeleteMapping("/api/{voucherId}")
    @ResponseBody
    public ResponseEntity<?> deleteVoucher(
            Authentication authentication,
            @PathVariable Long voucherId) {
        try {
            User currentUser = getCurrentUser(authentication);
            voucherService.deleteVoucher(currentUser.getId(), voucherId);
            return ResponseEntity.ok("Voucher đã được xóa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Kích hoạt/Vô hiệu hóa voucher
     */
    @PutMapping("/api/{voucherId}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleVoucherStatus(
            Authentication authentication,
            @PathVariable Long voucherId) {
        try {
            User currentUser = getCurrentUser(authentication);
            VoucherManagementDTO voucher = voucherService.toggleVoucherStatus(currentUser.getId(), voucherId);
            return ResponseEntity.ok(voucher);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Gửi email voucher cho khách hàng
     */
    @PostMapping("/api/{voucherId}/broadcast")
    @ResponseBody
    public ResponseEntity<?> broadcastVoucher(
            Authentication authentication,
            @PathVariable Long voucherId) {
        try {
            User currentUser = getCurrentUser(authentication);
            int sentCount = voucherService.broadcastVoucher(currentUser.getId(), voucherId);
            return ResponseEntity.ok(new BroadcastResult(sentCount, "Đã gửi email voucher thành công cho " + sentCount + " khách hàng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Helper class for broadcast response
    private record BroadcastResult(int sentCount, String message) {}
}

