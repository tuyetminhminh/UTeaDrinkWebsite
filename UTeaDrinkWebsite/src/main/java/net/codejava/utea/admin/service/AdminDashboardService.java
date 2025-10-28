package net.codejava.utea.admin.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.admin.dto.AdminDashboardStatsDTO;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.order.dto.RecentOrderDTO;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    
    public AdminDashboardStatsDTO getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59);
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);

        // KPI data
        BigDecimal monthlyRevenue = orderRepo.sumRevenueBetween(startOfMonth, endOfMonth);
        long monthlyOrders = orderRepo.countOrdersBetween(startOfMonth, endOfMonth);
        long newUsers = userRepo.countByCreatedAtBetween(startOfMonth, endOfMonth);
        long activeProducts = productRepo.countByStatus("ACTIVE");
        
        // Recent orders
        List<RecentOrderDTO> recentOrders = orderRepo.findRecentOrders(PageRequest.of(0, 5));
        
        // Chart data (last 30 days)
        // 1. Tạo danh sách các trạng thái mong muốn
        List<OrderStatus> statuses = List.of(OrderStatus.DELIVERED, OrderStatus.CONFIRMED, OrderStatus.DELIVERING);

        // 2. Truyền danh sách này vào phương thức repository
        List<Order> completedOrders = orderRepo.findCompletedOrdersSince(last30Days, statuses);

        Map<LocalDate, BigDecimal> revenueMap = completedOrders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getCreatedAt().toLocalDate(), // Nhóm các đơn hàng theo ngày tạo
                Collectors.reducing(BigDecimal.ZERO, Order::getTotal, BigDecimal::add) // Tính tổng 'total' cho mỗi nhóm
            ));

        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartData = new ArrayList<>();

        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            chartLabels.add(date.format(DateTimeFormatter.ofPattern("dd/MM")));
            chartData.add(revenueMap.getOrDefault(date, BigDecimal.ZERO));
        }

        return AdminDashboardStatsDTO.builder()
            .monthlyRevenue(monthlyRevenue)
            .monthlyOrders(monthlyOrders)
            .newUsersThisMonth(newUsers)
            .activeProducts(activeProducts)
            .recentOrders(recentOrders)
            .chartLabels(chartLabels)
            .chartData(chartData)
            .build();
    }
}