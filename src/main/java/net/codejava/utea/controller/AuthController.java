package net.codejava.utea.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import net.codejava.utea.dto.RegisterRequest;
import net.codejava.utea.entity.Account;
import net.codejava.utea.entity.Customer;
import net.codejava.utea.repository.AccountRepository;
import net.codejava.utea.repository.CustomerRepository;
import net.codejava.utea.service.impl.JwtService;
import net.codejava.utea.service.impl.CustomUserDetails;
import net.codejava.utea.service.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final AuthenticationManager authManager; // ✅ Inject qua constructor
    private final JwtService jwtService; // ✅ Inject luôn vào constructor

    public AuthController(AccountRepository accountRepo,
                          CustomerRepository customerRepo,
                          PasswordEncoder encoder,
                          OtpService otpService,
                          AuthenticationManager authManager,
                          JwtService jwtService) {
        this.accountRepo = accountRepo;
        this.customerRepo = customerRepo;
        this.encoder = encoder;
        this.otpService = otpService;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    // ===================== ĐĂNG NHẬP =====================
    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpServletResponse resp,
                          Model model,
                          @Value("${app.jwt.cookie-name:UTEA_TOKEN}") String cookieName,
                          @Value("${security.jwt.expiration-time:3600000}") long expMs,
                          @Value("${app.jwt.cookie-secure:false}") boolean cookieSecure,
                          @Value("${app.jwt.cookie-samesite:Lax}") String sameSite) {

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

            String token = jwtService.generateToken(user, user.getRole());

            Cookie c = new Cookie(cookieName, token);
            c.setHttpOnly(true);
            c.setSecure(cookieSecure);
            c.setPath("/");
            c.setMaxAge((int) (expMs / 1000));
            resp.addCookie(c);
            resp.addHeader("Set-Cookie",
                    "%s=%s; Max-Age=%d; Path=/; HttpOnly; %s"
                            .formatted(cookieName, token, (int) (expMs / 1000),
                                    cookieSecure ? "Secure; SameSite=" + sameSite : "SameSite=" + sameSite));

            switch (user.getRole().toUpperCase()) {
                case "ADMIN": return "redirect:/admin/home";
                case "CUSTOMER": return "redirect:/customer/home";
                case "SELLER": return "redirect:/seller/home";
                case "SHIPPER": return "redirect:/shipper/home";
                default: return "redirect:/main-home";
            }
        } catch (Exception e) {
            model.addAttribute("error", "❌ Tài khoản hoặc mật khẩu không đúng");
            return "auth/login";
        }
    }

    // ===================== ĐĂNG XUẤT =====================
    @PostMapping("/logout")
    public String doLogout(HttpServletResponse resp,
                           @Value("${app.jwt.cookie-name:UTEA_TOKEN}") String cookieName,
                           @Value("${app.jwt.cookie-secure:false}") boolean cookieSecure,
                           @Value("${app.jwt.cookie-samesite:Lax}") String sameSite) {
        Cookie c = new Cookie(cookieName, "");
        c.setHttpOnly(true);
        c.setSecure(cookieSecure);
        c.setPath("/");
        c.setMaxAge(0);
        resp.addCookie(c);
        resp.addHeader("Set-Cookie",
                "%s=; Max-Age=0; Path=/; HttpOnly; %s"
                        .formatted(cookieName, cookieSecure ? "Secure; SameSite=" + sameSite : "SameSite=" + sameSite));
        return "redirect:/main-home?logout=true";
    }

    // ===================== TRANG CHÍNH =====================
    @GetMapping({"/", "/main-home"})
    public String home() { return "home/main-home"; }

    @GetMapping("/customer/home")
    public String customerHome() { return "home/customer-home"; }

    @GetMapping("/admin/home")
    public String adminHome() { return "home/admin-home"; }

    @GetMapping("/seller/home")
    public String sellerHome() { return "home/seller-home"; }

    @GetMapping("/shipper/home")
    public String shipperHome() { return "home/shipper-home"; }

    // ===================== ĐĂNG KÝ =====================
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("req", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute("req") RegisterRequest req, Model model) {
        if (accountRepo.findByUsername(req.getUsername()).isPresent()) {
            model.addAttribute("error", "❌ Tên đăng nhập đã tồn tại!");
            return "auth/register";
        }

        if (customerRepo.findByEmail(req.getEmail()).isPresent()) {
            model.addAttribute("error", "❌ Email đã được sử dụng!");
            return "auth/register";
        }

        Account acc = Account.builder()
                .username(req.getUsername())
                .password(encoder.encode(req.getPassword()))
                .role("CUSTOMER")
                .enabled(true)
                .displayName(req.getFullName())
                .build();
        acc = accountRepo.save(acc);

        Customer c = Customer.builder()
                .account(acc)
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phoneNumber(req.getPhone())
                .diachi(req.getAddress())
                .status("ACTIVE")
                .build();
        customerRepo.save(c);

        model.addAttribute("registered", true);
        model.addAttribute("success", "✅ Đăng ký thành công! Hãy đăng nhập.");
        return "auth/login";
    }
    @GetMapping("/login")
    public String loginPage() {
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
            return "auth/reset";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/forgot";
        }
    }


    // ===================== ĐẶT LẠI MẬT KHẨU =====================
    @GetMapping("/reset")
    public String resetPage(@RequestParam(required = false) String email, Model model) {
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
