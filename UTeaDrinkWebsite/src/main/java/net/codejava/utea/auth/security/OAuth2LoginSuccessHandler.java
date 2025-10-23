package net.codejava.utea.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.service.JwtService;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler
		implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

	private final UserRepository userRepo;
	private final JwtService jwtService;

	@Value("${app.jwt.cookie-name:UTEA_TOKEN}")
	String cookieName;
	@Value("${security.jwt.expiration-time:604800000}")
	long expMs;
	@Value("${app.jwt.cookie-secure:false}")
	boolean cookieSecure;
	@Value("${app.jwt.cookie-samesite:Lax}")
	String sameSite;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		OAuth2User oauth = (OAuth2User) authentication.getPrincipal();
		String email = (String) oauth.getAttributes().get("email");
		if (email == null) {
			// fallback FB (đã map ở service) -> id@fb.local
			Object id = oauth.getAttributes().get("id");
			email = "fb_" + id + "@fb.local";
		}

		User user = userRepo.findByEmail(email).orElse(null);
		if (user == null) {
			response.sendRedirect(
					"/login?error=" + UriUtils.encode("Không tìm thấy user sau khi đăng nhập", StandardCharsets.UTF_8));
			return;
		}

		// role chính để điều hướng (ưu tiên ADMIN/MANAGER/CUSTOMER/SHIPPER)
		String topRole = user.getRoles().stream().map(r -> r.getCode()).findFirst().orElse("CUSTOMER");

		// sinh JWT (tái sử dụng JwtService bạn đang có: nhận username/email + role)
		String token = jwtService.generateTokenFromEmail(email, topRole);

		Cookie c = new Cookie(cookieName, token);
		c.setHttpOnly(true);
		c.setSecure(cookieSecure);
		c.setPath("/");
		c.setMaxAge((int) (expMs / 1000));
		response.addCookie(c);
		response.addHeader("Set-Cookie", "%s=%s; Max-Age=%d; Path=/; HttpOnly; %s".formatted(cookieName, token,
				(int) (expMs / 1000), cookieSecure ? "Secure; SameSite=" + sameSite : "SameSite=" + sameSite));

		// điều hướng theo role với thông báo đăng nhập thành công
		String redirect = switch (topRole) {
		case "ADMIN" -> "/admin/home?login=success&oauth=google";
		case "MANAGER" -> "/manager/home?login=success&oauth=google";
		case "SHIPPER" -> "/shipper/home?login=success&oauth=google";
		default -> "/customer/home?login=success&oauth=google";
		};
		response.sendRedirect(redirect);
	}
}
