package net.codejava.utea.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.RoleRepository;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.common.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

	private final UserService userService;
	private final UserRepository userRepo;
	private final RoleRepository roleRepo;

	/*
	 * ========================= LIST + SEARCH (phân trang)
	 * =========================
	 */
	@GetMapping
	public String list(@RequestParam(name = "kw", defaultValue = "") String kw,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, Model model) {
		Page<User> data = userService.search(kw, PageRequest.of(Math.max(page - 1, 0), size));
		model.addAttribute("page", data);
		model.addAttribute("kw", kw);
		model.addAttribute("rolesAll", roleRepo.findAll()); // để hiển thị checkboxes vai trò
		return "admin/users/index";
	}

	/*
	 * ========================= TẠO MỚI USER (CREATE) =========================
	 */
	@PostMapping("/create")
	public String create(@RequestParam String email, @RequestParam(required = false) String username,
			@RequestParam String fullName, @RequestParam String password,
			@RequestParam(name = "roles", required = false) String role, RedirectAttributes ra) {
		// validate cơ bản
		if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
			ra.addFlashAttribute("error", "Email và mật khẩu không được để trống.");
			return "redirect:/admin/users";
		}
		if (userRepo.existsByEmailIgnoreCase(email)) {
			ra.addFlashAttribute("error", "Email đã tồn tại.");
			return "redirect:/admin/users";
		}

		User u = User.builder()
	            .email(email.trim())
	            .username((username != null && !username.isBlank()) ? username.trim() : null)
	            .fullName(fullName)
	            .passwordHash(password)
	            .status("ACTIVE")
	            .build();

	    // gán 1 role (mặc định CUSTOMER)
	    String code = (role == null || role.isBlank()) ? "CUSTOMER" : role.toUpperCase();
	    roleRepo.findByCode(code).ifPresent(r -> u.setRoles(Set.of(r)));

	    userService.save(u);
	    ra.addFlashAttribute("success", "Tạo người dùng thành công.");
	    return "redirect:/admin/users";
	}

	/*
	 * ========================= CẬP NHẬT THÔNG TIN (UPDATE)
	 * =========================
	 */
	@PostMapping("/{id}/update")
	public String update(
	        @PathVariable Long id,
	        @RequestParam String fullName,
	        @RequestParam(required = false) String username,
	        @RequestParam(name = "role", required = false) String role,
	        RedirectAttributes ra
	) {
	    var u = userService.findById(id).orElse(null);
	    if (u == null) {
	        ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
	        return "redirect:/admin/users";
	    }

	    // ai đang đăng nhập?
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String currentPrincipal = auth != null ? auth.getName() : null; // email/username
	    boolean isSelf = currentPrincipal != null &&
	            (currentPrincipal.equalsIgnoreCase(u.getEmail()) ||
	             (u.getUsername()!=null && currentPrincipal.equalsIgnoreCase(u.getUsername())));

	    // nếu tự sửa chính mình: không cho gỡ ADMIN
	    if (isSelf && role != null && !role.isBlank()) {
	        String newRole = role.toUpperCase();
	        boolean wasAdmin = u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()));
	        if (wasAdmin && !"ADMIN".equals(newRole)) {
	            ra.addFlashAttribute("error", "Bạn không thể tự hạ quyền ADMIN của chính mình.");
	            return "redirect:/admin/users";
	        }
	    }

	    u.setFullName(fullName);
	    u.setUsername((username != null && !username.isBlank()) ? username.trim() : null);

	    if (role != null && !role.isBlank()) {
	        var r = roleRepo.findByCode(role.toUpperCase()).orElse(null);
	        if (r != null) u.setRoles(Set.of(r)); // chỉ 1 role
	    }

	    userService.save(u);
	    ra.addFlashAttribute("success", "Cập nhật người dùng thành công.");
	    return "redirect:/admin/users";
	}

	/*
	 * ========================= KHÓA / MỞ KHÓA =========================
	 */
	@PostMapping("/{id}/lock")
	public String lock(@PathVariable Long id, RedirectAttributes ra) {
		userService.enable(id, false);
		ra.addFlashAttribute("success", "Đã khóa tài khoản.");
		return "redirect:/admin/users";
	}

	@PostMapping("/{id}/unlock")
	public String unlock(@PathVariable Long id, RedirectAttributes ra) {
		userService.enable(id, true);
		ra.addFlashAttribute("success", "Đã mở khóa tài khoản.");
		return "redirect:/admin/users";
	}

	/*
	 * ========================= RESET MẬT KHẨU =========================
	 */
	@PostMapping("/{id}/reset-password")
	public String resetPassword(@PathVariable Long id, @RequestParam String newPassword, RedirectAttributes ra) {
		var u = userService.findById(id).orElse(null);
		if (u == null) {
			ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
			return "redirect:/admin/users";
		}
		if (!StringUtils.hasText(newPassword) || newPassword.length() < 4) {
			ra.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 4 ký tự.");
			return "redirect:/admin/users";
		}
		u.setPasswordHash(newPassword); // NoOpPasswordEncoder → set thẳng
		userService.save(u);
		ra.addFlashAttribute("success", "Đã đặt lại mật khẩu.");
		return "redirect:/admin/users";
	}

	/*
	 * ========================= GÁN VAI TRÒ (ASSIGN ROLES)
	 * =========================
	 */
	@PostMapping("/{id}/roles")
	public String assignRoles(
	        @PathVariable Long id,
	        @RequestParam(name = "role", required = false) String role,
	        RedirectAttributes ra
	) {
	    if (role == null || role.isBlank()) {
	        ra.addFlashAttribute("error", "Vui lòng chọn 1 vai trò.");
	        return "redirect:/admin/users";
	    }

	    // chặn tự hạ quyền
	    var u = userService.findById(id).orElse(null);
	    if (u == null) {
	        ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
	        return "redirect:/admin/users";
	    }

	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String current = auth != null ? auth.getName() : null;
	    boolean isSelf = current != null &&
	            (current.equalsIgnoreCase(u.getEmail()) ||
	             (u.getUsername()!=null && current.equalsIgnoreCase(u.getUsername())));

	    String newRole = role.toUpperCase();
	    boolean wasAdmin = u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()));
	    if (isSelf && wasAdmin && !"ADMIN".equals(newRole)) {
	        ra.addFlashAttribute("error", "Bạn không thể tự hạ quyền ADMIN của chính mình.");
	        return "redirect:/admin/users";
	    }

	    roleRepo.findByCode(newRole).ifPresent(r -> {
	        u.setRoles(Set.of(r));
	        userService.save(u);
	    });

	    ra.addFlashAttribute("success", "Cập nhật vai trò thành công.");
	    return "redirect:/admin/users";
	}

	/*
	 * ========================= XÓA (DELETE) =========================
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes ra) {
		userRepo.deleteById(id);
		ra.addFlashAttribute("success", "Đã xóa người dùng.");
		return "redirect:/admin/users";
	}
}
