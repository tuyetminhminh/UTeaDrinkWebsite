package net.codejava.utea.service.impl;


import jakarta.transaction.Transactional;
import net.codejava.utea.entity.PasswordResetOtp;
import net.codejava.utea.repository.AccountRepository;
import net.codejava.utea.repository.CustomerRepository;
import net.codejava.utea.repository.PasswordResetOtpRepository;
import net.codejava.utea.service.EmailService;
import net.codejava.utea.service.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpServiceImpl implements OtpService {
    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;
    private final PasswordResetOtpRepository otpRepo;
    private final EmailService emailService;
    private final PasswordEncoder encoder;

    @Value("${app.otp.expire-minutes:10}")
    int expireMinutes;

    public OtpServiceImpl(CustomerRepository customerRepo,
                          AccountRepository accountRepo,
                          PasswordResetOtpRepository otpRepo,
                          EmailService emailService,
                          PasswordEncoder encoder) {
        this.customerRepo = customerRepo;
        this.accountRepo = accountRepo;
        this.otpRepo = otpRepo;
        this.emailService = emailService;
        this.encoder = encoder;
    }

    private String gen6() {
        var rnd = new SecureRandom();
        int n = rnd.nextInt(900000) + 100000; // 100000..999999
        return String.valueOf(n);
    }

    @Override
    @Transactional
    public void sendResetOtpToEmail(String email) {
        var customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng với email này"));

        var acc = customer.getAccount();
        String code = gen6();

        var otp = PasswordResetOtp.builder()
                .account(acc)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(expireMinutes))
                .build();
        otpRepo.save(otp);

        String html = """
            <h3>UTeaDrink - Đặt lại mật khẩu</h3>
            <p>Mã OTP của bạn là: <b>%s</b></p>
            <p>Mã có hiệu lực trong %d phút.</p>
            """.formatted(code, expireMinutes);

        emailService.send(email, "UTeaDrink - Mã OTP đặt lại mật khẩu", html);
    }

    @Override
    @Transactional
    public void resetPasswordByEmail(String email, String otpCode, String newPassword) {
        var customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại"));
        var acc = customer.getAccount();

        var validOtp = otpRepo.findTopByAccountAndCodeAndUsedIsFalseAndExpiresAtAfterOrderByIdDesc(
                acc, otpCode, LocalDateTime.now()
        ).orElseThrow(() -> new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn"));

        acc.setPassword(encoder.encode(newPassword));
        accountRepo.save(acc);

        validOtp.setUsed(true);
        otpRepo.save(validOtp);
    }
}
