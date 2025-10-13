package net.codejava.utea.controller;

import net.codejava.utea.entity.Product;
import net.codejava.utea.repository.ProductCategoryRepository;
import net.codejava.utea.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductGuestController {

    private final ProductService productService;
    private final ProductCategoryRepository categoryRepo;

    public ProductGuestController(ProductService productService,
                                  ProductCategoryRepository categoryRepo) {
        this.productService = productService;
        this.categoryRepo = categoryRepo;
    }

    @GetMapping("/GuestProducts")
    public String listProducts(@RequestParam(value = "categoryId", required = false) Long categoryId,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int size,
                               Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        if (categoryId != null) {
            productPage = productService.getProductsByCategoryPaged(categoryId, pageable);
        } else {
            productPage = productService.getAllAvailableProductsPaged(pageable);
        }

        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("categoryId", categoryId);

        return "guest/products";
    }
}
