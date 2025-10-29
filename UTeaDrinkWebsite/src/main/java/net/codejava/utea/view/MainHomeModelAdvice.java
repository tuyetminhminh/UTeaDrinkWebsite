package net.codejava.utea.view;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.repository.ProductCategoryRepository;
import net.codejava.utea.catalog.service.ProductService;
import net.codejava.utea.manager.dto.ShopBannerDTO;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.manager.service.ShopService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import net.codejava.utea.review.dto.ReviewCardDTO;
import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;


import java.util.List;

@ControllerAdvice(assignableTypes = net.codejava.utea.auth.controller.AuthController.class)
@RequiredArgsConstructor
public class MainHomeModelAdvice {

    private final ShopService shopService;
    private final ProductService productService;
    private final ProductCategoryRepository categoryRepo;
    private final ShopRepository shopRepo;
    private final ReviewRepository reviewRepo;

    /* ===== Banners ===== */
    @ModelAttribute("banners")
    public List<ShopBannerDTO> banners() {
        return shopService.getActiveBannersAll();
    }

    @ModelAttribute("hasBanners")
    public boolean hasBanners(@ModelAttribute("banners") List<ShopBannerDTO> banners) {
        return banners != null && !banners.isEmpty();
    }

    /* ===== Anchor để giữ #products trong mọi link ===== */
    @ModelAttribute("anchor")
    public String anchor() {
        return "#products";
    }

    /* ===== Danh mục & danh sách shop (địa chỉ hiển thị ở Map) ===== */
    @ModelAttribute("categories")
    public Object categories() {
        return categoryRepo.findAll();
    }

    @ModelAttribute("shops")
    public List<Shop> shops() {
        return shopRepo.findAll();
    }

    /* ===== Nhận tham số phân trang ===== */
    @ModelAttribute("currentPage")
    public int currentPage(@RequestParam(value = "page", defaultValue = "0") int page) {
        return Math.max(page, 0);
    }

    @ModelAttribute("size")
    public int size(@RequestParam(value = "size", defaultValue = "12") int size) {
        return (size <= 0) ? 12 : size;
    }

    @ModelAttribute("selectedCategoryId")
    public Long selectedCategoryId(@RequestParam(value = "categoryId", required = false) Long categoryId) {
        return categoryId;
    }

    /* ===== Trang sản phẩm + list + tổng số trang (có kẹp trang) ===== */
    @ModelAttribute("productPage")
    public Page<Product> productPage(@ModelAttribute("currentPage") int page,
                                     @ModelAttribute("size") int size,
                                     @ModelAttribute("selectedCategoryId") Long categoryId) {
        var pageable = PageRequest.of(page, size);
        Page<Product> p;

        if (categoryId != null) {
            p = productService.getProductsByCategoryPaged(categoryId, pageable);
        } else {
            p = productService.getAllAvailableProductsPaged(pageable);
        }

        // Nếu người dùng truyền page quá lớn, kẹp về trang cuối
        int total = p.getTotalPages();
        if (total > 0 && page >= total) {
            p = (categoryId != null)
                    ? productService.getProductsByCategoryPaged(categoryId, PageRequest.of(total - 1, size))
                    : productService.getAllAvailableProductsPaged(PageRequest.of(total - 1, size));
        }
        return p;
    }

    @ModelAttribute("products")
    public List<Product> products(@ModelAttribute("productPage") Page<Product> productPage) {
        return productPage.getContent();
    }

    @ModelAttribute("totalPages")
    public int totalPages(@ModelAttribute("productPage") Page<Product> productPage) {
        return productPage.getTotalPages();
    }

    @ModelAttribute("reviewCards")
    public List<ReviewCardDTO> reviewCards() {
        List<ReviewCardDTO> cards;
        try {
            cards = reviewRepo.findTopCardsForHome(ReviewStatus.APPROVED, PageRequest.of(0, 10));
        } catch (Exception e) {
            cards = List.of(); // an toàn nếu DB có vấn đề
        }
        if (cards == null || cards.isEmpty()) {
            // 3 review mẫu
            cards = List.of(
                    new ReviewCardDTO("Đóng gói cẩn thận",
                            "Túi ly chắc chắn, sạch sẽ. Hương vị tươi và vừa miệng, giao nhanh. Rất đáng tiền!"),
                    new ReviewCardDTO("Cà phê ngon",
                            "Đậm nhưng không gắt, hậu vị thơm. Đặt nhiều lần đều ổn định, sẽ tiếp tục ủng hộ."),
                    new ReviewCardDTO("Trà trái cây tuyệt vời",
                            "Vị thanh, ít ngọt đúng yêu cầu. Trái cây tươi, trình bày đẹp mắt, giao đúng giờ.")
            );
        }
        return cards;
    }
}
