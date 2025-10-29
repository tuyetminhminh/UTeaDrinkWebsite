//package net.codejava.utea.admin.controller;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.RequiredArgsConstructor;
//
//import net.codejava.utea.catalog.entity.ProductCategory;
//import net.codejava.utea.catalog.repository.ProductCategoryRepository;
//import net.codejava.utea.catalog.service.ProductCategoryService;
//
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@PreAuthorize("hasRole('ADMIN')")
//@Controller
//@RequestMapping("/admin/categories")
//@RequiredArgsConstructor
//public class AdminCategoryController {
//
//    private final ProductCategoryRepository categoryRepo;
//    private final ProductCategoryService categoryService;
//
//    /* ========== LIST + SEARCH ========== */
//    @GetMapping
//    public String index(@RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page,
//                        @RequestParam(defaultValue = "10") int size, Model model) {
//        int p = Math.max(page, 1) - 1;
//        int s = normalizeSize(size);
//
//        Page<ProductCategory> pageData = (q == null || q.isBlank()) ? categoryRepo.findAll(PageRequest.of(p, s))
//                : categoryRepo.findByNameContainingIgnoreCase(q.trim(), PageRequest.of(p, s));
//
//        int totalPages = Math.max(pageData.getTotalPages(), 1);
//        if (page > totalPages && totalPages > 0) { // Thêm điều kiện totalPages > 0
//            return buildBackUrl(q, totalPages, s);
//        }
//
//        model.addAttribute("page", pageData);
//        model.addAttribute("q", q);
//        model.addAttribute("pageIndex", Math.max(page, 1));
//        model.addAttribute("size", s);
//        // ĐÃ SỬA: Cập nhật danh sách kích thước trang
//        model.addAttribute("sizes", List.of(5, 10, 15, 20, 50, 100));
//        return "admin/categories/index";
//    }
//
//    /* ========== NEW FORM ========== */
//    @GetMapping("/new")
//    public String newForm(@RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page,
//                          @RequestParam(defaultValue = "10") int size, Model model) {
//        CategoryForm form = new CategoryForm();
//        form.setActive(Boolean.TRUE); // nếu entity có cột active
//        keepNav(model, q, page, size);
//        model.addAttribute("form", form);
//        return "admin/categories/form";
//    }
//
//    /* ========== CREATE ========== */
//    @PostMapping
//    public String create(@ModelAttribute CategoryForm form, @RequestParam(required = false) String q,
//                         @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, Model model) {
//        var errors = validateForm(form, true);
//        if (!errors.isEmpty()) {
//            model.addAttribute("errors", errors);
//            model.addAttribute("form", form);
//            keepNav(model, q, page, size);
//            return "admin/categories/form";
//        }
//        categoryService.createCategory(form);
//        return buildBackUrl(q, page, size);
//    }
//
//    /* ========== EDIT FORM ========== */
//    @GetMapping("/{id}/edit")
//    public String editForm(@PathVariable Long id, @RequestParam(required = false) String q,
//                           @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, Model model) {
//        ProductCategory c = categoryRepo.findById(id).orElseThrow();
//        CategoryForm form = new CategoryForm();
//        form.setId(c.getId());
//        form.setName(c.getName());
//        form.setDescription(c.getDescription());
//        form.setActive("ACTIVE".equalsIgnoreCase(c.getStatus())); // nếu entity có cột active
//        keepNav(model, q, page, size);
//        model.addAttribute("form", form);
//        return "admin/categories/form";
//    }
//
//    /* ========== UPDATE ========== */
//    @PostMapping("/{id}")
//    public String update(@PathVariable Long id, @ModelAttribute CategoryForm form,
//                         @RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page,
//                         @RequestParam(defaultValue = "10") int size, Model model) {
//        var errors = validateForm(form, false);
//        if (!errors.isEmpty()) {
//            model.addAttribute("errors", errors);
//            model.addAttribute("form", form);
//            keepNav(model, q, page, size);
//            return "admin/categories/form";
//        }
//
//        categoryService.updateCategory(id, form);
//        return buildBackUrl(q, page, size);
//    }
//
//    /* ========== DELETE (hard) ========== */
//    @PostMapping("/{id}/delete")
//    public String delete(@PathVariable Long id, @RequestParam(required = false) String q,
//                         @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
//                         RedirectAttributes redirectAttributes) { // ĐÃ SỬA: Thêm RedirectAttributes
//        try {
//            categoryRepo.deleteById(id);
//            // Thêm thông báo thành công (tùy chọn)
//            redirectAttributes.addFlashAttribute("success", "Đã xóa danh mục thành công.");
//        } catch (DataIntegrityViolationException ex) {
//            // ĐÃ SỬA: Gửi thông báo lỗi về trang index thông qua flash attribute
//            redirectAttributes.addFlashAttribute("error", "Không thể xóa: Danh mục đang được sử dụng bởi sản phẩm.");
//        }
//        // Luôn điều hướng về trang danh sách
//        return buildBackUrl(q, page, size);
//    }
//
//    /* ========== THÊM MỚI: KÍCH HOẠT / VÔ HIỆU HÓA ========== */
//    @PostMapping("/{id}/activate")
//    public String activate(@PathVariable Long id,
//                           @RequestParam(required = false) String q,
//                           @RequestParam(defaultValue = "1") int page,
//                           @RequestParam(defaultValue = "10") int size,
//                           RedirectAttributes redirectAttributes) { // ĐÃ SỬA: Thêm RedirectAttributes
//        try {
//            categoryService.updateStatus(id, "ACTIVE");
//            redirectAttributes.addFlashAttribute("success", "Đã kích hoạt danh mục thành công.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi kích hoạt danh mục.");
//        }
//        return buildBackUrl(q, page, size);
//    }
//
//    @PostMapping("/{id}/deactivate")
//    public String deactivate(@PathVariable Long id,
//                             @RequestParam(required = false) String q,
//                             @RequestParam(defaultValue = "1") int page,
//                             @RequestParam(defaultValue = "10") int size,
//                             RedirectAttributes redirectAttributes) { // ĐÃ SỬA: Thêm RedirectAttributes
//        try {
//            categoryService.updateStatus(id, "INACTIVE");
//            redirectAttributes.addFlashAttribute("success", "Đã vô hiệu hóa danh mục thành công.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi vô hiệu hóa danh mục.");
//        }
//        return buildBackUrl(q, page, size);
//    }
//
//
//    /* ========== Helpers ========== */
//    private int normalizeSize(int size) {
//        // ĐÃ SỬA: Cập nhật danh sách kích thước hợp lệ
//        List<Integer> allowed = List.of(5, 10, 15, 20, 50, 100);
//        if (allowed.contains(size))
//            return size;
//        if (size < 1)
//            return 10;
//        return Math.min(size, 200);
//    }
//
//    // ... (các helper khác không đổi) ...
//    private void keepNav(Model model, String q, int page, int size) {
//        model.addAttribute("q", q);
//        model.addAttribute("pageIndex", Math.max(page, 1));
//        model.addAttribute("size", normalizeSize(size));
//    }
//
//    private String buildBackUrl(String q, int page, int size) {
//        int p = Math.max(page, 1);
//        int s = normalizeSize(size);
//        String url = String.format("redirect:/admin/categories?page=%d&size=%d", p, s);
//        if (q != null && !q.isBlank())
//            url += "&q=" + q.trim();
//        return url;
//    }
//
//    private List<String> validateForm(CategoryForm f, boolean creating) {
//        var errs = new ArrayList<String>();
//        if (f.getName() == null || f.getName().isBlank()) {
//            errs.add("Tên danh mục là bắt buộc.");
//        } else if (f.getName().trim().length() > 255) {
//            errs.add("Tên danh mục tối đa 255 ký tự.");
//        } else {
//            boolean dup = creating ? categoryRepo.existsByNameIgnoreCase(f.getName().trim())
//                    : categoryRepo.existsByNameIgnoreCaseAndIdNot(f.getName().trim(), f.getId());
//            if (dup)
//                errs.add("Tên danh mục đã tồn tại.");
//        }
//        // description optional
//        return errs;
//    }
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class CategoryForm {
//        private Long id;
//        private String name;
//        private String description;
//        private Boolean active; // tùy chọn: nếu entity có cột này
//    }
//
//}

package net.codejava.utea.admin.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import net.codejava.utea.catalog.entity.ProductCategory;
import net.codejava.utea.catalog.repository.ProductCategoryRepository;
import net.codejava.utea.catalog.service.ProductCategoryService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final ProductCategoryRepository categoryRepo;
    private final ProductCategoryService categoryService;

    /* ========== LIST + SEARCH ========== */
    @GetMapping
    public String index(@RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size, Model model) {
        int p = Math.max(page, 1) - 1;
        int s = normalizeSize(size);
        var pageable = PageRequest.of(p, s, Sort.by(Sort.Order.asc("name").ignoreCase()));

        Page<ProductCategory> pageData =
                (q == null || q.isBlank())
                        ? categoryRepo.findAll(pageable)
                        : categoryRepo.findByNameContainingIgnoreCase(q.trim(), pageable);

        int totalPages = Math.max(pageData.getTotalPages(), 1);
        if (page > totalPages && totalPages > 0) { // Thêm điều kiện totalPages > 0
            return buildBackUrl(q, totalPages, s);
        }

        model.addAttribute("page", pageData);
        model.addAttribute("q", q);
        model.addAttribute("pageIndex", Math.max(page, 1));
        model.addAttribute("size", s);
        // ĐÃ SỬA: Cập nhật danh sách kích thước trang
        model.addAttribute("sizes", new int[]{5, 10, 20, 50, 100});
        return "admin/categories/index";
    }

    /* ========== NEW FORM ========== */
    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size, Model model) {
        CategoryForm form = new CategoryForm();
        form.setActive(Boolean.TRUE); // nếu entity có cột active
        keepNav(model, q, page, size);
        model.addAttribute("form", form);
        return "admin/categories/form";
    }

    /* ========== CREATE ========== */
    @PostMapping
    public String create(@ModelAttribute CategoryForm form, @RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        var errors = validateForm(form, true);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("form", form);
            keepNav(model, q, page, size);
            return "admin/categories/form";
        }
        categoryService.createCategory(form);
        return buildBackUrl(q, page, size);
    }

    /* ========== EDIT FORM ========== */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, @RequestParam(required = false) String q,
                           @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        ProductCategory c = categoryRepo.findById(id).orElseThrow();
        CategoryForm form = new CategoryForm();
        form.setId(c.getId());
        form.setName(c.getName());
        form.setDescription(c.getDescription());
        form.setActive("ACTIVE".equalsIgnoreCase(c.getStatus())); // nếu entity có cột active
        keepNav(model, q, page, size);
        model.addAttribute("form", form);
        return "admin/categories/form";
    }

    /* ========== UPDATE ========== */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute CategoryForm form,
                         @RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size, Model model) {
        var errors = validateForm(form, false);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("form", form);
            keepNav(model, q, page, size);
            return "admin/categories/form";
        }

        categoryService.updateCategory(id, form);
        return buildBackUrl(q, page, size);
    }

    /* ========== DELETE (hard) ========== */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
                         RedirectAttributes redirectAttributes) { // ĐÃ SỬA: Thêm RedirectAttributes
        try {
            categoryRepo.deleteById(id);
            // Thêm thông báo thành công (tùy chọn)
            redirectAttributes.addFlashAttribute("success", "Đã xóa danh mục thành công.");
        } catch (DataIntegrityViolationException ex) {
            // ĐÃ SỬA: Gửi thông báo lỗi về trang index thông qua flash attribute
            redirectAttributes.addFlashAttribute("error", "Không thể xóa: Danh mục đang được sử dụng bởi sản phẩm.");
        }
        // Luôn điều hướng về trang danh sách
        return buildBackUrl(q, page, size);
    }

    /* ========== THÊM MỚI: KÍCH HOẠT / VÔ HIỆU HÓA ========== */
    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id,
                           @RequestParam(required = false) String q,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           RedirectAttributes redirectAttributes) { // ĐÃ SỬA: Thêm RedirectAttributes
        try {
            categoryService.updateStatus(id, "ACTIVE");
            redirectAttributes.addFlashAttribute("success", "Đã kích hoạt danh mục thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi kích hoạt danh mục.");
        }
        return buildBackUrl(q, page, size);
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id,
                             @RequestParam(required = false) String q,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size,
                             RedirectAttributes redirectAttributes) { // ĐÃ SỬA: Thêm RedirectAttributes
        try {
            categoryService.updateStatus(id, "INACTIVE");
            redirectAttributes.addFlashAttribute("success", "Đã vô hiệu hóa danh mục thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi vô hiệu hóa danh mục.");
        }
        return buildBackUrl(q, page, size);
    }


    /* ========== Helpers ========== */
    private int normalizeSize(int size) {
        // ĐÃ SỬA: Cập nhật danh sách kích thước hợp lệ
        List<Integer> allowed = List.of(5, 10, 20, 50, 100);
        if (allowed.contains(size))
            return size;
        if (size < 1)
            return 10;
        return Math.min(size, 200);
    }

    // ... (các helper khác không đổi) ...
    private void keepNav(Model model, String q, int page, int size) {
        model.addAttribute("q", q);
        model.addAttribute("pageIndex", Math.max(page, 1));
        model.addAttribute("size", normalizeSize(size));
    }

    private String buildBackUrl(String q, int page, int size) {
        int p = Math.max(page, 1);
        int s = normalizeSize(size);
        String url = String.format("redirect:/admin/categories?page=%d&size=%d", p, s);
        if (q != null && !q.isBlank())
            url += "&q=" + q.trim();
        return url;
    }

    private List<String> validateForm(CategoryForm f, boolean creating) {
        var errs = new ArrayList<String>();
        if (f.getName() == null || f.getName().isBlank()) {
            errs.add("Tên danh mục là bắt buộc.");
        } else if (f.getName().trim().length() > 255) {
            errs.add("Tên danh mục tối đa 255 ký tự.");
        } else {
            boolean dup = creating ? categoryRepo.existsByNameIgnoreCase(f.getName().trim())
                    : categoryRepo.existsByNameIgnoreCaseAndIdNot(f.getName().trim(), f.getId());
            if (dup)
                errs.add("Tên danh mục đã tồn tại.");
        }
        // description optional
        return errs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryForm {
        private Long id;
        private String name;
        private String description;
        private Boolean active; // tùy chọn: nếu entity có cột này
    }

}

