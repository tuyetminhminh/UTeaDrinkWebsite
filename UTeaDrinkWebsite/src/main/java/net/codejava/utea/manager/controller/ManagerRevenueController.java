package net.codejava.utea.manager.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.manager.dto.RevenueReportDTO;
import net.codejava.utea.manager.service.RevenueReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // ==================== VIEW ENDPOINTS ====================

    /**
     * Trang dashboard doanh thu
     */
    @GetMapping
    public String revenueDashboard(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {
        
        // Mặc định: 30 ngày gần nhất
        if (fromDate == null) {
            fromDate = LocalDate.now().minusDays(30);
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
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {
        
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
            @AuthenticationPrincipal User currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        try {
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
            @AuthenticationPrincipal User currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        try {
            byte[] excelData = revenueService.exportToExcel(currentUser.getId(), fromDate, toDate);
            
            String filename = String.format("revenue_report_%s_to_%s.xlsx", fromDate, toDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Lấy báo cáo nhanh (hôm nay)
     */
    @GetMapping("/api/today")
    @ResponseBody
    public ResponseEntity<?> getTodayReport(@AuthenticationPrincipal User currentUser) {
        try {
            LocalDate today = LocalDate.now();
            RevenueReportDTO report = revenueService.getRevenueReport(currentUser.getId(), today, today);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: Lấy báo cáo tuần này
     */
    @GetMapping("/api/this-week")
    @ResponseBody
    public ResponseEntity<?> getThisWeekReport(@AuthenticationPrincipal User currentUser) {
        try {
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
    public ResponseEntity<?> getThisMonthReport(@AuthenticationPrincipal User currentUser) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            RevenueReportDTO report = revenueService.getRevenueReport(currentUser.getId(), startOfMonth, today);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

