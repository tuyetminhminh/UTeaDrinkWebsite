package net.codejava.utea.auth.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.service.EmailService;
import net.codejava.utea.auth.service.OtpService;
import net.codejava.utea.common.entity.PasswordResetOtp;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.PasswordResetOtpRepository;
import net.codejava.utea.common.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void sendResetOtpToEmail(String email) {
        // Tìm user theo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        // Tạo mã OTP 6 số
        String otpCode = generateOtpCode();

        // Tạo thời hạn: 5 phút
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        // Lưu OTP vào database
        PasswordResetOtp otp = PasswordResetOtp.builder()
                .user(user)
                .code(otpCode)
                .expiresAt(expiresAt)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        otpRepository.save(otp);

        // Tạo nội dung email HTML
        String emailContent = buildOtpEmailHtml(user.getFullName(), otpCode);

        // Gửi email
        try {
            emailService.send(email, "🔑 Mã OTP đặt lại mật khẩu - UTeaDrink", emailContent);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.", e);
        }
    }

    @Override
    @Transactional
    public void resetPasswordByEmail(String email, String otpCode, String newPassword) {
        // Tìm user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // Tìm OTP hợp lệ (chưa sử dụng, chưa hết hạn)
        PasswordResetOtp otp = otpRepository
                .findTopByUserAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(user, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn"));

        // Kiểm tra mã OTP có đúng không
        if (!otp.getCode().equals(otpCode.trim())) {
            throw new RuntimeException("Mã OTP không chính xác");
        }

        // Đánh dấu OTP đã sử dụng
        otp.setUsed(true);
        otpRepository.save(otp);

        // Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Tạo mã OTP 6 số ngẫu nhiên
     */
    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6 chữ số
        return String.valueOf(otp);
    }

    /**
     * Tạo nội dung email HTML đẹp mắt
     */
    private String buildOtpEmailHtml(String fullName, String otpCode) {
        String name = (fullName != null && !fullName.isEmpty()) ? fullName : "Quý khách";
        
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<style>" +
            "body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }" +
            ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }" +
            ".header { background: linear-gradient(135deg, #FF6B35 0%, #F7931E 100%); padding: 30px; text-align: center; color: white; }" +
            ".header h1 { margin: 0; font-size: 28px; }" +
            ".content { padding: 40px 30px; }" +
            ".otp-box { background: linear-gradient(135deg, #FFF5E6 0%, #FFE8CC 100%); border: 2px dashed #FF6B35; border-radius: 10px; padding: 30px; text-align: center; margin: 25px 0; }" +
            ".otp-code { font-size: 42px; font-weight: bold; color: #FF6B35; letter-spacing: 8px; margin: 10px 0; }" +
            ".info { background: #f8f9fa; border-left: 4px solid #FF6B35; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
            ".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class=\"container\">" +
            "<div class=\"header\">" +
            "<h1>🍹 UTeaDrink</h1>" +
            "<p style=\"margin: 10px 0 0 0;\">Đặt lại mật khẩu</p>" +
            "</div>" +
            "<div class=\"content\">" +
            "<h2 style=\"color: #333; margin-top: 0;\">Xin chào " + name + "! 👋</h2>" +
            "<p style=\"color: #666; line-height: 1.6;\">" +
            "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. " +
            "Vui lòng sử dụng mã OTP dưới đây để tiếp tục:" +
            "</p>" +
            "<div class=\"otp-box\">" +
            "<p style=\"margin: 0; color: #666; font-size: 14px;\">MÃ OTP CỦA BẠN</p>" +
            "<div class=\"otp-code\">" + otpCode + "</div>" +
            "<p style=\"margin: 0; color: #999; font-size: 13px;\">Mã có hiệu lực trong 5 phút</p>" +
            "</div>" +
            "<div class=\"info\">" +
            "<strong>⚠️ Lưu ý quan trọng:</strong>" +
            "<ul style=\"margin: 10px 0; padding-left: 20px; color: #666;\">" +
            "<li>Mã OTP chỉ có hiệu lực trong <strong>5 phút</strong></li>" +
            "<li>Không chia sẻ mã này với bất kỳ ai</li>" +
            "<li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>" +
            "</ul>" +
            "</div>" +
            "<p style=\"color: #666; margin-top: 30px;\">" +
            "Nếu bạn gặp vấn đề, vui lòng liên hệ với chúng tôi qua email hỗ trợ." +
            "</p>" +
            "</div>" +
            "<div class=\"footer\">" +
            "<p style=\"margin: 0;\">© 2024 UTeaDrink - Hệ thống quản lý cửa hàng trà sữa</p>" +
            "<p style=\"margin: 10px 0 0 0;\">📧 Email này được gửi tự động, vui lòng không trả lời</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
}
