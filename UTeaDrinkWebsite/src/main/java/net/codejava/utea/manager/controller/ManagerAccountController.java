package net.codejava.utea.manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.dto.UserForm;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.common.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/account")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerAccountController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String viewAccount(@AuthenticationPrincipal CustomUserDetails cud, Model model) {
        User user = userService.findById(cud.getId()).orElseThrow();
        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setEmail(user.getEmail());
        form.setUsername(user.getUsername());
        form.setFullName(user.getFullName());
        form.setStatus(user.getStatus());
        form.setRoleCode("MANAGER");
        model.addAttribute("user", user);
        model.addAttribute("userForm", form);
        return "manager/account";
    }

    @PostMapping
    public String updateAccount(@Valid @ModelAttribute("userForm") UserForm userForm,
                               BindingResult br,
                               @AuthenticationPrincipal CustomUserDetails cud,
                               RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("user", userService.findById(cud.getId()).orElseThrow());
            return "manager/account";
        }
        // KHÔNG sửa username, fullName.
        ra.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        return "redirect:/manager/account";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails cud,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {
        User user = userService.findById(cud.getId()).orElseThrow();
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            ra.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
            return "redirect:/manager/account";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Xác nhận mật khẩu không khớp!");
            return "redirect:/manager/account";
        }
        if (newPassword.length() < 6) {
            ra.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
            return "redirect:/manager/account";
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userService.save(user);
        ra.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        return "redirect:/manager/account";
    }
}
