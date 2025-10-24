package net.codejava.utea.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.service.JwtService;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.entity.Role;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.common.repository.RoleRepository;
import net.codejava.utea.common.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authManager;
	private final JwtService jwtService;
	private final UserRepository userRepo;
	private final RoleRepository roleRepo;
	private final PasswordEncoder encoder;
	// private final OtpService otpService; // nếu bạn đã có

	@Value("${app.jwt.cookie-name:UTEA_TOKEN}")
	private String cookieName;
	@Value("${app.jwt.cookie-secure:false}")
	private boolean cookieSecure;
	@Value("${app.jwt.cookie-samesite:Lax}")
	private String sameSite;
	@Value("${security.jwt.expiration-time:604800000}")
	private long expMs; // 7 ngày
<<<<<<< Updated upstream
=======
    @GetMapping({"/", "/main-home"})
    public String home() { return "home/main-home"; }
    @GetMapping("/admin/home")
    public String adminHome() { return "home/admin-home"; }
    @GetMapping("/customer/home")
    public String customerHome() { return "home/customer-home"; }
    @GetMapping("/seller/home")
    public String sellerHome() { return "home/seller-home"; }
	@GetMapping("/manager/home")
	public String managerHome() { return "manager/manager-home"; }


	@GetMapping("/shipper/home")
	public String shipperHome() { return "home/shipper-home"; }
>>>>>>> Stashed changes

	@GetMapping("/login")
	public String loginPage() {
		return "auth/login";
	}

	@GetMapping("/login-success")
	public String loginSuccess(HttpServletRequest request, HttpServletResponse response) {
		// Lấy thông tin user đã đăng nhập
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
			
			// Tạo JWT token
			String token = jwtService.generateTokenFromEmail(user.getUsername(), user.getRoleSummary());
			ResponseCookie cookie = ResponseCookie.from(cookieName, token).httpOnly(true).secure(cookieSecure).path("/")
					.maxAge(expMs / 1000).sameSite(sameSite).build();
			response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

			// Redirect theo role
			String roles = user.getRoleSummary();
			if (roles.contains("ADMIN"))
				return "redirect:/admin/home?login=success";
			else if (roles.contains("MANAGER"))
				return "redirect:/manager/home?login=success";
			else if (roles.contains("SHIPPER"))
				return "redirect:/shipper/home?login=success";
			else
				return "redirect:/customer/home?login=success";
		}
		
		return "redirect:/login?error=true";
	}

	@PostMapping("/logout")
	public String doLogout(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(cookieName, "").httpOnly(true).secure(cookieSecure).path("/")
				.maxAge(0).sameSite(sameSite).build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		return "redirect:/login?logout";
	}

	/* ======= REGISTER ======= */
	@GetMapping("/register")
	public String registerPage() {
		return "auth/register";
	}

	@PostMapping("/register")
	public String doRegister(@RequestParam String email, @RequestParam String username, @RequestParam String fullName,
			@RequestParam String password, @RequestParam String confirmPassword, Model model) {
		
		// Validation
		if (!password.equals(confirmPassword)) {
			model.addAttribute("error", "❌ Mật khẩu xác nhận không khớp");
			return "auth/register";
		}
		
		if (password.length() < 6) {
			model.addAttribute("error", "❌ Mật khẩu phải có ít nhất 6 ký tự");
			return "auth/register";
		}
		
		if (userRepo.findByEmail(email).isPresent()) {
			model.addAttribute("error", "❌ Email đã tồn tại");
			return "auth/register";
		}
		if (userRepo.findByUsername(username).isPresent()) {
			model.addAttribute("error", "❌ Tên đăng nhập đã tồn tại");
			return "auth/register";
		}
		
		try {
			User u = User.builder()
				.email(email)
				.username(username)
				.fullName(fullName)
				.passwordHash(encoder.encode(password))
				.status("ACTIVE")
				.build();
			
			// Assign CUSTOMER role
			Role customerRole = roleRepo.findByCode("CUSTOMER")
				.orElseThrow(() -> new RuntimeException("CUSTOMER role not found"));
			Set<Role> roles = new HashSet<>();
			roles.add(customerRole);
			u.setRoles(roles);
			
			userRepo.save(u);
			model.addAttribute("success", "✅ Đăng ký thành công! Hãy đăng nhập.");
			return "auth/login";
		} catch (Exception ex) {
			model.addAttribute("error", "❌ Lỗi hệ thống: " + ex.getMessage());
			return "auth/register";
		}
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

	// ===================== TRANG CHÍNH =====================
	@GetMapping({"/", "/main-home"})
	public String home() { 
		System.out.println("DEBUG: Đang truy cập trang chủ - home/main-home");
		return "home/main-home"; 
	}

	@GetMapping("/customer/home")
	public String customerHome() { 
		return "customer/home"; 
	}

	@GetMapping("/admin/home")
	public String adminHome() { 
		return "admin/simple"; 
	}

	@GetMapping("/manager/home")
	public String managerHome() { 
		return "manager/home"; 
	}

	@GetMapping("/shipper/home")
	public String shipperHome() { 
		return "shipper/home"; 
	}
}
