package net.codejava.utea.customer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductCategory;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.catalog.repository.ProductCategoryRepository;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.catalog.repository.ProductVariantRepository;
import net.codejava.utea.review.service.ReviewService;
import net.codejava.utea.review.view.ReviewView;
import net.codejava.utea.review.repository.ReviewRepository;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.order.repository.OrderItemRepository;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.manager.entity.ShopBanner;
import net.codejava.utea.manager.repository.ShopBannerRepository;
import net.codejava.utea.catalog.entity.Topping;
import net.codejava.utea.catalog.service.ToppingService;
import net.codejava.utea.engagement.service.ViewedProductService;
import net.codejava.utea.common.security.CustomUserDetails;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class ProductCusController {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepo;
    private final OrderItemRepository orderItemRepo;
    private final ObjectMapper om = new ObjectMapper();
    private final ShopBannerRepository bannerRepo;
    private final ToppingService toppingService;
    private final ViewedProductService viewedProductService;

    // ------------------- MENU -------------------
    @GetMapping("/menu")
    public String menu(Model model,
                       @RequestParam(value = "page_best", defaultValue = "0") int pageBest,
                       @RequestParam Map<String, String> params) {

        long startTime = System.currentTimeMillis();
        System.out.println("🚀 [MENU] Starting menu page load...");

        final int PAGE_SIZE = 8; // Tăng lên 8 cho 4 cột x 2 hàng

        // 1) Best seller theo soldCount desc (AVAILABLE)
        long t1 = System.currentTimeMillis();
        Pageable bestPg = PageRequest.of(Math.max(0, pageBest), PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "soldCount"));
        Page<Product> bestSellers = productRepo.findByStatus("AVAILABLE", bestPg);
        System.out.println("⏱️ [MENU] Best sellers query: " + (System.currentTimeMillis() - t1) + "ms");

        // 2) Danh mục ACTIVE
        long t2 = System.currentTimeMillis();
        List<ProductCategory> categories = categoryRepo.findAll().stream()
                .filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
        System.out.println("⏱️ [MENU] Categories query: " + (System.currentTimeMillis() - t2) + "ms");

        // 3) Mỗi danh mục phân trang
        long t3 = System.currentTimeMillis();
        Map<Long, Page<Product>> catPages = new LinkedHashMap<>();
        Map<Long, Integer> catPageNums = new LinkedHashMap<>();

        for (ProductCategory cat : categories) {
            String key = "page_c_" + cat.getId();
            int pageIdx = 0;
            if (params.containsKey(key)) {
                try { pageIdx = Math.max(0, Integer.parseInt(params.get(key))); }
                catch (NumberFormatException ignore) {}
            }
            Pageable pg = PageRequest.of(pageIdx, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Product> page = productRepo.findByCategory_IdAndStatus(cat.getId(), "AVAILABLE", pg);
            catPages.put(cat.getId(), page);
            catPageNums.put(cat.getId(), pageIdx);
        }
        System.out.println("⏱️ [MENU] Category products query: " + (System.currentTimeMillis() - t3) + "ms (categories: " + categories.size() + ")");

        // Collect all products để tính rating và soldCount
        long t4 = System.currentTimeMillis();
        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(bestSellers.getContent());
        catPages.values().forEach(page -> allProducts.addAll(page.getContent()));
        System.out.println("⏱️ [MENU] Collected products: " + allProducts.size());

        // Tính rating và soldCount thực
        long t5 = System.currentTimeMillis();
        Map<Long, BigDecimal> ratingMap = computeRatingMap(allProducts);
        System.out.println("⏱️ [MENU] Rating map computation: " + (System.currentTimeMillis() - t5) + "ms");
        
        long t6 = System.currentTimeMillis();
        Map<Long, Integer> soldMap = computeSoldCountMap(allProducts);
        System.out.println("⏱️ [MENU] Sold map computation: " + (System.currentTimeMillis() - t6) + "ms");

        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("pageBest", pageBest);
        model.addAttribute("categories", categories);
        model.addAttribute("catPages", catPages);
        model.addAttribute("catPageNums", catPageNums);
        model.addAttribute("ratingMap", ratingMap);
        model.addAttribute("soldMap", soldMap);

        long t7 = System.currentTimeMillis();
        Sort bannerSort = Sort.by(Sort.Direction.ASC, "sortOrder")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ShopBanner> banners = bannerRepo.findByActiveTrue(bannerSort);
        System.out.println("⏱️ [MENU] Banners query: " + (System.currentTimeMillis() - t7) + "ms");

        model.addAttribute("banners", banners);

        System.out.println("✅ [MENU] Total page load time: " + (System.currentTimeMillis() - startTime) + "ms\n");
        return "customer/menu";
    }

    /**
     * Tính average rating thực từ reviews đã approved - Optimized batch query
     */
    private Map<Long, BigDecimal> computeRatingMap(List<Product> products) {
        Map<Long, BigDecimal> map = new HashMap<>();
        Set<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        
        if (productIds.isEmpty()) return map;

        try {
            // Dùng batch query để tính tất cả cùng lúc (tránh N+1 problem)
            List<Object[]> results = reviewRepo.avgRatingByProducts(productIds, ReviewStatus.APPROVED);
            
            for (Object[] row : results) {
                Long productId = (Long) row[0];
                Double avgRating = (Double) row[1];
                if (avgRating != null && avgRating > 0) {
                    map.put(productId, BigDecimal.valueOf(avgRating));
                }
            }
        } catch (Exception e) {
            System.err.println("Error computing rating map: " + e.getMessage());
        }
        return map;
    }

    /**
     * Tính sold count thực từ order items DELIVERED - Optimized batch query
     */
    private Map<Long, Integer> computeSoldCountMap(List<Product> products) {
        Map<Long, Integer> map = new HashMap<>();
        Set<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        
        if (productIds.isEmpty()) return map;

        try {
            // Dùng batch query để tính tất cả cùng lúc (tránh N+1 problem)
            List<Object[]> results = orderItemRepo.sumQuantityByProductsAndStatus(productIds, OrderStatus.DELIVERED);
            
            for (Object[] row : results) {
                Long productId = (Long) row[0];
                Long quantity = (Long) row[1];
                if (quantity != null && quantity > 0) {
                    map.put(productId, quantity.intValue());
                }
            }
        } catch (Exception e) {
            System.err.println("Error computing sold count map: " + e.getMessage());
        }
        return map;
    }

    // ------------------- PRODUCT DETAIL -------------------
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id,
                                @RequestParam(value = "rating", required = false) Integer rating,  // lọc sao
                                @RequestParam(value = "rp", defaultValue = "0") int reviewPage,    // trang review
                                @RequestParam(value = "back", required = false) String back,       // link quay lại
                                @AuthenticationPrincipal CustomUserDetails userDetails,             // user đã đăng nhập
                                Model model) throws JsonProcessingException {

        Product product = productRepo.findByIdAndStatus(id, "AVAILABLE")
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại hoặc đã ẩn"));

        // ✅ Track viewing (chỉ khi đã đăng nhập)
        if (userDetails != null && userDetails.getUser() != null) {
            viewedProductService.trackView(userDetails.getUser().getId(), id);
        }

        // Biến thể theo giá tăng dần
        List<ProductVariant> variants = variantRepo.findByProduct_IdOrderByPriceAsc(id);

        BigDecimal defaultPrice;
        Long defaultVariantId = null;
        Map<Long, BigDecimal> priceMap = new LinkedHashMap<>();

        if (variants != null && !variants.isEmpty()) {
            defaultPrice = variants.get(0).getPrice();
            defaultVariantId = variants.get(0).getId();
            for (ProductVariant v : variants) {
                priceMap.put(v.getId(), v.getPrice());
            }
        } else {
            defaultPrice = product.getBasePrice();
        }
        String priceMapJson = om.writeValueAsString(priceMap);

        // Related (tối đa 6)
        List<Product> related = (product.getCategory() != null)
                ? productRepo.findByCategory_IdAndStatus(
                product.getCategory().getId(),
                "AVAILABLE",
                PageRequest.of(0, 6)
        ).getContent()
                : Collections.emptyList();

        // Reviews (đã duyệt) + filter sao + phân trang
        final int PAGE_SIZE = 5;
        Page<ReviewView> reviews = reviewService.listApproved(
                product.getId(),
                rating,
                PageRequest.of(Math.max(0, reviewPage), PAGE_SIZE)
        );
        Double avg = reviewService.avgRating(product.getId());
        Map<Integer, Long> counts = reviewService.countByStars(product.getId()); // 1..5

        // ==== Chuẩn bị biến "an toàn" cho template ====
        String backUrl = (back == null || back.isBlank()) ? "/customer/menu" : back;

        List<Integer> stars = Arrays.asList(5, 4, 3, 2, 1);

        int currRp = reviews.getNumber();
        int totalRp = reviews.getTotalPages();
        int prevRp = (currRp > 0) ? (currRp - 1) : 0;
        int nextRp = (totalRp == 0) ? 0 : Math.min(currRp + 1, totalRp - 1);

        List<Integer> rpPages = new ArrayList<>();
        for (int i = 0; i < totalRp; i++) rpPages.add(i);

        // ✅ Check theo TÊN category thay vì ID (an toàn hơn)
        boolean isBakery = product.getCategory() != null && 
                          "Bánh".equalsIgnoreCase(product.getCategory().getName());
        java.util.List<Topping> toppings = java.util.Collections.emptyList();
        if (!isBakery && product.getShop() != null) {
            toppings = toppingService.getToppingsForShop(product.getShop().getId());
        }

        // ==== Gán model ====
        model.addAttribute("isBakery", isBakery);
        model.addAttribute("toppings", toppings);
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("defaultPrice", defaultPrice);
        model.addAttribute("defaultVariantId", defaultVariantId);
        model.addAttribute("priceMapJson", priceMapJson);
        model.addAttribute("relatedProducts", related);

        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", (avg == null) ? "0.0" : String.format(Locale.US, "%.1f", avg));
        model.addAttribute("ratingCounts", counts);
        model.addAttribute("selectedRating", rating);

        model.addAttribute("stars", stars);
        model.addAttribute("rp", currRp);
        model.addAttribute("totalRp", totalRp);
        model.addAttribute("prevRp", prevRp);
        model.addAttribute("nextRp", nextRp);
        model.addAttribute("rpPages", rpPages);

        model.addAttribute("backUrl", backUrl);

        return "customer/product";
    }

    // ------------------- RECENTLY VIEWED PAGE -------------------
    @GetMapping("/recently-viewed")
    public String recentlyViewed(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  Model model) {
        
        // Kiểm tra đăng nhập
        if (userDetails == null || userDetails.getUser() == null) {
            return "redirect:/login";
        }

        Long userId = userDetails.getUser().getId();
        
        // Lấy danh sách sản phẩm đã xem (phân trang 12 sản phẩm/trang)
        final int PAGE_SIZE = 12;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<net.codejava.utea.engagement.entity.ViewedProduct> viewedPage = 
            viewedProductService.getRecentlyViewed(userId, pageable);
        
        // Lấy products từ ViewedProduct
        List<Product> products = viewedPage.getContent().stream()
                .map(net.codejava.utea.engagement.entity.ViewedProduct::getProduct)
                .filter(p -> p != null && "AVAILABLE".equals(p.getStatus()))
                .toList();
        
        // Lấy rating và sold count cho từng product
        Map<Long, Double> ratingMap = new java.util.HashMap<>();
        Map<Long, Integer> soldMap = new java.util.HashMap<>();
        
        for (Product p : products) {
            Double rating = reviewService.avgRating(p.getId());
            ratingMap.put(p.getId(), rating);
            
            // Sold count từ product
            soldMap.put(p.getId(), p.getSoldCount() != null ? p.getSoldCount() : 0);
        }
        
        model.addAttribute("products", products);
        model.addAttribute("ratingMap", ratingMap);
        model.addAttribute("soldMap", soldMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", viewedPage.getTotalPages());
        model.addAttribute("totalItems", viewedPage.getTotalElements());
        
        return "customer/recently-viewed";
    }

    @GetMapping("/search.json")
    @ResponseBody
    public Map<String, Object> searchJson(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size) {

        Pageable pg = PageRequest.of(Math.max(0, page), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        var searchPage = productRepo.search(q == null ? "" : q.trim(), null, null, null, pg);

        var items = searchPage.getContent().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("price", p.getBasePrice());
            m.put("imageUrl", p.getMainImageUrl()); // tên getter theo entity của bạn
            return m;
        }).toList();

        return Map.of(
                "content", items,
                "page", searchPage.getNumber(),
                "totalPages", searchPage.getTotalPages()
        );
    }
}
