//package net.codejava.utea.admin.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import net.codejava.utea.common.entity.User;
//import net.codejava.utea.common.repository.RoleRepository;
//import net.codejava.utea.common.repository.UserRepository;
//import net.codejava.utea.common.service.UserService;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.*;
//
//@Controller
//@RequestMapping("/admin/users")
//@RequiredArgsConstructor
//public class AdminUserController {
//
//    private final UserService userService;
//    private final UserRepository userRepo;
//    private final RoleRepository roleRepo;
//
//    /*
//     * ========================= LIST + SEARCH (phân trang)
//     * =========================
//     */
//    @GetMapping
//    public String list(@RequestParam(name = "kw", defaultValue = "") String kw,
//                       @RequestParam(name = "page", defaultValue = "1") int page,
//                       @RequestParam(name = "size", defaultValue = "10") int size, Model model) {
//        Page<User> data = userService.search(kw, PageRequest.of(Math.max(page - 1, 0), size));
//        model.addAttribute("page", data);
//        model.addAttribute("kw", kw);
//        model.addAttribute("rolesAll", roleRepo.findAll()); // để hiển thị checkboxes vai trò
//        return "admin/users/index";
//    }
//
//    /*
//     * ========================= TẠO MỚI USER (CREATE) =========================
//     */
//    @PostMapping("/create")
//    public String create(@RequestParam String email, @RequestParam(required = false) String username,
//                         @RequestParam String fullName, @RequestParam String password,
//                         @RequestParam(name = "roles", required = false) String role, RedirectAttributes ra) {
//        // validate cơ bản
//        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
//            ra.addFlashAttribute("error", "Email và mật khẩu không được để trống.");
//            return "redirect:/admin/users";
//        }
//        if (userRepo.existsByEmailIgnoreCase(email)) {
//            ra.addFlashAttribute("error", "Email đã tồn tại.");
//            return "redirect:/admin/users";
//        }
//
//        User u = User.builder()
//                .email(email.trim())
//                .username((username != null && !username.isBlank()) ? username.trim() : null)
//                .fullName(fullName)
//                .passwordHash(password)
//                .status("ACTIVE")
//                .build();
//
//        // gán 1 role (mặc định CUSTOMER)
//        String code = (role == null || role.isBlank()) ? "CUSTOMER" : role.toUpperCase();
//        roleRepo.findByCode(code).ifPresent(r -> u.setRoles(Set.of(r)));
//
//        userService.save(u);
//        ra.addFlashAttribute("success", "Tạo người dùng thành công.");
//        return "redirect:/admin/users";
//    }
//
//    /*
//     * ========================= CẬP NHẬT THÔNG TIN (UPDATE)
//     * =========================
//     */
//    @PostMapping("/{id}/update")
//    public String update(
//            @PathVariable Long id,
//            @RequestParam String fullName,
//            @RequestParam(required = false) String username,
//            @RequestParam(name = "role", required = false) String role,
//            RedirectAttributes ra
//    ) {
//        var u = userService.findById(id).orElse(null);
//        if (u == null) {
//            ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
//            return "redirect:/admin/users";
//        }
//
//        // ai đang đăng nhập?
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String currentPrincipal = auth != null ? auth.getName() : null; // email/username
//        boolean isSelf = currentPrincipal != null &&
//                (currentPrincipal.equalsIgnoreCase(u.getEmail()) ||
//                        (u.getUsername()!=null && currentPrincipal.equalsIgnoreCase(u.getUsername())));
//
//        // nếu tự sửa chính mình: không cho gỡ ADMIN
//        if (isSelf && role != null && !role.isBlank()) {
//            String newRole = role.toUpperCase();
//            boolean wasAdmin = u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()));
//            if (wasAdmin && !"ADMIN".equals(newRole)) {
//                ra.addFlashAttribute("error", "Bạn không thể tự hạ quyền ADMIN của chính mình.");
//                return "redirect:/admin/users";
//            }
//        }
//
//        u.setFullName(fullName);
//        u.setUsername((username != null && !username.isBlank()) ? username.trim() : null);
//
//        if (role != null && !role.isBlank()) {
//            var r = roleRepo.findByCode(role.toUpperCase()).orElse(null);
//            if (r != null) u.setRoles(Set.of(r)); // chỉ 1 role
//        }
//
//        userService.save(u);
//        ra.addFlashAttribute("success", "Cập nhật người dùng thành công.");
//        return "redirect:/admin/users";
//    }
//
//    /*
//     * ========================= KHÓA / MỞ KHÓA =========================
//     */
//    @PostMapping("/{id}/lock")
//    public String lock(@PathVariable Long id, RedirectAttributes ra) {
//        userService.enable(id, false);
//        ra.addFlashAttribute("success", "Đã khóa tài khoản.");
//        return "redirect:/admin/users";
//    }
//
//    @PostMapping("/{id}/unlock")
//    public String unlock(@PathVariable Long id, RedirectAttributes ra) {
//        userService.enable(id, true);
//        ra.addFlashAttribute("success", "Đã mở khóa tài khoản.");
//        return "redirect:/admin/users";
//    }
//
//    /*
//     * ========================= RESET MẬT KHẨU =========================
//     */
//    @PostMapping("/{id}/reset-password")
//    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword, RedirectAttributes ra) {
//        var u = userService.findById(id).orElse(null);
//        if (u == null) {
//            ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
//            return "redirect:/admin/users";
//        }
//        if (!StringUtils.hasText(newPassword) || newPassword.length() < 4) {
//            ra.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 4 ký tự.");
//            return "redirect:/admin/users";
//        }
//        u.setPasswordHash(newPassword); // NoOpPasswordEncoder → set thẳng
//        userService.save(u);
//        ra.addFlashAttribute("success", "Đã đặt lại mật khẩu.");
//        return "redirect:/admin/users";
//    }
//
//    /*
//     * ========================= GÁN VAI TRÒ (ASSIGN ROLES)
//     * =========================
//     */
//    @PostMapping("/{id}/roles")
//    public String assignRoles(
//            @PathVariable Long id,
//            @RequestParam(name = "role", required = false) String role,
//            RedirectAttributes ra
//    ) {
//        if (role == null || role.isBlank()) {
//            ra.addFlashAttribute("error", "Vui lòng chọn 1 vai trò.");
//            return "redirect:/admin/users";
//        }
//
//        // chặn tự hạ quyền
//        var u = userService.findById(id).orElse(null);
//        if (u == null) {
//            ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
//            return "redirect:/admin/users";
//        }
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String current = auth != null ? auth.getName() : null;
//        boolean isSelf = current != null &&
//                (current.equalsIgnoreCase(u.getEmail()) ||
//                        (u.getUsername()!=null && current.equalsIgnoreCase(u.getUsername())));
//
//        String newRole = role.toUpperCase();
//        boolean wasAdmin = u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()));
//        if (isSelf && wasAdmin && !"ADMIN".equals(newRole)) {
//            ra.addFlashAttribute("error", "Bạn không thể tự hạ quyền ADMIN của chính mình.");
//            return "redirect:/admin/users";
//        }
//
//        roleRepo.findByCode(newRole).ifPresent(r -> {
//            u.setRoles(Set.of(r));
//            userService.save(u);
//        });
//
//        ra.addFlashAttribute("success", "Cập nhật vai trò thành công.");
//        return "redirect:/admin/users";
//    }
//
//    /*
//     * ========================= XÓA (DELETE) =========================
//     */
//    @PostMapping("/{id}/delete")
//    public String delete(@PathVariable Long id, RedirectAttributes ra) {
//        userRepo.deleteById(id);
//        ra.addFlashAttribute("success", "Đã xóa người dùng.");
//        return "redirect:/admin/users";
//    }
//}

package net.codejava.utea.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import net.codejava.utea.common.dto.UserForm;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.RoleRepository;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.common.service.UserAdminAppService;
import net.codejava.utea.common.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.*;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserAdminAppService userApp;

    /*
     * ========================= LIST + SEARCH (phân trang)
     * =========================
     */
    @GetMapping
    public String list(@RequestParam(name = "kw",   defaultValue = "") String kw,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       Model model) {

        int p = Math.max(page - 1, 0);                // 0-based cho Spring
        Page<User> data = userService.search(kw, PageRequest.of(p, size));

        model.addAttribute("page", data);
        model.addAttribute("pageIndex", data.getNumber() + 1); // 1-based cho UI
        model.addAttribute("size", size);
        model.addAttribute("sizes", new int[]{5, 10,20,50,100});  // combobox
        model.addAttribute("kw", kw);
        model.addAttribute("rolesAll", roleRepo.findAll());
        return "admin/users/index";
    }

    /*
     * ========================= TẠO MỚI USER (CREATE) =========================
     */
    @GetMapping("/new")
    public String createForm(Model model){
        var form = new UserForm();
        model.addAttribute("form", form);
        model.addAttribute("rolesAll", roleRepo.findAll());
        return "admin/users/form";
    }

    // CREATE
    @PostMapping
    public String create(@Valid @ModelAttribute("form") UserForm form,
                         BindingResult br, Model model, RedirectAttributes ra) {
        // bắt buộc nhập pass khi tạo mới
        if (!StringUtils.hasText(form.getPassword())) {
            br.rejectValue("password", "required", "Vui lòng nhập mật khẩu (tối thiểu 4 ký tự).");
        } else if (form.getPassword().length() < 4) {
            br.rejectValue("password", "size", "Mật khẩu phải có ít nhất 4 ký tự.");
        }
        if (br.hasErrors()) {
            model.addAttribute("rolesAll", roleRepo.findAll());
            return "admin/users/form";
        }
        try {
            userApp.create(form);
            ra.addFlashAttribute("success", "Tạo người dùng thành công.");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex) {
            br.reject("err", ex.getMessage());           // lỗi chung VN
            model.addAttribute("rolesAll", roleRepo.findAll());
            return "admin/users/form";
        }
    }


    // ============ EDIT ============
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra){
        return userRepo.findById(id).map(u -> {
            var f = new UserForm();
            f.setId(u.getId());
            f.setEmail(u.getEmail());
            f.setUsername(u.getUsername());
            f.setFullName(u.getFullName());
            f.setStatus(u.getStatus());
            f.setRoleCode(u.getRoles().stream().findFirst().map(r -> r.getCode()).orElse("CUSTOMER"));

            model.addAttribute("form", f);
            model.addAttribute("rolesAll", roleRepo.findAll());
            return "admin/users/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/admin/users";
        });
    }


    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") UserForm form,
                         BindingResult br, Model model, RedirectAttributes ra){
        if (StringUtils.hasText(form.getPassword()) && form.getPassword().length() < 4) {
            br.rejectValue("password", "size", "Mật khẩu phải có ít nhất 4 ký tự.");}
        if (br.hasErrors()){
            model.addAttribute("rolesAll", roleRepo.findAll());
            return "admin/users/form";
        }
        // xác định có phải tự sửa mình không
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = (auth != null) ? auth.getName() : null;
        var u = userRepo.findById(id).orElse(null);
        boolean isSelfAdmin = false;
        if (u != null && principal != null){
            boolean isSelf = principal.equalsIgnoreCase(u.getEmail()) ||
                    (u.getUsername()!=null && principal.equalsIgnoreCase(u.getUsername()));
            boolean wasAdmin = u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()));
            isSelfAdmin = isSelf && wasAdmin;
        }

        try {
            userApp.update(id, form, isSelfAdmin);
            ra.addFlashAttribute("success", "Cập nhật người dùng thành công.");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex){
            br.reject("err", ex.getMessage());
            model.addAttribute("rolesAll", roleRepo.findAll());
            return "admin/users/form";
        }
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
