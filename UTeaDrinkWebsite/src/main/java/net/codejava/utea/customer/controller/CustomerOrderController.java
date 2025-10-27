package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.customer.service.impl.MyOrderQueryService;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.order.view.CustomerOrderItemView;
import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final MyOrderQueryService queryService;
    private final OrderRepository orderRepo;
    private final ReviewRepository reviewRepo;

    private User currentUser(CustomUserDetails cud){
        if (cud == null) throw new RuntimeException("Chưa đăng nhập");
        User u = new User(); u.setId(cud.getId()); return u;
    }

    // Danh sách theo item (tách từng sản phẩm)
    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails cud,
                       @RequestParam(defaultValue = "ALL") String filter,
                       @RequestParam(defaultValue = "newest") String sort,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "7") int size,
                       Model model) {

        String f;
        switch (filter) {
            case "NEW","CONFIRMED","PREPARING","DELIVERING","DELIVERED","CANCELED" -> f = filter;
            default -> f = "ALL";
        }
        String s = "oldest".equalsIgnoreCase(sort) ? "oldest" : "newest";


        var u = currentUser(cud);
        Page<CustomerOrderItemView> p = queryService.listItems(u, f, s, page, size);

        model.addAttribute("items", p);
        model.addAttribute("filter", f);
        model.addAttribute("sort", s);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "customer/orders";
    }

    // Hủy đơn nếu là chủ đơn và còn NEW/CONFIRMED/PREPARING
    @PostMapping("/{orderCode}/cancel")
    public String cancel(@AuthenticationPrincipal CustomUserDetails cud,
                         @PathVariable String orderCode) {

        var u = currentUser(cud);
        Order o = orderRepo.findByOrderCode(orderCode).orElseThrow();
        if (!o.getUser().getId().equals(u.getId())) {
            throw new RuntimeException("Không có quyền hủy đơn này.");
        }

        if (o.getStatus() == OrderStatus.NEW
                || o.getStatus() == OrderStatus.CONFIRMED
                || o.getStatus() == OrderStatus.PREPARING) {
            o.setStatus(OrderStatus.CANCELED);
            orderRepo.save(o);
        }
        return "redirect:/customer/orders";
    }

    // Tạo/cập nhật review theo từng item
    @PostMapping("/review")
    public String review(@AuthenticationPrincipal CustomUserDetails cud,
                         @RequestParam Long orderItemId,
                         @RequestParam Long productId,
                         @RequestParam String orderCode,
                         @RequestParam Integer rating,
                         @RequestParam String content) {

        var u = currentUser(cud);

        // upsert: tìm review theo (userId + orderItemId)
        Review r = reviewRepo.findAll().stream()
                .filter(x -> x.getOrderItemId()!=null && x.getOrderItemId().equals(orderItemId)
                        && x.getUser()!=null && x.getUser().getId().equals(u.getId()))
                .findFirst().orElse(null);

        if (r == null) {
            var product = new net.codejava.utea.catalog.entity.Product();
            product.setId(productId);

            r = Review.builder()
                    .user(u)
                    .product(product)
                    .orderItemId(orderItemId)
                    .rating(rating)
                    .content(content)
                    // tuỳ chính sách: PENDING / APPROVED
                    .status(ReviewStatus.APPROVED)
                    .build();
        } else {
            r.setRating(rating);
            r.setContent(content);
            r.setStatus(ReviewStatus.APPROVED);
        }
        reviewRepo.save(r);
        return "redirect:/customer/orders";
    }
}
