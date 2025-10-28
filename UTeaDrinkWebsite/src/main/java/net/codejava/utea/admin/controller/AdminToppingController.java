package net.codejava.utea.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.dto.ToppingForm;
import net.codejava.utea.catalog.dto.ToppingRow;
import net.codejava.utea.catalog.repository.ToppingRepository;
import net.codejava.utea.catalog.service.AdminToppingAppService;
import net.codejava.utea.manager.repository.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/toppings")
@RequiredArgsConstructor
public class AdminToppingController {

    private final AdminToppingAppService app;
    private final ShopRepository shopRepo;
    private final ToppingRepository toppingRepository;

    /* ================= LIST + FILTER + PAGINATION ================= */
    @GetMapping
    public String index(@RequestParam(required = false) Long shopId,
                        @RequestParam(defaultValue = "") String kw,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {

        int pageIndex = Math.max(page, 1);           // 1-based cho UI
        int s = normalizeSize(size);                  // chuẩn hoá size
        String keyword = kw == null ? "" : kw.trim();

        Pageable pageable = PageRequest.of(pageIndex - 1, s, Sort.by(Sort.Direction.ASC, "id"));
        Page<ToppingRow> data = toppingRepository.searchRows(shopId, keyword, pageable);

        // Nếu page vượt tổng trang (thường sau khi xoá), đẩy về trang cuối
        int totalPages = Math.max(data.getTotalPages(), 1);
        if (pageIndex > totalPages) {
            return buildBackUrl(shopId, keyword, totalPages, s);
        }

        model.addAttribute("page", data);
        model.addAttribute("pageIndex", pageIndex);                      // 1-based
        model.addAttribute("size", s);
        model.addAttribute("sizes", new int[]{10, 20, 50, 100});         // cho combobox
        model.addAttribute("kw", keyword);
        model.addAttribute("shopId", shopId);
        model.addAttribute("shops", shopRepo.findAll());
        return "admin/toppings/index";
    }

    /* ================= NEW FORM ================= */
    @GetMapping("/new")
    public String newForm(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Long shopId,
                          @RequestParam(defaultValue = "") String kw,
                          Model model) {

        keepNav(model, shopId, kw, page, size);
        model.addAttribute("form", new ToppingForm());
        model.addAttribute("shops", shopRepo.findAll());
        return "admin/toppings/form";
    }

    /* ================= CREATE ================= */
    @PostMapping
    public String create(@Valid @ModelAttribute("form") ToppingForm form,
                         BindingResult br,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) Long shopId,
                         @RequestParam(defaultValue = "") String kw,
                         Model model,
                         RedirectAttributes ra) {

        if (br.hasErrors()) {
            keepNav(model, shopId, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/toppings/form";
        }
        try {
            app.create(form);
            ra.addFlashAttribute("success", "Đã tạo topping.");
            return buildBackUrl(shopId, kw, page, size);
        } catch (IllegalArgumentException ex) {
            br.reject("err", ex.getMessage());
            keepNav(model, shopId, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/toppings/form";
        }
    }

    /* ================= EDIT FORM ================= */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) Long shopId,
                           @RequestParam(defaultValue = "") String kw,
                           Model model,
                           RedirectAttributes ra) {
        try {
            var t = app.findOrThrow(id);
            var f = new ToppingForm();
            f.setId(t.getId());
            f.setShopId(t.getShop().getId());
            f.setName(t.getName());
            f.setPrice(t.getPrice());
            f.setStatus(t.getStatus());

            keepNav(model, shopId, kw, page, size);
            model.addAttribute("form", f);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/toppings/form";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return buildBackUrl(shopId, kw, page, size);
        }
    }

    /* ================= UPDATE ================= */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") ToppingForm form,
                         BindingResult br,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) Long shopId,
                         @RequestParam(defaultValue = "") String kw,
                         Model model,
                         RedirectAttributes ra) {

        if (br.hasErrors()) {
            keepNav(model, shopId, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/toppings/form";
        }
        try {
            app.update(id, form);
            ra.addFlashAttribute("success", "Đã cập nhật topping.");
            return buildBackUrl(shopId, kw, page, size);
        } catch (IllegalArgumentException ex) {
            br.reject("err", ex.getMessage());
            keepNav(model, shopId, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/toppings/form";
        }
    }

    /* ================= TOGGLE STATUS ================= */
    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) Long shopId,
                         @RequestParam(defaultValue = "") String kw,
                         RedirectAttributes ra) {
        app.toggleStatus(id);
        ra.addFlashAttribute("success", "Đã đổi trạng thái topping.");
        return buildBackUrl(shopId, kw, page, size);
    }

    /* ================= DELETE ================= */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) Long shopId,
                         @RequestParam(defaultValue = "") String kw,
                         RedirectAttributes ra) {
        app.delete(id);
        ra.addFlashAttribute("success", "Đã xóa topping.");
        return buildBackUrl(shopId, kw, page, size);
    }

    /* ================= Helpers ================= */
    private void keepNav(Model model, Long shopId, String kw, int page, int size) {
        model.addAttribute("shopId", shopId);
        model.addAttribute("kw", kw == null ? "" : kw.trim());
        model.addAttribute("pageIndex", Math.max(page, 1));  // 1-based
        model.addAttribute("size", normalizeSize(size));
        model.addAttribute("sizes", new int[]{10, 20, 50, 100});
    }

    private String buildBackUrl(Long shopId, String kw, int page, int size) {
        int p = Math.max(page, 1);
        int s = normalizeSize(size);
        String url = String.format("redirect:/admin/toppings?page=%d&size=%d", p, s);
        if (shopId != null) url += "&shopId=" + shopId;
        if (kw != null && !kw.isBlank()) url += "&kw=" + kw.trim();
        return url;
    }

    private int normalizeSize(int size) {
        List<Integer> allowed = List.of(10, 20, 50, 100);
        if (allowed.contains(size)) return size;
        if (size < 1) return 10;
        return Math.min(size, 200);
    }
}
