package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.manager.dto.RevenueReportDTO;
import net.codejava.utea.manager.service.RevenueReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/manager/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerRevenueController {

    private final RevenueReportService revenueService;
    
    /**
     * Helper method to extract User from Authentication
     */
    private User getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new IllegalStateException("Không thể xác định người dùng hiện tại");
    }

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang dashboard doanh thu
     */
    @GetMapping
    public String revenueDashboard(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Mặc định: tháng hiện tại (thay vì 30 ngày)
        if (fromDate == null) {
            fromDate = LocalDate.now().withDayOfMonth(1);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }

        RevenueReportDTO report = revenueService.getRevenueReport(currentUser.getId(), fromDate, toDate);
        
        model.addAttribute("report", report);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "manager/revenue-dashboard";
    }

    /**
     * Trang báo cáo chi tiết
     */
    @GetMapping("/report")
    public String revenueReport(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Mặc định: tháng hiện tại
        if (fromDate == null) {
            fromDate = LocalDate.now().withDayOfMonth(1);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }

        RevenueReportDTO report = revenueService.getRevenueReport(currentUser.getId(), fromDate, toDate);
        
        model.addAttribute("report", report);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "manager/revenue-report";
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API: Lấy báo cáo doanh thu
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getRevenueReport(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        try {
            User currentUser = getCurrentUser(authentication);
            RevenueReportDTO report = revenueService.getRevenueReport(currentUser.getId(), fromDate, toDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Xuất báo cáo Excel
     */
    @GetMapping("/api/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        try {
            User currentUser = getCurrentUser(authentication);
            byte[] excelData = revenueService.exportToExcel(currentUser.getId(), fromDate, toDate);
            
            // Filename with proper encoding
            String filename = String.format("BaoCaoDoanhThu_%s_den_%s.xlsx", fromDate, toDate);
            
            HttpHeaders headers = new HttpHeaders();
            // Set proper content type for Excel files
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.setContentLength(excelData.length);
            // Prevent caching
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            System.out.println("=== Exporting Excel ===");
            System.out.println("Manager ID: " + currentUser.getId());
            System.out.println("Date range: " + fromDate + " to " + toDate);
            System.out.println("File size: " + excelData.length + " bytes");
            System.out.println("Filename: " + filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (IOException e) {
            System.err.println("ERROR exporting Excel: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            System.err.println("ERROR in exportExcel: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Lấy báo cáo nhanh (hôm nay) - Enhanced for Dashboard
     */
    @GetMapping("/api/today")
    @ResponseBody
    public ResponseEntity<?> getTodayReport(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            
            System.out.println("=== DEBUG: getTodayReport ===");
            System.out.println("Manager ID: " + currentUser.getId());
            System.out.println("Today: " + today);
            
            RevenueReportDTO todayReport = revenueService.getRevenueReport(currentUser.getId(), today, today);
            RevenueReportDTO yesterdayReport = revenueService.getRevenueReport(currentUser.getId(), yesterday, yesterday);
            
            System.out.println("Today's completed orders: " + todayReport.getCompletedOrders());
            System.out.println("Today's total revenue (BigDecimal): " + todayReport.getTotalRevenue());
            System.out.println("Today's total revenue (long): " + todayReport.getTotalRevenue().longValue());
            
            // Calculate trends
            double revenueTrend = yesterdayReport.getTotalRevenue().doubleValue() > 0
                    ? ((todayReport.getTotalRevenue().doubleValue() - yesterdayReport.getTotalRevenue().doubleValue()) 
                       / yesterdayReport.getTotalRevenue().doubleValue()) * 100
                    : 0;
                    
            int orderTrend = todayReport.getTotalOrders() - yesterdayReport.getTotalOrders();
            
            // Convert BigDecimal to long for proper JSON serialization
            long totalRevenueValue = todayReport.getTotalRevenue().longValue();
            
            System.out.println("Response totalRevenue: " + totalRevenueValue);
            
            return ResponseEntity.ok(java.util.Map.of(
                "totalRevenue", totalRevenueValue,
                "totalOrders", todayReport.getTotalOrders(),
                "trend", Math.round(revenueTrend * 10) / 10.0,
                "orderTrend", orderTrend
            ));
        } catch (Exception e) {
            System.err.println("ERROR in getTodayReport: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * API: Lấy dữ liệu doanh thu 7 ngày gần nhất (cho chart)
     * Doanh thu tính theo ngày GIAO hàng (DELIVERED), không phải ngày tạo đơn
     */
    @GetMapping("/api/last7days")
    @ResponseBody
    public ResponseEntity<?> getLast7DaysRevenue(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);
            
            System.out.println("\n=== DEBUG: getLast7DaysRevenue ===");
            System.out.println("Date range: " + startDate + " to " + endDate);
            
            java.util.List<String> labels = new java.util.ArrayList<>();
            java.util.List<Long> values = new java.util.ArrayList<>(); // Changed to Long for proper JSON serialization
            
            for (int i = 0; i < 7; i++) {
                LocalDate date = startDate.plusDays(i);
                RevenueReportDTO dayReport = revenueService.getRevenueReport(currentUser.getId(), date, date);
                
                String label = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
                long revenue = dayReport.getTotalRevenue().longValue();
                
                System.out.println("  " + label + ": " + revenue + " VND (" + dayReport.getCompletedOrders() + " orders)");
                
                labels.add(label);
                values.add(revenue);
            }
            
            System.out.println("=== END DEBUG ===\n");
            
            return ResponseEntity.ok(java.util.Map.of(
                "labels", labels,
                "values", values
            ));
        } catch (Exception e) {
            System.err.println("ERROR in getLast7DaysRevenue: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy báo cáo tuần này
     */
    @GetMapping("/api/this-week")
    @ResponseBody
    public ResponseEntity<?> getThisWeekReport(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
            RevenueReportDTO report = revenueService.getRevenueReport(currentUser.getId(), startOfWeek, today);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy báo cáo tháng này
     */
    @GetMapping("/api/this-month")
    @ResponseBody
    public ResponseEntity<?> getThisMonthReport(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            RevenueReportDTO report = revenueService.getRevenueReport(currentUser.getId(), startOfMonth, today);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

