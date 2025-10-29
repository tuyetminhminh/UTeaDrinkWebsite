package net.codejava.utea.admin.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.admin.dto.ShopRevenueSummaryDTO;
import net.codejava.utea.admin.dto.SystemRevenueReportDTO;
import net.codejava.utea.manager.dto.DailyRevenueDTO;
import net.codejava.utea.manager.dto.RevenueReportDTO;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.manager.service.RevenueReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final RevenueReportService revenueReportService; // Dùng lại service của Manager
    private final ShopRepository shopRepo;
    private final ShopManagerRepository shopManagerRepo;

    public SystemRevenueReportDTO getSystemRevenueReport(LocalDate fromDate, LocalDate toDate) {
        List<Shop> allShops = shopRepo.findAll();
        List<ShopRevenueSummaryDTO> shopSummaries = new ArrayList<>();
        Map<LocalDate, DailyRevenueDTO> aggregatedDailyRevenue = new TreeMap<>();

        for (Shop shop : allShops) {
            // Tìm một manager của shop để lấy báo cáo
            shopManagerRepo.findFirstByShop_Id(shop.getId()).ifPresent(manager -> {
                RevenueReportDTO shopReport = revenueReportService.getRevenueReport(manager.getManager().getId(), fromDate, toDate);

                // 1. Tạo bản tóm tắt cho từng shop
                ShopRevenueSummaryDTO summary = ShopRevenueSummaryDTO.builder()
                        .shopId(shop.getId())
                        .shopName(shop.getName())
                        .totalRevenue(shopReport.getTotalRevenue())
                        .totalOrders(shopReport.getTotalOrders())
                        .averageOrderValue(shopReport.getAverageOrderValue())
                        .build();
                shopSummaries.add(summary);

                // 2. Tổng hợp doanh thu theo ngày
                shopReport.getDailyRevenue().forEach(daily -> {
                    aggregatedDailyRevenue.compute(daily.getDate(), (date, existing) -> {
                        if (existing == null) {
                            return DailyRevenueDTO.builder().date(date).revenue(daily.getRevenue()).orderCount(daily.getOrderCount()).build();
                        }
                        existing.setRevenue(existing.getRevenue().add(daily.getRevenue()));
                        existing.setOrderCount(existing.getOrderCount() + daily.getOrderCount());
                        return existing;
                    });
                });
            });
        }

        // 3. Tính toán các chỉ số tổng hợp toàn hệ thống từ shopSummaries
        BigDecimal totalSystemRevenue = shopSummaries.stream().map(ShopRevenueSummaryDTO::totalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalSystemOrders = shopSummaries.stream().mapToInt(ShopRevenueSummaryDTO::totalOrders).sum();

        // Sắp xếp doanh thu theo ngày
        List<DailyRevenueDTO> dailyRevenueList = new ArrayList<>(aggregatedDailyRevenue.values());

        return SystemRevenueReportDTO.builder()
                .fromDate(fromDate).toDate(toDate)
                .totalSystemRevenue(totalSystemRevenue)
                .totalSystemOrders(totalSystemOrders)
                .shopSummaries(shopSummaries)
                .dailyRevenue(dailyRevenueList)
                .build();
    }
}