package net.codejava.utea.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductCategory;
import net.codejava.utea.catalog.repository.ProductCategoryRepository;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.promotion.entity.Promotion;
import net.codejava.utea.promotion.entity.Voucher;
import net.codejava.utea.promotion.repository.PromotionRepository;
import net.codejava.utea.promotion.repository.VoucherRepository;
import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service cung cấp context từ database cho AI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIContextService {

    private final ProductRepository productRepo;
    private final ProductCategoryRepository categoryRepo;
    private final OrderRepository orderRepo;
    private final ReviewRepository reviewRepo;
    private final PromotionRepository promotionRepo;
    private final VoucherRepository voucherRepo;
    private final ShopRepository shopRepo;

    /**
     * Tạo system prompt với thông tin về shop
     */
    public String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý AI thông minh của UTeaDrink - cửa hàng trà sữa online.\n");
        sb.append("Bạn đang chat trực tiếp với KHÁCH HÀNG, KHÔNG phải với người huấn luyện AI.\n\n");
        
        sb.append("🚨🚨🚨 LUẬT TIẾNG VIỆT - BẮT BUỘC TUÂN THỦ 100% 🚨🚨🚨\n");
        sb.append("TRƯỚC KHI GỬI MỖI CÂU TRẢ LỜI, BẠN PHẢI:\n");
        sb.append("1. KIỂM TRA từng ký tự tiếng Việt có dấu đầy đủ và chính xác\n");
        sb.append("2. KHÔNG ĐƯỢC có ký tự lỗi như: b?n, đư?c, m?nh, đ?c, chu?n, g?i ý...\n");
        sb.append("3. CHỈ ĐƯỢC dùng chữ đúng: bạn, được, mình, đặc, chuẩn, gợi ý, rất, của, để...\n");
        sb.append("4. ĐỌC LẠI toàn bộ câu trả lời trước khi gửi để đảm bảo 100% tiếng Việt chuẩn\n");
        sb.append("5. NẾU phát hiện BẤT KỲ ký tự lỗi nào (?, �, v.v.) thì PHẢI sửa lại ngay\n\n");
        
        sb.append("⚠️ LƯU Ý QUAN TRỌNG:\n");
        sb.append("- MỖI LẦN trả lời đều phải áp dụng luật tiếng Việt\n");
        sb.append("- KHÔNG có ngoại lệ nào được phép vi phạm\n");
        sb.append("- Tiếng Việt có dấu chính xác là ưu tiên số 1\n\n");
        
        sb.append("🎯 CÁCH TRẢ LỜI KHÁCH HÀNG:\n");
        sb.append("✅ KHI KHÁCH CHÀO HỎI (xin chào, hello, hi...):\n");
        sb.append("   - Chào lại ngắn gọn, thân thiện (VD: 'Chào bạn! 😊 Mình có thể giúp gì cho bạn?')\n");
        sb.append("   - KHÔNG nói về việc đã đọc hướng dẫn, cam kết, tuân thủ...\n");
        sb.append("   - KHÔNG cảm ơn về hướng dẫn hay thông tin chi tiết gì cả\n");
        sb.append("   - Tập trung hỏi khách cần gì để hỗ trợ ngay\n\n");
        sb.append("✅ KHI KHÁCH HỎI SẢN PHẨM/DỊCH VỤ:\n");
        sb.append("   - Tư vấn cụ thể, rõ ràng với thông tin từ database\n");
        sb.append("   - Gợi ý thêm các lựa chọn phù hợp\n");
        sb.append("   - Hỏi thêm để hiểu nhu cầu khách hàng\n\n");
        
        sb.append("🚫 TUYỆT ĐỐI KHÔNG ĐƯỢC:\n");
        sb.append("- Nói 'Cảm ơn bạn đã cung cấp hướng dẫn/thông tin chi tiết...'\n");
        sb.append("- Nói 'Tôi đã đọc kỹ', 'Tôi cam kết', 'Tôi sẽ tuân thủ...'\n");
        sb.append("- Trả lời như đang chat với người train AI\n");
        sb.append("- Nhắc đến các quy định nội bộ với khách hàng\n\n");
        
        sb.append("NHIỆM VỤ CỦA BẠN:\n");
        sb.append("- Tư vấn sản phẩm: Giúp khách hàng tìm trà sữa, thức uống phù hợp\n");
        sb.append("- Giải đáp thắc mắc: Về menu, giá cả, khuyến mãi, đánh giá\n");
        sb.append("- Hỗ trợ đặt hàng: Hướng dẫn cách đặt hàng, thanh toán, giao hàng\n");
        sb.append("- Tra cứu đơn hàng: Giúp kiểm tra trạng thái đơn hàng\n");
        sb.append("- Trả lời đúng thông tin của shop: địa chỉ, số điện thoại, email, giờ hoạt động, chính sách...\n\n");
        
        sb.append("PHONG CÁCH TRẢ LỜI:\n");
        sb.append("- Thân thiện, nhiệt tình, chuyên nghiệp\n");
        sb.append("- Ngắn gọn, súc tích, dễ hiểu\n");
        sb.append("- Sử dụng emoji phù hợp (😊🍹🎉✨)\n");
        sb.append("- Luôn hỏi thêm nếu cần thông tin để tư vấn tốt hơn\n\n");
        
        sb.append("📌 NHẮC LẠI: Kiểm tra tiếng Việt có dấu đúng 100% trước khi gửi mỗi câu trả lời!\n\n");
        
        return sb.toString();
    }

    /**
     * Lấy thông tin sản phẩm theo keyword
     */
    public String getProductsContext(String keyword, int limit) {
        try {
            List<Product> products;
            if (keyword != null && !keyword.isBlank()) {
                // Search by name
                products = productRepo.findAll().stream()
                        .filter(p -> "AVAILABLE".equalsIgnoreCase(p.getStatus()))
                        .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                        .limit(limit)
                        .collect(Collectors.toList());
            } else {
                products = productRepo.findByStatus("AVAILABLE", PageRequest.of(0, limit)).getContent();
            }

            if (products.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("DANH SÁCH SẢN PHẨM:\n");
            for (Product p : products) {
                sb.append(String.format("- %s (ID: %d)\n", p.getName(), p.getId()));
                sb.append(String.format("  Giá: %,.0f đ\n", p.getBasePrice()));
                if (p.getDescription() != null && !p.getDescription().isBlank()) {
                    String desc = p.getDescription().length() > 100 
                            ? p.getDescription().substring(0, 100) + "..." 
                            : p.getDescription();
                    sb.append(String.format("  Mô tả: %s\n", desc));
                }
                // Xử lý rating_avg và sold_count an toàn
                int soldCount = p.getSoldCount() != null ? p.getSoldCount() : 0;
                double rating = p.getRatingAvg() != null ? p.getRatingAvg().doubleValue() : 0.0;
                sb.append(String.format("  Đã bán: %d | Đánh giá: %.1f⭐\n", soldCount, rating));
                sb.append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            log.error("Error getting products context: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Lấy thông tin danh mục
     */
    public String getCategoriesContext() {
        try {
            List<ProductCategory> categories = categoryRepo.findAll().stream()
                    .filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus()))
                    .collect(Collectors.toList());

            if (categories.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("DANH MỤC SẢN PHẨM:\n");
            for (ProductCategory cat : categories) {
                sb.append(String.format("- %s\n", cat.getName()));
            }
            sb.append("\n");
            return sb.toString();

        } catch (Exception e) {
            log.error("Error getting categories context: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Lấy sản phẩm bán chạy
     */
    public String getBestSellersContext(int limit) {
        try {
            List<Product> products = productRepo.findByStatus(
                    "AVAILABLE", 
                    PageRequest.of(0, limit, org.springframework.data.domain.Sort.by(
                            org.springframework.data.domain.Sort.Direction.DESC, "soldCount"
                    ))
            ).getContent();

            if (products.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("TOP SẢN PHẨM BÁN CHẠY:\n");
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                int soldCount = p.getSoldCount() != null ? p.getSoldCount() : 0;
                sb.append(String.format("%d. %s - %,.0f đ (Đã bán: %d)\n", 
                        i + 1, p.getName(), p.getBasePrice(), soldCount));
            }
            sb.append("\n");
            return sb.toString();

        } catch (Exception e) {
            log.error("Error getting best sellers context: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Lấy đánh giá của sản phẩm
     */
    public String getProductReviewsContext(Long productId, int limit) {
        try {
            List<Review> reviews = reviewRepo.findAll().stream()
                    .filter(r -> r.getProduct() != null && r.getProduct().getId().equals(productId))
                    .filter(r -> r.getStatus() == ReviewStatus.APPROVED)
                    .limit(limit)
                    .collect(Collectors.toList());

            if (reviews.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("ĐÁNH GIÁ SẢN PHẨM (ID: %d):\n", productId));
            for (Review r : reviews) {
                sb.append(String.format("- %d⭐ ", r.getRating()));
                if (r.getContent() != null && !r.getContent().isBlank()) {
                    String content = r.getContent().length() > 80 
                            ? r.getContent().substring(0, 80) + "..." 
                            : r.getContent();
                    sb.append(content);
                }
                sb.append("\n");
            }
            sb.append("\n");
            return sb.toString();

        } catch (Exception e) {
            log.error("Error getting reviews context: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Tìm đơn hàng của user
     */
    public String getUserOrdersContext(Long userId, int limit) {
        try {
            List<Order> orders = orderRepo.findByUser_Id(userId).stream()
                    .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                    .limit(limit)
                    .collect(Collectors.toList());

            if (orders.isEmpty()) {
                return "Người dùng chưa có đơn hàng nào.\n\n";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("ĐƠN HÀNG GẦN ĐÂY:\n");
            for (Order o : orders) {
                sb.append(String.format("- Mã: %s | Trạng thái: %s | Tổng: %,.0f đ\n", 
                        o.getOrderCode() != null ? o.getOrderCode() : "#" + o.getId(), 
                        o.getStatus(), 
                        o.getTotal()));
            }
            sb.append("\n");
            return sb.toString();

        } catch (Exception e) {
            log.error("Error getting user orders context: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Build context động dựa vào user message
     */
    public String buildDynamicContext(String userMessage, Long userId) {
        StringBuilder context = new StringBuilder();
        
        // Always include system prompt
        context.append(buildSystemPrompt());

        String lowerMsg = userMessage.toLowerCase();

        // Nếu hỏi về địa chỉ, thông tin shop
        if (containsAny(lowerMsg, "địa chỉ", "ở đâu", "cửa hàng", "shop", "liên hệ", "điện thoại", "số điện thoại")) {
            context.append(getShopsContext());
        }

        // Nếu hỏi về khuyến mãi, giảm giá
        if (containsAny(lowerMsg, "khuyến mãi", "giảm giá", "sale", "promotion", "ưu đãi", "voucher", "mã giảm")) {
            context.append(getActivePromotionsContext(5));
            context.append(getActiveVouchersContext(5));
        }

        // Nếu hỏi về sản phẩm, danh mục
        if (containsAny(lowerMsg, "sản phẩm", "trà", "nước", "thức uống", "menu", "gì", "có")) {
            context.append(getCategoriesContext());
            context.append(getBestSellersContext(5));
        }

        // Nếu hỏi về đơn hàng
        if (userId != null && containsAny(lowerMsg, "đơn hàng", "order", "mua", "đặt")) {
            context.append(getUserOrdersContext(userId, 3));
        }

        // Nếu hỏi cụ thể về một sản phẩm (có keyword)
        String[] keywords = {"trà sữa", "trà đào", "matcha", "cà phê", "sinh tố", "yogurt"};
        for (String keyword : keywords) {
            if (lowerMsg.contains(keyword)) {
                context.append(getProductsContext(keyword, 3));
                break;
            }
        }

        return context.toString();
    }

    /**
     * Lấy thông tin các shop
     */
    public String getShopsContext() {
        try {
            List<Shop> shops = shopRepo.findAll().stream()
                    .filter(s -> "OPEN".equalsIgnoreCase(s.getStatus()))
                    .collect(Collectors.toList());

            if (shops.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("THÔNG TIN CỬA HÀNG UTEADRINK:\n");
            for (Shop shop : shops) {
                sb.append(String.format("📍 %s\n", shop.getName()));
                if (shop.getAddress() != null && !shop.getAddress().isBlank()) {
                    sb.append(String.format("   Địa chỉ: %s\n", shop.getAddress()));
                }
                if (shop.getPhone() != null && !shop.getPhone().isBlank()) {
                    sb.append(String.format("   Điện thoại: %s\n", shop.getPhone()));
                }
                sb.append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            log.error("Error getting shops context: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Lấy khuyến mãi đang hoạt động
     */
    public String getActivePromotionsContext(int limit) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Promotion> promotions = promotionRepo.findAll().stream()
                    .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                    .filter(p -> p.getActiveFrom() == null || p.getActiveFrom().isBefore(now))
                    .filter(p -> p.getActiveTo() == null || p.getActiveTo().isAfter(now))
                    .limit(limit)
                    .collect(Collectors.toList());

            if (promotions.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🎉 CHƯƠNG TRÌNH KHUYẾN MÃI ĐANG DIỄN RA:\n");
            for (Promotion promo : promotions) {
                if (promo.getTitle() != null && !promo.getTitle().isBlank()) {
                    sb.append(String.format("- %s\n", promo.getTitle()));
                }
                if (promo.getDescription() != null && !promo.getDescription().isBlank()) {
                    String desc = promo.getDescription().length() > 100 
                            ? promo.getDescription().substring(0, 100) + "..." 
                            : promo.getDescription();
                    sb.append(String.format("  %s\n", desc));
                }
                if (promo.getActiveTo() != null) {
                    sb.append(String.format("  Có hiệu lực đến: %s\n", 
                            promo.getActiveTo().toLocalDate()));
                }
                sb.append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            log.error("Error getting promotions context: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Lấy voucher đang khả dụng
     */
    public String getActiveVouchersContext(int limit) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Voucher> vouchers = voucherRepo.findAll().stream()
                    .filter(v -> "ACTIVE".equalsIgnoreCase(v.getStatus()))
                    .filter(v -> v.getActiveFrom() == null || v.getActiveFrom().isBefore(now))
                    .filter(v -> v.getActiveTo() == null || v.getActiveTo().isAfter(now))
                    .filter(v -> v.getUsageLimit() == null || v.getUsedCount() < v.getUsageLimit())
                    .limit(limit)
                    .collect(Collectors.toList());

            if (vouchers.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🎫 MÃ GIẢM GIÁ KHẢ DỤNG:\n");
            for (Voucher voucher : vouchers) {
                sb.append(String.format("- Mã: %s\n", voucher.getCode()));
                
                if (voucher.getForFirstOrder() != null && voucher.getForFirstOrder()) {
                    sb.append("  Dành cho đơn đầu tiên\n");
                }
                if (voucher.getForBirthday() != null && voucher.getForBirthday()) {
                    sb.append("  Dành cho sinh nhật\n");
                }
                if (voucher.getActiveTo() != null) {
                    sb.append(String.format("  HSD: %s\n", voucher.getActiveTo().toLocalDate()));
                }
                if (voucher.getUsageLimit() != null) {
                    int remaining = voucher.getUsageLimit() - (voucher.getUsedCount() != null ? voucher.getUsedCount() : 0);
                    sb.append(String.format("  Còn lại: %d lượt\n", remaining));
                }
                sb.append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            log.error("Error getting vouchers context: {}", e.getMessage());
            return "";
        }
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

