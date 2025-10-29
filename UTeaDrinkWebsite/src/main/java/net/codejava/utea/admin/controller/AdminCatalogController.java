//package net.codejava.utea.admin.controller;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.RequiredArgsConstructor;
//import net.codejava.utea.catalog.entity.Product;
//import net.codejava.utea.catalog.entity.ProductCategory;
//import net.codejava.utea.catalog.entity.ProductVariant;
//import net.codejava.utea.catalog.entity.enums.Size;
//import net.codejava.utea.catalog.repository.ProductCategoryRepository;
//import net.codejava.utea.catalog.repository.ProductRepository;
//import net.codejava.utea.catalog.service.AdminProductService;
//import net.codejava.utea.manager.entity.Shop;
//import net.codejava.utea.manager.repository.ShopRepository;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//
//@PreAuthorize("hasRole('ADMIN')")
//@Controller
//@RequestMapping("/admin/products")
//@RequiredArgsConstructor
//public class AdminCatalogController {
//
//    private final ProductRepository productRepo;
//    private final ShopRepository shopRepo;
//    private final ProductCategoryRepository categoryRepo;
//    private final AdminProductService adminProductService; // Sử dụng Service
//
//    // DTO cho Form
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class ProductForm {
//        private Long id;
//        private String name;
//        private String description;
//        private Long categoryId;
//        private Long shopId;
//        private String status;
//        // Chúng ta sẽ quản lý các biến thể
//        private List<VariantForm> variants = new ArrayList<>();
//    }
//
//    @Data@NoArgsConstructor
//    @AllArgsConstructor
//    public static class VariantForm {
//        private Long id; // Dùng khi update
//        private Size size; // S, M, L
//        private BigDecimal price;
//        private Integer volumeMl;
//    }
//
//    /* Danh sách + filter */
//    @GetMapping
//    public String index(
//            @RequestParam(required = false) String q,
//            @RequestParam(required = false) Long shopId,
//            @RequestParam(defaultValue = "ALL") String status,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size,
//            Model model) {
//
//        int p = Math.max(page, 1) - 1;
//        Pageable pageable = PageRequest.of(p, size);
//        Page<Product> pageData = productRepo.adminSearch(q, shopId, status, pageable);
//
//        model.addAttribute("page", pageData);
//        model.addAttribute("pageIndex", Math.max(page, 1));
//        model.addAttribute("size", size);
//        model.addAttribute("q", q);
//        model.addAttribute("shopId", shopId);
//        model.addAttribute("status", status);
//        model.addAttribute("shops", shopRepo.findAll());
//        model.addAttribute("sizes", List.of(5, 10, 20, 50));
//        return "admin/products/index";
//    }
//
//    // ===== NEW FORM =====
//    @GetMapping("/new")
//    public String newForm(Model model, @ModelAttribute("form") ProductForm form) {
//
//        // Tạo sẵn 3 biến thể S, M, L
//        if (form.getVariants() == null || form.getVariants().isEmpty()) {
//            form.getVariants().add(new VariantForm(null, Size.S, null, 350)); // Sửa giá thành null
//            form.getVariants().add(new VariantForm(null, Size.M, null, 500));
//            form.getVariants().add(new VariantForm(null, Size.L, null, 700));
//        }
//        form.setStatus("AVAILABLE");
//
//        addCommonAttributes(model);
//        model.addAttribute("form", form);
//        return "admin/products/form";
//    }
//
//    // ===== CREATE =====
//    @PostMapping
//    public String create(
//            @ModelAttribute("form") ProductForm form, // Dùng ProductForm
//            @RequestParam("images") List<MultipartFile> images,
//            BindingResult bindingResult,
//            RedirectAttributes redirectAttributes) {
//
//        if (bindingResult.hasErrors()) {
//            redirectAttributes.addFlashAttribute("errors", List.of("Dữ liệu không hợp lệ"));
//            return "redirect:/admin/products/new";
//        }
//
//        try {
//            adminProductService.createProductWithVariants(form, images);
//            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errors", List.of(e.getMessage()));
//            redirectAttributes.addFlashAttribute("form", form); // Giữ lại dữ liệu form
//            return "redirect:/admin/products/new";
//        }
//        return "redirect:/admin/products";
//    }
//
//    // ============ UPDATE ============
//    @PostMapping("/{id}")
//    public String update(
//            @PathVariable Long id,
//            @ModelAttribute("form") ProductForm form,
//            @RequestParam("images") List<MultipartFile> images,
//            BindingResult bindingResult,
//            RedirectAttributes redirectAttributes) {
//
//        form.setId(id); // Đảm bảo ID được gán
//        if (bindingResult.hasErrors()) {
//            redirectAttributes.addFlashAttribute("errors", List.of("Dữ liệu không hợp lệ"));
//            return "redirect:/admin/products/" + id + "/edit";
//        }
//
//        try {
//            adminProductService.updateProductWithVariants(form, images);
//            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công.");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errors", List.of(e.getMessage()));
//            redirectAttributes.addFlashAttribute("form", form);
//            return "redirect:/admin/products/" + id + "/edit";
//        }
//        return "redirect:/admin/products";
//    }
//
//    private void addCommonAttributes(Model model) {
//        model.addAttribute("categories", categoryRepo.findAll());
//        model.addAttribute("shops", shopRepo.findAll());
//    }
//
//    // ===== EDIT FORM =====
//    @GetMapping("/{id}/edit")
//    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
//        try {
//            ProductForm form = adminProductService.getProductFormById(id);
//            addCommonAttributes(model);
//            model.addAttribute("form", form);
//            // Lấy ảnh hiện có (nếu cần)
//            model.addAttribute("existingImages", adminProductService.getImagesByProductId(id));
//            return "admin/products/form";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errors", List.of("Không tìm thấy sản phẩm."));
//            return "redirect:/admin/products";
//        }
//    }
//
//    // ẨN SẢN PHẨM
//    @PostMapping("/{id}/hide")
//    public String hide(@PathVariable Long id,
//                       @RequestParam(required = false) String q,
//                       @RequestParam(required = false) Long shopId,
//                       @RequestParam(required = false, defaultValue = "ALL") String status,
//                       @RequestParam(defaultValue = "1") int page,
//                       @RequestParam(defaultValue = "10") int size) {
//        adminProductService.hide(id, null);
//        return buildBackUrl(q, shopId, status, page, size);
//    }
//
//    /* Mở lại */
//    @PostMapping("/{id}/unhide")
//    public String unhide(@PathVariable Long id, @RequestParam(required = false) String q,
//                         @RequestParam(required = false) Long shopId, @RequestParam(required = false) String status,
//                         @RequestParam(defaultValue = "1") int page, @RequestParam(required = false) Integer size) {
//        adminProductService.unhide(id);
//        return buildBackUrl(q, shopId, status, page, size);
//    }
//
//    // XÓA HẲN
//    @PostMapping("/{id}/delete-hard")
//    public String deleteHard(@PathVariable Long id,
//                             @RequestParam(required = false) String q,
//                             @RequestParam(required = false) Long shopId,
//                             @RequestParam(required = false, defaultValue = "ALL") String status,
//                             @RequestParam(defaultValue = "1") int page,
//                             @RequestParam(defaultValue = "10") int size) {
//        adminProductService.hardDelete(id);
//        return buildBackUrl(q, shopId, status, page, size);
//    }
//
//
//
//    /* Cập nhật nhẹ tên/mô tả/giá/cate */
//    @PostMapping("/{id}/update")
//    public String update(@PathVariable Long id, @RequestParam String name,
//                         @RequestParam(required = false) String description, @RequestParam(required = false) BigDecimal basePrice,
//                         @RequestParam(required = false) Long categoryId, @RequestParam(required = false) String q,
//                         @RequestParam(required = false) Long shopId, @RequestParam(required = false) String status,
//                         @RequestParam(defaultValue = "1") int page, @RequestParam(required = false) Integer size) {
//
//        adminProductService.updateBasic(id, name, description, basePrice, categoryId);
//        return buildBackUrl(q, shopId, status, page, size);
//    }
//
//    private int normalizeSize(int size) {
//        // Cho phép 5/10/20/50/100 hoặc số custom trong [1..200]
//        List<Integer> allowed = List.of(5, 10, 20, 50, 100);
//        if (allowed.contains(size))
//            return size;
//        if (size < 1)
//            return 10;
//        return Math.min(size, 200);
//    }
//
//    private String buildBackUrl(String q, Long shopId, String status, int page, Integer size) {
//        String s = (status == null || status.isBlank()) ? "ALL" : status;
//        int p = Math.max(1, page);
//        int z = (size == null ? 10 : normalizeSize(size));
//        // Dùng style query param để tự động encode và tránh "null"
//        String url = String.format("redirect:/admin/products?page=%d&size=%d&status=%s", p, z, s);
//        if (q != null && !q.isBlank())
//            url += "&q=" + q;
//        if (shopId != null)
//            url += "&shopId=" + shopId;
//        return url;
//    }
//}

package net.codejava.utea.admin.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.enums.Size;
import net.codejava.utea.catalog.repository.ProductCategoryRepository;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.catalog.service.AdminProductService;
import net.codejava.utea.manager.repository.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final ProductRepository productRepo;
    private final ShopRepository shopRepo;
    private final ProductCategoryRepository categoryRepo;
    private final AdminProductService adminProductService; // Sử dụng Service

    // DTO cho Form
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductForm {
        private Long id;
        private String name;
        private String description;
        private Long categoryId;
        private Long shopId;
        private String status;
        // Chúng ta sẽ quản lý các biến thể
        private List<VariantForm> variants = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantForm {
        private Long id; // Dùng khi update
        private Size size; // S, M, L
        private BigDecimal price;
        private Integer volumeMl;
    }

    /* Danh sách + filter */
    @GetMapping
    public String index(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long shopId,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int pageSize,
            Model model) {

        int p = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(p, pageSize);
        Page<Product> pageData = productRepo.adminSearch(q, shopId, status, pageable);

        model.addAttribute("page", pageData);
        model.addAttribute("pageIndex", Math.max(page, 1));
        model.addAttribute("size", pageSize);
        model.addAttribute("q", q);
        model.addAttribute("shopId", shopId);
        model.addAttribute("status", status);
        model.addAttribute("shops", shopRepo.findAll());
        model.addAttribute("sizes", new int[] { 5, 10, 20, 50, 100 });
        return "admin/products/index";
    }

    // ===== NEW FORM =====
    @GetMapping("/new")
    public String newForm(Model model, @ModelAttribute("form") ProductForm form) {

        // Tạo sẵn 3 biến thể S, M, L
        if (form.getVariants() == null || form.getVariants().isEmpty()) {
            form.getVariants().add(new VariantForm(null, Size.S, null, 350)); // Sửa giá thành null
            form.getVariants().add(new VariantForm(null, Size.M, null, 500));
            form.getVariants().add(new VariantForm(null, Size.L, null, 700));
        }
        form.setStatus("AVAILABLE");

        addCommonAttributes(model);
        model.addAttribute("form", form);
        return "admin/products/form";
    }

    // ===== CREATE =====
    @PostMapping
    public String create(
            @ModelAttribute("form") ProductForm form, // Dùng ProductForm
            @RequestParam("images") List<MultipartFile> images,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", List.of("Dữ liệu không hợp lệ"));
            return "redirect:/admin/products/new";
        }

        try {
            adminProductService.createProductWithVariants(form, images);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errors", List.of(e.getMessage()));
            redirectAttributes.addFlashAttribute("form", form); // Giữ lại dữ liệu form
            return "redirect:/admin/products/new";
        }
        return "redirect:/admin/products";
    }

    // ============ UPDATE ============
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute("form") ProductForm form,
            @RequestParam("images") List<MultipartFile> images,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        form.setId(id); // Đảm bảo ID được gán
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", List.of("Dữ liệu không hợp lệ"));
            return "redirect:/admin/products/" + id + "/edit";
        }

        try {
            adminProductService.updateProductWithVariants(form, images);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errors", List.of(e.getMessage()));
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/admin/products/" + id + "/edit";
        }
        return "redirect:/admin/products";
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("shops", shopRepo.findAll());
    }

    // ===== EDIT FORM =====
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductForm form = adminProductService.getProductFormById(id);
            addCommonAttributes(model);
            model.addAttribute("form", form);
            // Lấy ảnh hiện có (nếu cần)
            model.addAttribute("existingImages", adminProductService.getImagesByProductId(id));
            return "admin/products/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errors", List.of("Không tìm thấy sản phẩm."));
            return "redirect:/admin/products";
        }
    }

    // ẨN SẢN PHẨM
    @PostMapping("/{id}/hide")
    public String hide(@PathVariable Long id,
                       @RequestParam(required = false) String q,
                       @RequestParam(required = false) Long shopId,
                       @RequestParam(required = false, defaultValue = "ALL") String status,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       RedirectAttributes ra) {
        adminProductService.hide(id, null);
        ra.addAttribute("q", q);
        ra.addAttribute("shopId", shopId);
        ra.addAttribute("status", status);
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        return "redirect:/admin/products";
    }

    /* Mở lại */
    @PostMapping("/{id}/unhide")
    public String unhide(@PathVariable Long id, @RequestParam(required = false) String q,
                         @RequestParam(required = false) Long shopId, @RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "1") int page, @RequestParam(required = false) Integer size,
                         RedirectAttributes ra) {
        adminProductService.unhide(id);
        ra.addAttribute("q", q);
        ra.addAttribute("shopId", shopId);
        ra.addAttribute("status", status);
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        return "redirect:/admin/products";
    }

    // XÓA HẲN
    @PostMapping("/{id}/delete-hard")
    public String deleteHard(@PathVariable Long id,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) Long shopId,
                             @RequestParam(required = false, defaultValue = "ALL") String status,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size, RedirectAttributes ra) {
        adminProductService.hardDelete(id);

        ra.addAttribute("q", q);
        ra.addAttribute("shopId", shopId);
        ra.addAttribute("status", status);
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);

        return "redirect:/admin/products";
    }

    /* Cập nhật nhẹ tên/mô tả/giá/cate */
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @RequestParam String name,
                         @RequestParam(required = false) String description, @RequestParam(required = false) BigDecimal basePrice,
                         @RequestParam(required = false) Long categoryId, @RequestParam(required = false) String q,
                         @RequestParam(required = false) Long shopId, @RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "1") int page, @RequestParam(required = false) Integer size,
                         RedirectAttributes ra) {

        adminProductService.updateBasic(id, name, description, basePrice, categoryId);
        ra.addAttribute("q", q);
        ra.addAttribute("shopId", shopId);
        ra.addAttribute("status", status);
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        return "redirect:/admin/products";
    }


}

