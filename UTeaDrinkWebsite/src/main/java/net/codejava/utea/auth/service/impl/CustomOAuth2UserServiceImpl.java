package net.codejava.utea.auth.service.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.service.CustomOAuth2UserService;
import net.codejava.utea.common.entity.Role;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.RoleRepository;
import net.codejava.utea.common.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService
		implements org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OAuth2UserRequest, OAuth2User>,
		CustomOAuth2UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// Lấy user từ nhà cung cấp (Google/Facebook)
		OAuth2User oAuth2User = super.loadUser(userRequest);
		Map<String, Object> attrs = oAuth2User.getAttributes();

		final String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" / "facebook"

		// Rút email/name theo provider – gán vào biến final để dùng trong
		// lambda/streams
		final String email = extractEmail(registrationId, attrs);
		final String name = extractName(registrationId, attrs);

		if (email == null || email.isBlank()) {
			throw new OAuth2AuthenticationException("Email không tồn tại từ nhà cung cấp " + registrationId);
		}

		// Tìm/khởi tạo user theo email (KHÔNG đổi biến email/name sau khi gán ->
		// effectively final)
		User user = userRepository.findByEmail(email).orElseGet(() -> {
			// tạo user mới với role CUSTOMER
			Role roleCustomer = roleRepository.findByCode("CUSTOMER")
					.orElseGet(() -> roleRepository.save(Role.builder().code("CUSTOMER").build()));

			User u = User.builder().email(email).fullName(name != null ? name : email).status("ACTIVE").build();
			u.getRoles().add(roleCustomer);
			return userRepository.save(u);
		});

		// Tạo authorities từ roles của user
		Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
				.map(r -> new SimpleGrantedAuthority(r.getCode())).collect(java.util.stream.Collectors.toSet());

		// Trả DefaultOAuth2User; đặt "email" làm key cho getName()
		return new DefaultOAuth2User((Collection<? extends GrantedAuthority>) authorities, attrs,
				resolveNameAttributeKey(registrationId, userRequest) // ưu tiên "email"
		);
	}

	private String extractEmail(String registrationId, Map<String, Object> attrs) {
		// Google: "email"; Facebook: "email" (cần scope email)
		Object value = attrs.get("email");
		if (value instanceof String s && !s.isBlank())
			return s;

		// Fallback một số provider có cấu trúc lạ
		if ("facebook".equalsIgnoreCase(registrationId)) {
			// tuỳ app config, có thể cần fields?email,name
			// nếu không có email -> null
		}
		return null;
	}

	private String extractName(String registrationId, Map<String, Object> attrs) {
		// Google/Facebook thường có "name"
		Object value = attrs.get("name");
		if (value instanceof String s && !s.isBlank())
			return s;

		// Fallback từ "given_name" + "family_name"
		String given = (String) attrs.getOrDefault("given_name", "");
		String family = (String) attrs.getOrDefault("family_name", "");
		String combined = (given + " " + family).trim();
		return combined.isEmpty() ? null : combined;
	}

	/**
	 * Chọn thuộc tính làm nameAttributeKey; ta muốn dùng "email" nếu có.
	 */
	private String resolveNameAttributeKey(String registrationId, OAuth2UserRequest req) {
		// Nếu provider không có email, có thể dùng sub/id mặc định
		String configured = req.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
				.getUserNameAttributeName();
		return (configured != null && !configured.isBlank()) ? configured : "email";
	}
}
