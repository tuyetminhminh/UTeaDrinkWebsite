package net.codejava.utea.manager.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.service.EmailService;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.promotion.entity.Voucher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherEmailService {

    private final EmailService emailService;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;

    /**
     * Gửi email voucher cho danh sách khách hàng phù hợp điều kiện
     */
    @Transactional(readOnly = true)
    public int broadcastVoucher(Voucher voucher, Shop shop) {
        List<String> eligibleEmails;
        
        System.out.println("🎯 [VoucherEmailService] Bắt đầu broadcast voucher: " + voucher.getCode());
        System.out.println("   Shop: " + shop.getName() + " (ID: " + shop.getId() + ")");
        System.out.println("   ForFirstOrder: " + voucher.getForFirstOrder());
        System.out.println("   ForBirthday: " + voucher.getForBirthday());
        
        // Kiểm tra điều kiện đặc biệt
        if (Boolean.TRUE.equals(voucher.getForFirstOrder())) {
            // Chỉ gửi cho khách hàng chưa có đơn hàng NÀO Ở SHOP NÀY
            eligibleEmails = getEligibleCustomersForFirstOrder(shop);
            System.out.println("   → Tìm thấy " + eligibleEmails.size() + " khách hàng chưa có đơn đầu tiên ở shop này");
        } else if (Boolean.TRUE.equals(voucher.getForBirthday())) {
            // Chỉ gửi cho khách hàng có sinh nhật trong tháng (TODO: cần thêm birthday field)
            eligibleEmails = getEligibleCustomersForBirthday(shop);
            System.out.println("   → Tìm thấy " + eligibleEmails.size() + " khách hàng sinh nhật");
        } else {
            // Voucher thường: gửi cho tất cả khách hàng đã từng đặt hàng ở shop
            eligibleEmails = getAllCustomersOfShop(shop);
            System.out.println("   → Tìm thấy " + eligibleEmails.size() + " khách hàng của shop");
        }

        // Gửi email cho từng khách hàng
        int sentCount = 0;
        for (String email : eligibleEmails) {
            try {
                sendVoucherEmail(email, voucher, shop);
                sentCount++;
                System.out.println("   ✓ Đã gửi cho: " + email);
            } catch (Exception e) {
                // Log error but continue sending to others
                System.err.println("   ✗ Lỗi gửi cho " + email + ": " + e.getMessage());
            }
        }

        System.out.println("🎉 [VoucherEmailService] Hoàn thành: Đã gửi " + sentCount + "/" + eligibleEmails.size() + " email");
        return sentCount;
    }

    /**
     * ✅ Lấy danh sách email khách hàng chưa có đơn hàng NÀO Ở SHOP NÀY
     * (Không phải chưa có đơn hàng ở tất cả shop)
     */
    private List<String> getEligibleCustomersForFirstOrder(Shop shop) {
        // Lấy tất cả user có role CUSTOMER
        List<User> allCustomers = userRepo.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> "CUSTOMER".equals(r.getCode())))
                .collect(Collectors.toList());

        // Lấy danh sách customer đã từng đặt hàng ở shop này
        List<Order> shopOrders = orderRepo.findByShop_Id(shop.getId());
        Set<Long> customerIdsWithOrders = shopOrders.stream()
                .map(order -> order.getUser().getId())
                .collect(Collectors.toSet());

        // ✅ Lọc ra những customer CHƯA có đơn hàng NÀO ở shop này
        return allCustomers.stream()
                .filter(customer -> !customerIdsWithOrders.contains(customer.getId()))
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách email khách hàng có sinh nhật trong tháng
     * ⚠️ TODO: Cần thêm birthday field vào User entity
     * Hiện tại: Tạm gửi cho TẤT CẢ khách hàng của shop (fallback)
     */
    private List<String> getEligibleCustomersForBirthday(Shop shop) {
        System.out.println("⚠️ [Birthday Voucher] Birthday field chưa được implement trong User entity");
        System.out.println("   → Tạm thời gửi cho TẤT CẢ khách hàng của shop (fallback)");
        
        // ⚠️ Fallback: Gửi cho tất cả khách hàng của shop
        // TODO: Sau này sửa logic để check sinh nhật thực sự
        return getAllCustomersOfShop(shop);
    }

    /**
     * Lấy tất cả email khách hàng đã từng đặt hàng ở shop
     */
    private List<String> getAllCustomersOfShop(Shop shop) {
        // Lấy tất cả đơn hàng của shop
        List<Order> orders = orderRepo.findByShop_Id(shop.getId());
        System.out.println("   [Debug] Tìm thấy " + orders.size() + " đơn hàng của shop ID: " + shop.getId());
        
        // Lấy danh sách email unique của khách hàng
        List<String> emails = orders.stream()
                .map(order -> order.getUser().getEmail())
                .distinct()
                .collect(Collectors.toList());
        
        System.out.println("   [Debug] Unique emails: " + emails);
        return emails;
    }

    /**
     * Gửi email voucher cho 1 khách hàng
     */
    private void sendVoucherEmail(String toEmail, Voucher voucher, Shop shop) {
        String subject = "🎁 " + shop.getName() + " - Mã giảm giá đặc biệt dành cho bạn!";
        
        String html = buildVoucherEmailHtml(toEmail, voucher, shop);
        
        emailService.send(toEmail, subject, html);
    }

    /**
     * Tạo HTML template cho email voucher
     */
    private String buildVoucherEmailHtml(String toEmail, Voucher voucher, Shop shop) {
        // Parse rule JSON
        String ruleJson = voucher.getRuleJson();
        int percentOff = extractPercentOff(ruleJson);
        long minTotal = extractMinTotal(ruleJson);
        long amountCap = extractAmountCap(ruleJson);

        String voucherType = "";
        if (Boolean.TRUE.equals(voucher.getForFirstOrder())) {
            voucherType = "<span style='background: linear-gradient(135deg, #fef3c7 0%%, #fde68a 100%%); color: #92400e; padding: 5px 15px; border-radius: 20px; font-size: 12px; font-weight: 700;'>🎉 ĐƠN ĐẦU TIÊN</span>";
        } else if (Boolean.TRUE.equals(voucher.getForBirthday())) {
            voucherType = "<span style='background: linear-gradient(135deg, #fce7f3 0%%, #fbcfe8 100%%); color: #831843; padding: 5px 15px; border-radius: 20px; font-size: 12px; font-weight: 700;'>🎂 SINH NHẬT</span>";
        }

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { font-family: 'Arial', sans-serif; background: #f5f7fa; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 10px 40px rgba(0,0,0,0.1); }
        .header { background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); padding: 40px 30px; text-align: center; }
        .header h1 { color: white; margin: 0; font-size: 28px; }
        .header p { color: rgba(255,255,255,0.8); margin: 10px 0 0; font-size: 14px; }
        .content { padding: 40px 30px; }
        .voucher-box { background: linear-gradient(135deg, #dbeafe 0%%, #bfdbfe 100%%); border: 2px dashed #0891b2; border-radius: 15px; padding: 30px; text-align: center; margin: 30px 0; }
        .voucher-code { font-family: 'Courier New', monospace; font-size: 36px; font-weight: 800; color: #0f172a; letter-spacing: 4px; margin: 15px 0; }
        .voucher-details { background: white; border-radius: 10px; padding: 20px; margin: 20px 0; }
        .detail-item { display: flex; align-items: center; margin: 10px 0; }
        .detail-icon { color: #0891b2; margin-right: 10px; font-size: 18px; }
        .cta-button { display: inline-block; background: linear-gradient(135deg, #0f172a 0%%, #1e293b 100%%); color: white; padding: 15px 40px; border-radius: 10px; text-decoration: none; font-weight: 700; margin: 20px 0; }
        .footer { background: #f8fafc; padding: 30px; text-align: center; color: #64748b; font-size: 13px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎁 Mã Giảm Giá Đặc Biệt</h1>
            <p>%s</p>
        </div>
        
        <div class="content">
            <p style="font-size: 16px; color: #1e293b;">Xin chào,</p>
            <p style="font-size: 16px; color: #64748b; line-height: 1.6;">
                Chúng tôi xin gửi tặng bạn mã giảm giá đặc biệt để sử dụng cho đơn hàng tiếp theo tại <strong>%s</strong>!
            </p>
            
            %s
            
            <div class="voucher-box">
                <div class="voucher-code">%s</div>
                <p style="color: #64748b; margin: 10px 0;">Sao chép mã này để sử dụng khi thanh toán</p>
            </div>
            
            <div class="voucher-details">
                <h3 style="color: #0f172a; margin-top: 0;">📋 Chi tiết ưu đãi:</h3>
                <div class="detail-item">
                    <span class="detail-icon">💰</span>
                    <span style="color: #1e293b;">Giảm <strong>%d%%%%</strong> giá trị đơn hàng</span>
                </div>
                %s
                %s
                <div class="detail-item">
                    <span class="detail-icon">⏰</span>
                    <span style="color: #1e293b;">Có hiệu lực đến: <strong>%s</strong></span>
                </div>
                <div class="detail-item">
                    <span class="detail-icon">🔢</span>
                    <span style="color: #1e293b;">Còn lại: <strong>%d/%d</strong> lượt sử dụng</span>
                </div>
            </div>
            
            <div style="text-align: center;">
                <a href="http://localhost:8080/customer/menu" class="cta-button">
                    🛒 Đặt hàng ngay
                </a>
            </div>
            
            <p style="color: #94a3b8; font-size: 13px; margin-top: 30px; line-height: 1.6;">
                <strong>Lưu ý:</strong> Mã giảm giá chỉ áp dụng một lần cho mỗi đơn hàng. Vui lòng nhập mã tại trang thanh toán để được giảm giá.
            </p>
        </div>
        
        <div class="footer">
            <p><strong>%s</strong></p>
            <p>Email này được gửi tự động, vui lòng không trả lời.</p>
            <p style="margin-top: 15px;">
                <a href="#" style="color: #0891b2; text-decoration: none; margin: 0 10px;">Chính sách</a> |
                <a href="#" style="color: #0891b2; text-decoration: none; margin: 0 10px;">Liên hệ</a> |
                <a href="#" style="color: #0891b2; text-decoration: none; margin: 0 10px;">Hủy đăng ký</a>
            </p>
        </div>
    </div>
</body>
</html>
                """.formatted(
                shop.getName(),
                shop.getName(),
                voucherType,
                voucher.getCode(),
                percentOff,
                minTotal > 0 ? String.format(
                        "<div class=\"detail-item\"><span class=\"detail-icon\">📦</span><span style=\"color: #1e293b;\">Đơn tối thiểu: <strong>%,d đ</strong></span></div>",
                        minTotal
                ) : "",
                amountCap > 0 ? String.format(
                        "<div class=\"detail-item\"><span class=\"detail-icon\">🎯</span><span style=\"color: #1e293b;\">Giảm tối đa: <strong>%,d đ</strong></span></div>",
                        amountCap
                ) : "",
                voucher.getActiveTo() != null ? voucher.getActiveTo().toString().substring(0, 10) : "Không giới hạn",
                (voucher.getUsageLimit() != null ? voucher.getUsageLimit() - (voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) : 999),
                voucher.getUsageLimit() != null ? voucher.getUsageLimit() : 999,
                shop.getName()
        );
    }

    // Helper methods to parse rule JSON
    private int extractPercentOff(String ruleJson) {
        try {
            if (ruleJson != null && ruleJson.contains("percentOff")) {
                String value = ruleJson.split("percentOff\":")[1].split("[,}]")[0].trim();
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

    private long extractMinTotal(String ruleJson) {
        try {
            if (ruleJson != null && ruleJson.contains("minTotal")) {
                String value = ruleJson.split("minTotal\":")[1].split("[,}]")[0].trim();
                return Long.parseLong(value);
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

    private long extractAmountCap(String ruleJson) {
        try {
            if (ruleJson != null && ruleJson.contains("amountCap")) {
                String value = ruleJson.split("amountCap\":")[1].split("[,}]")[0].trim();
                return Long.parseLong(value);
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }
}

