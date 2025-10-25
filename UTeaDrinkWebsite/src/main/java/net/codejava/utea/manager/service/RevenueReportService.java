package net.codejava.utea.manager.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.dto.*;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.OrderItem;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueReportService {

    private final OrderRepository orderRepo;
    private final ShopManagerRepository shopManagerRepo;

    // ==================== REVENUE REPORT ====================

    /**
     * Lấy báo cáo doanh thu theo khoảng thời gian
     * Doanh thu tính theo ngày đơn hàng được GIAO (DELIVERED), không phải ngày tạo đơn
     */
    @Transactional(readOnly = true)
    public RevenueReportDTO getRevenueReport(Long managerId, LocalDate fromDate, LocalDate toDate) {
        Shop shop = getShopByManagerId(managerId);
        
        // Compact logging - only summary
        boolean isVerbose = false; // Set to true for detailed debugging
        
        if (isVerbose) {
            System.out.println("\n=== getRevenueReport: " + fromDate + " to " + toDate + " ===");
        }

        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX);

        // Lấy tất cả đơn hàng của shop
        List<Order> allShopOrders = orderRepo.findAll().stream()
                .filter(o -> o.getShop().getId().equals(shop.getId()))
                .collect(Collectors.toList());

        // Lọc đơn hàng ĐÃ GIAO trong khoảng thời gian
        // Doanh thu chỉ tính từ đơn hàng DELIVERED
        List<Order> completedOrders = allShopOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> {
                    // Tìm thời điểm chuyển sang DELIVERED từ lịch sử
                    LocalDateTime deliveredAt = getDeliveredTime(o);
                    // Nếu không có lịch sử, dùng updatedAt
                    if (deliveredAt == null) {
                        deliveredAt = o.getUpdatedAt() != null ? o.getUpdatedAt() : o.getCreatedAt();
                    }
                    
                    // Sử dụng !isBefore và !isAfter để bao gồm cả boundary dates
                    return !deliveredAt.isBefore(startDateTime) && !deliveredAt.isAfter(endDateTime);
                })
                .collect(Collectors.toList());

        // Lấy tất cả đơn hàng TẠO trong khoảng thời gian (để tính tổng đơn, tỷ lệ hủy)
        List<Order> orders = allShopOrders.stream()
                .filter(o -> {
                    LocalDateTime createdAt = o.getCreatedAt();
                    return !createdAt.isBefore(startDateTime) && !createdAt.isAfter(endDateTime);
                })
                .collect(Collectors.toList());

        // Lọc đơn hủy từ orders
        List<Order> canceledOrders = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELED)
                .collect(Collectors.toList());

        // Tính doanh thu từ các đơn ĐÃ GIAO trong khoảng thời gian
        BigDecimal totalRevenue = completedOrders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalOrders = orders.size();
        Integer completedCount = completedOrders.size();
        Integer canceledCount = canceledOrders.size();
        
        if (isVerbose) {
            System.out.println("  Completed: " + completedCount + ", Revenue: " + totalRevenue.longValue() + " VND");
        }

        Double cancelRate = totalOrders > 0 
                ? (canceledCount.doubleValue() / totalOrders.doubleValue()) * 100 
                : 0.0;

        BigDecimal averageOrderValue = completedCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Top products
        List<TopProductDTO> topProducts = getTopProducts(completedOrders);

        // Daily revenue
        List<DailyRevenueDTO> dailyRevenue = getDailyRevenue(completedOrders, fromDate, toDate);

        // Hourly revenue
        List<HourlyRevenueDTO> hourlyRevenue = getHourlyRevenue(completedOrders);

        return RevenueReportDTO.builder()
                .shopId(shop.getId())
                .shopName(shop.getName())
                .fromDate(fromDate)
                .toDate(toDate)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .completedOrders(completedCount)
                .canceledOrders(canceledCount)
                .cancelRate(cancelRate)
                .averageOrderValue(averageOrderValue)
                .topProducts(topProducts)
                .dailyRevenue(dailyRevenue)
                .hourlyRevenue(hourlyRevenue)
                .build();
    }

    /**
     * Lấy top sản phẩm bán chạy
     */
    private List<TopProductDTO> getTopProducts(List<Order> orders) {
        Map<Long, TopProductDTO> productMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                Long productId = item.getProduct().getId();
                
                TopProductDTO dto = productMap.getOrDefault(productId, TopProductDTO.builder()
                        .productId(productId)
                        .productName(item.getProduct().getName())
                        .totalSold(0)
                        .totalRevenue(BigDecimal.ZERO)
                        .build());

                dto.setTotalSold(dto.getTotalSold() + item.getQuantity());
                dto.setTotalRevenue(dto.getTotalRevenue().add(item.getLineTotal()));

                productMap.put(productId, dto);
            }
        }

        return productMap.values().stream()
                .sorted((a, b) -> b.getTotalSold().compareTo(a.getTotalSold()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Lấy doanh thu theo ngày
     * orders đã truyền vào là danh sách DELIVERED orders, lọc theo delivery date
     */
    private List<DailyRevenueDTO> getDailyRevenue(List<Order> orders, LocalDate fromDate, LocalDate toDate) {
        Map<LocalDate, DailyRevenueDTO> dailyMap = new HashMap<>();

        // Khởi tạo tất cả các ngày trong khoảng
        LocalDate date = fromDate;
        while (!date.isAfter(toDate)) {
            dailyMap.put(date, DailyRevenueDTO.builder()
                    .date(date)
                    .revenue(BigDecimal.ZERO)
                    .orderCount(0)
                    .build());
            date = date.plusDays(1);
        }

        // Tính doanh thu cho mỗi ngày (theo ngày GIAO, không phải ngày tạo)
        for (Order order : orders) {
            LocalDateTime deliveredAt = getDeliveredTime(order);
            if (deliveredAt == null) {
                deliveredAt = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
            }
            
            LocalDate orderDate = deliveredAt.toLocalDate();
            DailyRevenueDTO dto = dailyMap.get(orderDate);
            if (dto != null) {
                dto.setRevenue(dto.getRevenue().add(order.getTotal()));
                dto.setOrderCount(dto.getOrderCount() + 1);
            }
        }

        return dailyMap.values().stream()
                .sorted(Comparator.comparing(DailyRevenueDTO::getDate))
                .collect(Collectors.toList());
    }

    /**
     * Lấy doanh thu theo giờ (theo giờ GIAO, không phải giờ tạo đơn)
     */
    private List<HourlyRevenueDTO> getHourlyRevenue(List<Order> orders) {
        Map<Integer, HourlyRevenueDTO> hourlyMap = new HashMap<>();

        // Khởi tạo tất cả các giờ (0-23)
        for (int hour = 0; hour < 24; hour++) {
            hourlyMap.put(hour, HourlyRevenueDTO.builder()
                    .hour(hour)
                    .revenue(BigDecimal.ZERO)
                    .orderCount(0)
                    .build());
        }

        // Tính doanh thu cho mỗi giờ (theo giờ GIAO)
        for (Order order : orders) {
            LocalDateTime deliveredAt = getDeliveredTime(order);
            if (deliveredAt == null) {
                deliveredAt = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
            }
            
            int hour = deliveredAt.getHour();
            HourlyRevenueDTO dto = hourlyMap.get(hour);
            dto.setRevenue(dto.getRevenue().add(order.getTotal()));
            dto.setOrderCount(dto.getOrderCount() + 1);
        }

        return hourlyMap.values().stream()
                .sorted(Comparator.comparing(HourlyRevenueDTO::getHour))
                .collect(Collectors.toList());
    }

    /**
     * Xuất báo cáo Excel
     */
    @Transactional(readOnly = true)
    public byte[] exportToExcel(Long managerId, LocalDate fromDate, LocalDate toDate) throws IOException {
        RevenueReportDTO report = getRevenueReport(managerId, fromDate, toDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            // Tạo sheet tổng quan
            Sheet summarySheet = workbook.createSheet("Tổng quan");
            createSummarySheet(summarySheet, report);

            // Tạo sheet top sản phẩm
            Sheet productsSheet = workbook.createSheet("Top sản phẩm");
            createProductsSheet(productsSheet, report);

            // Tạo sheet doanh thu theo ngày
            Sheet dailySheet = workbook.createSheet("Doanh thu theo ngày");
            createDailySheet(dailySheet, report);

            // Tạo sheet doanh thu theo giờ
            Sheet hourlySheet = workbook.createSheet("Doanh thu theo giờ");
            createHourlySheet(hourlySheet, report);

            // Ghi ra byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ==================== EXCEL HELPER METHODS ====================

    private void createSummarySheet(Sheet sheet, RevenueReportDTO report) {
        int rowNum = 0;

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("BÁO CÁO DOANH THU - " + report.getShopName());
        rowNum++;

        // Period
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue("Từ ngày:");
        periodRow.createCell(1).setCellValue(report.getFromDate().toString());
        periodRow.createCell(2).setCellValue("Đến ngày:");
        periodRow.createCell(3).setCellValue(report.getToDate().toString());
        rowNum++;

        // Summary data
        createDataRow(sheet, rowNum++, "Tổng doanh thu:", report.getTotalRevenue().toString() + " VNĐ");
        createDataRow(sheet, rowNum++, "Tổng đơn hàng:", report.getTotalOrders().toString());
        createDataRow(sheet, rowNum++, "Đơn hoàn thành:", report.getCompletedOrders().toString());
        createDataRow(sheet, rowNum++, "Đơn hủy:", report.getCanceledOrders().toString());
        createDataRow(sheet, rowNum++, "Tỷ lệ hủy:", String.format("%.2f%%", report.getCancelRate()));
        createDataRow(sheet, rowNum++, "Giá trị trung bình:", report.getAverageOrderValue().toString() + " VNĐ");

        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createProductsSheet(Sheet sheet, RevenueReportDTO report) {
        int rowNum = 0;

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("STT");
        headerRow.createCell(1).setCellValue("Tên sản phẩm");
        headerRow.createCell(2).setCellValue("Số lượng bán");
        headerRow.createCell(3).setCellValue("Doanh thu");

        // Data rows
        int stt = 1;
        for (TopProductDTO product : report.getTopProducts()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue(product.getProductName());
            row.createCell(2).setCellValue(product.getTotalSold());
            row.createCell(3).setCellValue(product.getTotalRevenue().toString() + " VNĐ");
        }

        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDailySheet(Sheet sheet, RevenueReportDTO report) {
        int rowNum = 0;

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Ngày");
        headerRow.createCell(1).setCellValue("Doanh thu");
        headerRow.createCell(2).setCellValue("Số đơn");

        // Data rows
        for (DailyRevenueDTO daily : report.getDailyRevenue()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(daily.getDate().toString());
            row.createCell(1).setCellValue(daily.getRevenue().toString() + " VNĐ");
            row.createCell(2).setCellValue(daily.getOrderCount());
        }

        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createHourlySheet(Sheet sheet, RevenueReportDTO report) {
        int rowNum = 0;

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Giờ");
        headerRow.createCell(1).setCellValue("Doanh thu");
        headerRow.createCell(2).setCellValue("Số đơn");

        // Data rows
        for (HourlyRevenueDTO hourly : report.getHourlyRevenue()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(hourly.getHour() + ":00");
            row.createCell(1).setCellValue(hourly.getRevenue().toString() + " VNĐ");
            row.createCell(2).setCellValue(hourly.getOrderCount());
        }

        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDataRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    // ==================== HELPER METHODS ====================

    private Shop getShopByManagerId(Long managerId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));
        return shopManager.getShop();
    }

    /**
     * Lấy thời điểm đơn hàng chuyển sang DELIVERED từ lịch sử trạng thái
     */
    private LocalDateTime getDeliveredTime(Order order) {
        try {
            if (order.getStatusHistories() == null || order.getStatusHistories().isEmpty()) {
                return null;
            }
            
            return order.getStatusHistories().stream()
                    .filter(h -> h.getStatus() == OrderStatus.DELIVERED)
                    .map(h -> h.getChangedAt())
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            // Silently handle LazyInitializationException or other errors
            return null;
        }
    }
}

