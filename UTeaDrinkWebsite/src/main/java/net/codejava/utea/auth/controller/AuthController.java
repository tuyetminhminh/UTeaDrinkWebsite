package net.codejava.utea.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.dto.RegisterRequest;
import net.codejava.utea.auth.service.JwtService;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.common.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final UserService userService;
    // private final OtpService otpService; // nếu bạn đã có

    @Value("${app.jwt.cookie-name:UTEA_TOKEN}")
    private String cookieName;
    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;
    @Value("${app.jwt.cookie-samesite:Lax}")
    private String sameSite;
    @Value("${security.jwt.expiration-time:604800000}")
    private long expMs; // 7 ngày
    @GetMapping({"/", "/main-home"})
    public String home() { return "home/main-home"; }
    /*@GetMapping("/admin/home")
    public String adminHome() { return "home/admin-home"; }*/
    // Customer home moved to HomeController for banner support
    @GetMapping("/seller/home")
    public String sellerHome() { return "home/seller-home"; }
    @GetMapping("/manager/home")
    public String managerHome() { return "manager/manager-home"; }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, // nhận cả email/username
                          @RequestParam String password, HttpServletResponse response, Model model) {
        try {
            Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

            String token = jwtService.generateTokenFromEmail(user.getUsername(), user.getRoleSummary());
            ResponseCookie cookie = ResponseCookie.from(cookieName, token).httpOnly(true).secure(cookieSecure).path("/")
                    .maxAge(expMs / 1000).sameSite(sameSite).build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            String roles = user.getRoleSummary();
            if (roles.contains("ADMIN"))
                return "redirect:/admin/home";
            if (roles.contains("MANAGER"))
                return "redirect:/manager/home";
            if (roles.contains("SHIPPER"))
                return "redirect:/shipper/home";
            return "redirect:/customer/home";

        } catch (Exception ex) {
            model.addAttribute("error", "❌ Tài khoản hoặc mật khẩu không đúng");
            return "auth/login";
        }
    }

    @PostMapping("/logout")
    public String doLogout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "").httpOnly(true).secure(cookieSecure).path("/")
                .maxAge(0).sameSite(sameSite).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return "redirect:/login?logout";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("req", new RegisterRequest());
        return "auth/register";
    }


    @PostMapping("/register")
    @Transactional
    public String doRegister(@ModelAttribute("req") RegisterRequest req, Model model) {

        // Validate đơn giản
        if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
            model.addAttribute("error", "❌ Email đã tồn tại");
            return "auth/register";
        }
        if (req.getUsername() != null && !req.getUsername().isBlank()
                && userRepo.existsByUsername(req.getUsername())) {
            model.addAttribute("error", "❌ Tên đăng nhập đã tồn tại");
            return "auth/register";
        }

        // Tạo user
        User u = User.builder()
                .email(req.getEmail())
                .username(req.getUsername())
                .fullName(req.getFullName())
                .passwordHash(encoder.encode(req.getPassword()))
                .status("ACTIVE")
                .build();

        u = userRepo.save(u);

        // 💡 GÁN ROLE CUSTOMER -> sinh dòng trong bảng user_roles
        userService.assignRoles(u.getId(), Set.of("CUSTOMER"));

        model.addAttribute("success", "✅ Đăng ký thành công! Hãy đăng nhập.");
        return "auth/login";
    }


    /* ======= FORGOT ======= */
    @GetMapping("/forgot")
    public String forgotPage() {
        return "auth/forgot";
    }

    @PostMapping("/forgot")
    public String doForgot(@RequestParam String email, Model model) {
        // nếu có OtpService thì gửi OTP:
        // try { otpService.sendResetOtpToEmail(email); ... } catch (Exception e) { ...
        // }
        if (userRepo.findByEmail(email).isEmpty()) {
            model.addAttribute("error", "Không tìm thấy email đã đăng ký");
            return "auth/forgot";
        }
        // giả sử chuyển ngay tới trang reset, bạn có thể hiển thị message "OTP đã gửi"
        model.addAttribute("email", email);
        model.addAttribute("sent", true);
        return "auth/reset";
    }

    /* ======= RESET ======= */
    @GetMapping("/reset")
    public String resetPage(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "auth/reset";
    }

    @PostMapping("/reset")
    public String doReset(@RequestParam String email, @RequestParam String password,
                          // @RequestParam String otp, // nếu bạn dùng OTP thật
                          Model model) {
        var u = userRepo.findByEmail(email).orElse(null);
        if (u == null) {
            model.addAttribute("error", "Email không hợp lệ");
            model.addAttribute("email", email);
            return "auth/reset";
        }
        // nếu có OTP: validate otpService.validate(email, otp) trước khi đổi mật khẩu.
        u.setPasswordHash(encoder.encode(password));
        userRepo.save(u);
        model.addAttribute("success", "🔑 Mật khẩu đã được đặt lại! Hãy đăng nhập.");
        return "auth/login";
    }
}
