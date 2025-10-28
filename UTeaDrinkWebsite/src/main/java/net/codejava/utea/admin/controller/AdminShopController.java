package net.codejava.utea.admin.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.manager.dto.ShopDTO;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.manager.service.ShopAdminService;
import net.codejava.utea.common.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/shops")
public class AdminShopController {

    private final ShopRepository shopRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    // ------ LIST ------
    @GetMapping
    public String index(@RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String q,
                        Model model) {

        int pageIndex = Math.max(page, 1);
        Page<Shop> p = shopRepo.findAll(PageRequest.of(pageIndex - 1, size));

        // lấy tên manager cho mỗi shop để hiển thị
        List<Long> ids = p.getContent().stream().map(Shop::getId).toList();
        Map<Long, String> managerNameMap = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : shopManagerRepo.findManagerNamesByShopIds(ids)) {
                managerNameMap.put((Long) row[0], (String) row[1]);
            }
        }
        p.getContent().forEach(s -> s.setTempManagerName(managerNameMap.get(s.getId())));

        model.addAttribute("page", p);
        model.addAttribute("pageIndex", pageIndex);
        model.addAttribute("size", size);
        model.addAttribute("q", q);
        model.addAttribute("sizes", new int[] { 5, 10, 20, 50 });
        return "admin/shops/index";
    }

    // ------ NEW FORM ------
    @GetMapping("/new")
    public String createForm(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model) {
        ShopDTO form = ShopDTO.builder().status("OPEN").build();
        model.addAttribute("form", form);
        model.addAttribute("pageIndex", page);
        model.addAttribute("size", size);
        return "admin/shops/form";
    }

    // ------ CREATE ------
    @PostMapping
    public String create(@Valid @ModelAttribute("form") ShopDTO form,
                         BindingResult br,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model,
                         RedirectAttributes ra) {

        // 1) validate field cơ bản
        if (br.hasErrors()) {
            model.addAttribute("pageIndex", page);
            model.addAttribute("size", size);
            return "admin/shops/form";
        }

        // 2) chuẩn hoá + validate trùng lặp
        normalize(form);
        validateUniqueCreate(form, br);
        if (form.getManagerId() != null && shopManagerRepo.existsByManagerIdBusy(form.getManagerId(), null)) {
            br.rejectValue("managerId", "busy", "Manager này đang quản lý shop khác.");
        }
        if (br.hasErrors()) {
            model.addAttribute("pageIndex", page);
            model.addAttribute("size", size);
            return "admin/shops/form";
        }

        // 3) lưu shop
        Shop s = new Shop();
        s.setName(form.getName());
        s.setPhone(StringUtils.hasText(form.getPhone()) ? form.getPhone().trim() : null);
        s.setAddress(StringUtils.hasText(form.getAddress()) ? form.getAddress().trim() : null);
        s.setStatus(form.getStatus());
        shopRepo.save(s);

        // 4) gán manager (tuỳ chọn)
        if (form.getManagerId() != null) {
            User manager = userRepo.findById(form.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager không tồn tại"));
            shopManagerRepo.save(ShopManager.builder().shop(s).manager(manager).build());
        }

        ra.addFlashAttribute("success", "Đã tạo cửa hàng.");
        return "redirect:/admin/shops?page=" + page + "&size=" + size;
    }

    // ------ EDIT FORM ------
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model,
                           RedirectAttributes ra) {

        Optional<Shop> opt = shopRepo.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Cửa hàng không tồn tại.");
            return "redirect:/admin/shops?page=" + page + "&size=" + size;
        }
        Shop s = opt.get();
        Optional<ShopManager> sm = shopManagerRepo.findByShopIdWithManager(id);

        ShopDTO form = ShopDTO.builder()
                .id(s.getId())
                .name(s.getName())
                .address(s.getAddress())
                .phone(s.getPhone())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .managerId(sm.map(x -> x.getManager() != null ? x.getManager().getId() : null).orElse(null))
                .managerName(sm.map(x -> x.getManager() != null ? x.getManager().getFullName() : null).orElse(null))
                .build();

        model.addAttribute("form", form);
        model.addAttribute("pageIndex", page);
        model.addAttribute("size", size);
        return "admin/shops/form";
    }

    // ------ UPDATE ------
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") ShopDTO form,
                         BindingResult br,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model,
                         RedirectAttributes ra) {

        Shop s = shopRepo.findById(id).orElse(null);
        if (s == null) {
            ra.addFlashAttribute("error", "Cửa hàng không tồn tại.");
            return "redirect:/admin/shops?page=" + page + "&size=" + size;
        }

        if (br.hasErrors()) {
            model.addAttribute("pageIndex", page);
            model.addAttribute("size", size);
            return "admin/shops/form";
        }

        normalize(form);
        validateUniqueUpdate(form, id, br);
        if (form.getManagerId() != null && shopManagerRepo.existsByManagerIdBusy(form.getManagerId(), id)) {
            br.rejectValue("managerId", "busy", "Manager này đang quản lý shop khác.");
        }
        if (br.hasErrors()) {
            model.addAttribute("pageIndex", page);
            model.addAttribute("size", size);
            return "admin/shops/form";
        }

        s.setName(form.getName());
        s.setPhone(StringUtils.hasText(form.getPhone()) ? form.getPhone().trim() : null);
        s.setAddress(StringUtils.hasText(form.getAddress()) ? form.getAddress().trim() : null);
        s.setStatus(form.getStatus());
        shopRepo.save(s);

        Optional<ShopManager> linkOpt = shopManagerRepo.findByShop_Id(id);
        Long newManagerId = form.getManagerId();

        if (newManagerId == null) {
            linkOpt.ifPresent(shopManagerRepo::delete);
        } else {
            User manager = userRepo.findById(newManagerId)
                    .orElseThrow(() -> new IllegalArgumentException("Manager không tồn tại"));
            if (linkOpt.isPresent()) {
                ShopManager link = linkOpt.get();
                link.setManager(manager);
                shopManagerRepo.save(link);
            } else {
                shopManagerRepo.save(ShopManager.builder().shop(s).manager(manager).build());
            }
        }

        ra.addFlashAttribute("success", "Đã cập nhật cửa hàng.");
        return "redirect:/admin/shops?page=" + page + "&size=" + size;
    }

    // ------ DELETE ------
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         RedirectAttributes ra) {

        if (!shopRepo.existsById(id)) {
            ra.addFlashAttribute("error", "Cửa hàng không tồn tại.");
            return "redirect:/admin/shops?page=" + page + "&size=" + size;
        }
        if (productRepo.existsByShop_Id(id)) {
            ra.addFlashAttribute("error", "Không thể xoá: cửa hàng còn sản phẩm.");
            return "redirect:/admin/shops?page=" + page + "&size=" + size;
        }
        shopRepo.deleteById(id);
        ra.addFlashAttribute("success", "Đã xoá cửa hàng.");
        return "redirect:/admin/shops?page=" + page + "&size=" + size;
    }

    // ------ AJAX cho combobox ------
    @GetMapping(value = "/managers/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> searchManagers(
            @RequestParam("q") String q,
            @RequestParam(value = "currentShopId", required = false) Long currentShopId) {

        String kw = (q == null) ? "" : q.trim();

        // lấy user có role MANAGER (role_id = 2)
        List<User> candidates = userRepo.searchByRoleAndKeyword(2L, kw);

        // loại manager đang quản lý shop khác (trừ shop đang edit)
        return candidates.stream()
                .filter(u -> !shopManagerRepo.existsByManagerIdBusy(u.getId(), currentShopId))
                .limit(20)
                .map(u -> {
                    String name = (u.getFullName() != null && !u.getFullName().isBlank())
                            ? u.getFullName() : u.getEmail();
                    String desc = (u.getEmail() != null ? u.getEmail() : "");

                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("text", name);
                    m.put("desc", desc);
                    return m;
                })
                .toList();
    }


    // ===== helpers =====
    private void normalize(ShopDTO f) {
        if (f.getName() != null)
            f.setName(f.getName().trim());
        if (f.getAddress() != null)
            f.setAddress(f.getAddress().trim());
        if (f.getPhone() != null)
            f.setPhone(f.getPhone().trim());
    }

    private void validateUniqueCreate(ShopDTO f, BindingResult br) {
        if (shopRepo.existsByNameIgnoreCase(f.getName()))
            br.rejectValue("name", "dup", "Tên cửa hàng đã tồn tại");
        if (StringUtils.hasText(f.getPhone()) && shopRepo.existsByPhone(f.getPhone()))
            br.rejectValue("phone", "dup", "Số điện thoại đã tồn tại");
        if (StringUtils.hasText(f.getAddress()) && shopRepo.existsByAddressIgnoreCase(f.getAddress()))
            br.rejectValue("address", "dup", "Địa chỉ đã tồn tại");
    }

    private void validateUniqueUpdate(ShopDTO f, Long id, BindingResult br) {
        if (shopRepo.existsByNameIgnoreCaseAndIdNot(f.getName(), id))
            br.rejectValue("name", "dup", "Tên cửa hàng đã tồn tại");
        if (StringUtils.hasText(f.getPhone()) && shopRepo.existsByPhoneAndIdNot(f.getPhone(), id))
            br.rejectValue("phone", "dup", "Số điện thoại đã tồn tại");
        if (StringUtils.hasText(f.getAddress()) && shopRepo.existsByAddressIgnoreCaseAndIdNot(f.getAddress(), id))
            br.rejectValue("address", "dup", "Địa chỉ đã tồn tại");
    }
}
