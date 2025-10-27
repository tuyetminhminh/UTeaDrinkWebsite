package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.common.service.AddressService;
import net.codejava.utea.common.service.UserService;
import net.codejava.utea.customer.dto.AddressForm;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/account")
public class CustomerAccountController {

    private final UserService userService;
    private final AddressService addressService;
    private final PasswordEncoder passwordEncoder;

    /** Trang quản lý tài khoản */
    @GetMapping
    public String viewAccount(@AuthenticationPrincipal CustomUserDetails cud, Model model) {
        User user = userService.findById(cud.getId()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("addresses", addressService.listOf(user));
        model.addAttribute("addressForm", new AddressForm());
        return "customer/account";
    }

    // -------------------- QUẢN LÝ ĐỊA CHỈ --------------------

    @PostMapping("/addresses")
    public String addAddress(@AuthenticationPrincipal CustomUserDetails cud,
                             @Valid @ModelAttribute("addressForm") AddressForm form,
                             BindingResult br,
                             RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Vui lòng nhập đầy đủ thông tin hợp lệ!");
            return "redirect:/customer/account";
        }

        User owner = userService.findById(cud.getId()).orElseThrow();
        var address = net.codejava.utea.common.entity.Address.builder()
                .user(owner)
                .receiverName(form.getReceiverName())
                .phone(form.getPhone())
                .line(form.getLine())
                .ward(form.getWard())
                .district(form.getDistrict())
                .province(form.getProvince())
                .isDefault(form.isDefault())
                .build();

        addressService.save(address);
        ra.addFlashAttribute("success", "Đã thêm địa chỉ mới!");
        return "redirect:/customer/account";
    }

    @PostMapping("/addresses/{id}/default")
    public String markDefault(@AuthenticationPrincipal CustomUserDetails cud,
                              @PathVariable Long id,
                              RedirectAttributes ra) {
        addressService.markDefault(id, cud.getId());
        ra.addFlashAttribute("success", "Đã đặt địa chỉ mặc định!");
        return "redirect:/customer/account";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(@AuthenticationPrincipal CustomUserDetails cud,
                                @PathVariable Long id,
                                RedirectAttributes ra) {
        addressService.delete(id, cud.getId());
        ra.addFlashAttribute("success", "Đã xóa địa chỉ!");
        return "redirect:/customer/account";
    }

    // -------------------- ĐỔI MẬT KHẨU --------------------

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails cud,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {

        User user = userService.findById(cud.getId()).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            ra.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác!");
            return "redirect:/customer/account";
        }

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Xác nhận mật khẩu không khớp!");
            return "redirect:/customer/account";
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userService.save(user);

        ra.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        return "redirect:/customer/account";
    }
}
