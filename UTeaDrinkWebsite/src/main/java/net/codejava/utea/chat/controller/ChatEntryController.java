package net.codejava.utea.chat.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.chat.entity.Conversation;
import net.codejava.utea.chat.repository.ConversationRepository;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.common.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatEntryController {

	private final ConversationRepository conversationRepo;
	private final UserRepository userRepo;

	/**
	 * CUSTOMER bấm “Chat” -> tạo (hoặc lấy) 1 hội thoại với admin mặc định rồi
	 * redirect vào trang chat
	 */
	@GetMapping("/chat/start")
	public String startForCustomer(@AuthenticationPrincipal CustomUserDetails me) {
		if (me == null) {
			// chưa login -> đưa về login (hoặc về customer/home tuỳ bạn)
			return "redirect:/login";
		}

		Long customerId = me.getId();

		// 1) Lấy admin mặc định (cứ lấy admin đầu tiên). Bạn có thể thay bằng “admin
		// CSKH” hoặc round-robin.
		// YÊU CẦU: UserRepository phải có findFirstByRoles_CodeOrderByIdAsc(String
		// roleCode)
		User admin = userRepo.findFirstByRoles_CodeOrderByIdAsc("ADMIN")
				.orElseThrow(() -> new IllegalStateException("Chưa có tài khoản ADMIN để nhận chat"));

		// 2) Tìm hội thoại cũ
		Conversation c = conversationRepo.findByAdmin_IdAndCustomer_Id(admin.getId(), customerId).orElseGet(() -> {
			// 3) Chưa có thì tạo mới
			Conversation nc = new Conversation();
			nc.setAdmin(admin);
			// map customer từ user
			User cus = userRepo.findById(customerId)
					.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + customerId));
			nc.setCustomer(cus);
			nc.setCreatedAt(LocalDateTime.now());
			nc.setLastMessageAt(LocalDateTime.now());
			return conversationRepo.save(nc);
		});

		return "redirect:/chat/customer/" + c.getId();
	}

	/**
	 * ADMIN muốn chat với một khách cụ thể (ví dụ link từ trang quản lý user)
	 * /chat/start/{customerId}
	 */
	@GetMapping("/chat/start/customer")
	public String startForAdmin(Long customerId, @AuthenticationPrincipal CustomUserDetails adminUser) {
		if (adminUser == null)
			return "redirect:/login";

		Long adminId = adminUser.getId();

		Conversation c = conversationRepo.findByAdmin_IdAndCustomer_Id(adminId, customerId).orElseGet(() -> {
			Conversation nc = new Conversation();
			User admin = userRepo.findById(adminId)
					.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy admin: " + adminId));
			User cus = userRepo.findById(customerId)
					.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy customer: " + customerId));
			nc.setAdmin(admin);
			nc.setCustomer(cus);
			nc.setCreatedAt(LocalDateTime.now());
			nc.setLastMessageAt(LocalDateTime.now());
			return conversationRepo.save(nc);
		});

		return "redirect:/chat/admin/" + c.getId();
	}
}
