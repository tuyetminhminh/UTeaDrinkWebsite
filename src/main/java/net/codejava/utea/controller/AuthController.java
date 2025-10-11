package net.codejava.utea.controller;

import net.codejava.utea.dto.RegisterRequest;
import net.codejava.utea.entity.Account;
import net.codejava.utea.entity.Customer;
import net.codejava.utea.repository.AccountRepository;
import net.codejava.utea.repository.CustomerRepository;
import net.codejava.utea.service.OtpService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AccountRepository accountRepo;
    private final CustomerRepository customerRepo;
    private final PasswordEncoder encoder;
    private final OtpService otpService;

    public AuthController(AccountRepository accountRepo,
                          CustomerRepository customerRepo,
                          PasswordEncoder encoder,
                          OtpService otpService) {
        this.accountRepo = accountRepo;
        this.customerRepo = customerRepo;
        this.encoder = encoder;
        this.otpService = otpService;
    }
    @GetMapping("/customer/home")
    public String customerHome() {
        return "home/customer-home"; // trỏ đến templates/home/customer-home.html
    }

    @GetMapping("/admin/home")
    public String adminHome() {
        return "home/admin-home"; // trỏ đến templates/home/admin-home.html
    }


    // ===================== TRANG CHÍNH =====================
    @GetMapping({"/","main-home"})
    public String home() { return "home/main-home"; }

    // ===================== ĐĂNG NHẬP =====================
    @GetMapping("/login")
    public String loginPage() { return "auth/login"; }

    // ===================== ĐĂNG KÝ =====================
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("req", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute("req") RegisterRequest req, Model model) {
        // Kiểm tra username trùng
        if (accountRepo.findByUsername(req.getUsername()).isPresent()) {
            model.addAttribute("error", "❌ Tên đăng nhập đã tồn tại!");
            return "auth/register";
        }

        // Kiểm tra email trùng
        if (customerRepo.findByEmail(req.getEmail()).isPresent()) {
            model.addAttribute("error", "❌ Email đã được sử dụng!");
            return "auth/register";
        }

        // Tạo Account (role CUSTOMER)
        Account acc = Account.builder()
                .username(req.getUsername())
                .password(encoder.encode(req.getPassword())) // mã hóa an toàn
                .role("CUSTOMER")
                .enabled(true)
                .displayName(req.getFullName())
                .build();
        acc = accountRepo.save(acc);

        // Tạo Customer profile (thêm địa chỉ)
        Customer c = Customer.builder()
                .account(acc)
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phoneNumber(req.getPhone())
                .diachi(req.getAddress()) // 🏠 thêm địa chỉ vào đây
                .status("ACTIVE")
                .build();
        customerRepo.save(c);

        model.addAttribute("registered", true);
        model.addAttribute("success", "✅ Đăng ký thành công! Hãy đăng nhập.");
        return "auth/login";
    }

    // ===================== QUÊN MẬT KHẨU =====================
    @GetMapping("/forgot")
    public String forgotPage() { return "auth/forgot"; }

    @PostMapping("/forgot")
    public String doForgot(@RequestParam String email, Model model) {
        try {
            otpService.sendResetOtpToEmail(email);
            model.addAttribute("email", email);
            model.addAttribute("sent", true);
            model.addAttribute("message", "📧 Mã OTP đã được gửi tới email: " + email);
            return "auth/reset"; // qua trang nhập OTP + mật khẩu mới
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/forgot";
        }
    }

    // ===================== ĐẶT LẠI MẬT KHẨU =====================
    @GetMapping("/reset")
    public String resetPage(@RequestParam(required=false) String email, Model model) {
        model.addAttribute("email", email);
        return "auth/reset";
    }

    @PostMapping("/reset")
    public String doReset(@RequestParam String email,
                          @RequestParam String otp,
                          @RequestParam String password,
                          Model model) {
        try {
            otpService.resetPasswordByEmail(email, otp, password);
            model.addAttribute("resetOk", true);
            model.addAttribute("success", "🔑 Mật khẩu đã được đặt lại! Hãy đăng nhập.");
            return "auth/login";
        } catch (Exception e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
            return "auth/reset";
        }
    }
}
