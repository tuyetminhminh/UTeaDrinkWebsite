package net.codejava.utea.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.promotion.dto.PromotionForm;
import net.codejava.utea.promotion.dto.PromotionRow;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.entity.enums.PromoType;
import net.codejava.utea.promotion.repository.PromotionRepository;
import net.codejava.utea.promotion.service.AdminPromotionAppService;
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
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final AdminPromotionAppService app;
    private final PromotionRepository promoRepo;
    private final ShopRepository shopRepo;

    /* ============== LIST + FILTER + PAGINATION ============== */
    @GetMapping
    public String index(@RequestParam(required = false) PromoScope scope,
                        @RequestParam(required = false) PromoType type,
                        @RequestParam(defaultValue = "") String kw,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {

        int pageIndex = Math.max(page, 1);     // 1-based cho UI
        int s = normalizeSize(size);
        String keyword = kw == null ? "" : kw.trim();

        Pageable pageable = PageRequest.of(pageIndex - 1, s, Sort.by(Sort.Direction.ASC, "id"));
        Page<PromotionRow> data = promoRepo.searchRows(scope, type, keyword, pageable);

        int totalPages = Math.max(data.getTotalPages(), 1);
        if (pageIndex > totalPages) {
            return buildBackUrl(scope, type, keyword, totalPages, s);
        }

        model.addAttribute("page", data);
        model.addAttribute("pageIndex", pageIndex);
        model.addAttribute("size", s);
        model.addAttribute("sizes", new int[]{10, 20, 50, 100});

        model.addAttribute("kw", keyword);
        model.addAttribute("scope", scope);
        model.addAttribute("type", type);
        return "admin/promotions/index";
    }

    /* ============== NEW FORM ============== */
    @GetMapping("/new")
    public String newForm(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) PromoScope scope,
                          @RequestParam(required = false) PromoType type,
                          @RequestParam(defaultValue = "") String kw,
                          Model model) {
        keepNav(model, scope, type, kw, page, size);
        model.addAttribute("form", new PromotionForm());
        model.addAttribute("shops", shopRepo.findAll());
        return "admin/promotions/form";
    }

    /* ============== CREATE ============== */
    @PostMapping
    public String create(@Valid @ModelAttribute("form") PromotionForm form,
                         BindingResult br,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) PromoScope scope,
                         @RequestParam(required = false) PromoType type,
                         @RequestParam(defaultValue = "") String kw,
                         Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            keepNav(model, scope, type, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/promotions/form";
        }
        try {
            app.create(form);
            ra.addFlashAttribute("success", "Đã tạo khuyến mãi thành công.");
            return buildBackUrl(scope, type, kw, page, size);
        } catch (IllegalArgumentException ex) {
            br.reject("err", ex.getMessage());
            keepNav(model, scope, type, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/promotions/form";
        }
    }

    /* ============== EDIT FORM ============== */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) PromoScope scope,
                           @RequestParam(required = false) PromoType type,
                           @RequestParam(defaultValue = "") String kw,
                           Model model, RedirectAttributes ra) {
        try {
            keepNav(model, scope, type, kw, page, size);
            model.addAttribute("form", app.findForForm(id));
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/promotions/form";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return buildBackUrl(scope, type, kw, page, size);
        }
    }

    /* ============== UPDATE ============== */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") PromotionForm form,
                         BindingResult br,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) PromoScope scope,
                         @RequestParam(required = false) PromoType type,
                         @RequestParam(defaultValue = "") String kw,
                         Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            keepNav(model, scope, type, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/promotions/form";
        }
        try {
            app.update(id, form);
            ra.addFlashAttribute("success", "Đã cập nhật khuyến mãi.");
            return buildBackUrl(scope, type, kw, page, size);
        } catch (IllegalArgumentException ex) {
            br.reject("err", ex.getMessage());
            keepNav(model, scope, type, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/promotions/form";
        }
    }

    /* ============== DELETE ============== */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) PromoScope scope,
                         @RequestParam(required = false) PromoType type,
                         @RequestParam(defaultValue = "") String kw,
                         RedirectAttributes ra) {
        try {
            app.delete(id);
            ra.addFlashAttribute("success", "Đã xóa khuyến mãi.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Không thể xóa khuyến mãi này.");
        }
        return buildBackUrl(scope, type, kw, page, size);
    }

    /* ============== Helpers ============== */
    private void keepNav(Model model, PromoScope scope, PromoType type, String kw, int page, int size) {
        model.addAttribute("scope", scope);
        model.addAttribute("type", type);
        model.addAttribute("kw", kw == null ? "" : kw.trim());
        model.addAttribute("pageIndex", Math.max(page, 1)); // 1-based
        model.addAttribute("size", normalizeSize(size));
        model.addAttribute("sizes", new int[]{10, 20, 50, 100});
    }

    private String buildBackUrl(PromoScope scope, PromoType type, String kw, int page, int size) {
        int p = Math.max(page, 1);
        int s = normalizeSize(size);
        String url = String.format("redirect:/admin/promotions?page=%d&size=%d", p, s);
        if (scope != null) url += "&scope=" + scope;
        if (type != null)  url += "&type=" + type;
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
