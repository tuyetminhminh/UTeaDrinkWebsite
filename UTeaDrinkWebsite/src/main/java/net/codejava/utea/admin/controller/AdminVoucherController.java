package net.codejava.utea.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.promotion.dto.VoucherForm;
import net.codejava.utea.promotion.dto.VoucherRow;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.repository.VoucherRepository;
import net.codejava.utea.promotion.service.AdminVoucherAppService;
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
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final AdminVoucherAppService app;
    private final VoucherRepository voucherRepo;
    private final ShopRepository shopRepo;

    /* ============== LIST + FILTER + PAGINATION ============== */
    @GetMapping
    public String index(@RequestParam(required = false) PromoScope scope,
                        @RequestParam(defaultValue = "") String kw,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {

        int pageIndex = Math.max(page, 1);     // 1-based cho UI
        int s = normalizeSize(size);
        String keyword = kw == null ? "" : kw.trim();

        Pageable pageable = PageRequest.of(pageIndex - 1, s, Sort.by(Sort.Direction.ASC, "id"));
        Page<VoucherRow> data = voucherRepo.searchRows(scope, keyword, pageable);

        int totalPages = Math.max(data.getTotalPages(), 1);
        if (pageIndex > totalPages) {
            return buildBackUrl(scope, keyword, totalPages, s);
        }

        model.addAttribute("page", data);
        model.addAttribute("pageIndex", pageIndex);
        model.addAttribute("size", s);
        model.addAttribute("sizes", new int[]{10, 20, 50, 100});

        model.addAttribute("kw", keyword);
        model.addAttribute("scope", scope);
        return "admin/vouchers/index";
    }

    /* ============== NEW FORM ============== */
    @GetMapping("/new")
    public String newForm(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) PromoScope scope,
                          @RequestParam(defaultValue = "") String kw,
                          Model model) {
        keepNav(model, scope, kw, page, size);
        model.addAttribute("form", new VoucherForm());
        model.addAttribute("shops", shopRepo.findAll());
        return "admin/vouchers/form";
    }

    /* ============== CREATE ============== */
    @PostMapping
    public String create(@Valid @ModelAttribute("form") VoucherForm form,
                         BindingResult br,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) PromoScope scope,
                         @RequestParam(defaultValue = "") String kw,
                         Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            keepNav(model, scope, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/vouchers/form";
        }
        try {
            app.create(form);
            ra.addFlashAttribute("success", "Đã tạo voucher thành công.");
            return buildBackUrl(scope, kw, page, size);
        } catch (IllegalArgumentException ex) {
            br.reject("err", ex.getMessage());
            keepNav(model, scope, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/vouchers/form";
        }
    }

    /* ============== EDIT FORM ============== */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) PromoScope scope,
                           @RequestParam(defaultValue = "") String kw,
                           Model model, RedirectAttributes ra) {
        try {
            keepNav(model, scope, kw, page, size);
            model.addAttribute("form", app.findForForm(id));
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/vouchers/form";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return buildBackUrl(scope, kw, page, size);
        }
    }

    /* ============== UPDATE ============== */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") VoucherForm form,
                         BindingResult br,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) PromoScope scope,
                         @RequestParam(defaultValue = "") String kw,
                         Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            keepNav(model, scope, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/vouchers/form";
        }
        try {
            app.update(id, form);
            ra.addFlashAttribute("success", "Đã cập nhật voucher.");
            return buildBackUrl(scope, kw, page, size);
        } catch (IllegalArgumentException ex) {
            br.reject("err", ex.getMessage());
            keepNav(model, scope, kw, page, size);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/vouchers/form";
        }
    }

    /* ============== DELETE ============== */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) PromoScope scope,
                         @RequestParam(defaultValue = "") String kw,
                         RedirectAttributes ra) {
        try {
            app.delete(id);
            ra.addFlashAttribute("success", "Đã xóa voucher.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Không thể xóa voucher này.");
        }
        return buildBackUrl(scope, kw, page, size);
    }

    /* ============== Helpers ============== */
    private void keepNav(Model model, PromoScope scope, String kw, int page, int size) {
        model.addAttribute("scope", scope);
        model.addAttribute("kw", kw == null ? "" : kw.trim());
        model.addAttribute("pageIndex", Math.max(page, 1)); // 1-based
        model.addAttribute("size", normalizeSize(size));
        model.addAttribute("sizes", new int[]{10, 20, 50, 100});
    }

    private String buildBackUrl(PromoScope scope, String kw, int page, int size) {
        int p = Math.max(page, 1);
        int s = normalizeSize(size);
        String url = String.format("redirect:/admin/vouchers?page=%d&size=%d", p, s);
        if (scope != null) url += "&scope=" + scope;
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

