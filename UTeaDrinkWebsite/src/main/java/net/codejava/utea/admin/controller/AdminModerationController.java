package net.codejava.utea.admin.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.chat.dto.UserModerationRow;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.review.dto.ReviewModerationRow;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;
import net.codejava.utea.review.service.AdminModerationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/moderation")
@RequiredArgsConstructor
public class AdminModerationController {

    private final AdminModerationService service;
    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;

    @GetMapping
    public String index(
            // Tham số cho tab review
            @RequestParam(required = false, defaultValue = "PENDING") ReviewStatus status,
            @RequestParam(defaultValue = "", name = "review_kw") String reviewKw,
            @RequestParam(defaultValue = "1", name = "review_page") int reviewPage,
            // Tham số cho tab chat
            @RequestParam(defaultValue = "", name = "user_kw") String userKw,
            @RequestParam(defaultValue = "1", name = "user_page") int userPage,
            Model model) {

        // Dữ liệu cho tab Review
        Pageable reviewPageable = PageRequest.of(Math.max(reviewPage - 1, 0), 10, Sort.by("createdAt").descending());
        Page<ReviewModerationRow> reviewData = reviewRepo.searchForModeration(status, reviewKw, reviewPageable);
        model.addAttribute("reviewPage", reviewData);
        model.addAttribute("status", status);
        model.addAttribute("review_kw", reviewKw);

        // Dữ liệu cho tab User/Chat
        // Dữ liệu cho tab User/Chat
        Pageable userPageable = PageRequest.of(Math.max(userPage - 1, 0), 10, Sort.by(Sort.Direction.ASC, "u.id"));
        Page<UserModerationRow> userData = userRepo.searchForModeration(userKw, userPageable);
        model.addAttribute("userPage", userData);
        model.addAttribute("user_kw", userKw);

        return "admin/moderation/index";
    }

    // --- Review Actions ---
    @PostMapping("/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id, RedirectAttributes ra) {
        service.approveReview(id);
        ra.addFlashAttribute("success", "Đã duyệt review #" + id);
        return "redirect:/admin/moderation";
    }

    @PostMapping("/reviews/{id}/reject")
    public String rejectReview(@PathVariable Long id, RedirectAttributes ra) {
        service.rejectReview(id);
        ra.addFlashAttribute("success", "Đã từ chối review #" + id);
        return "redirect:/admin/moderation";
    }

    // --- Chat Ban Actions ---
    @PostMapping("/users/{id}/ban-chat")
    public String banUserChat(@PathVariable Long id,
                              @RequestParam(defaultValue = "24") int hours,
                              RedirectAttributes ra) {
        service.banChat(id, hours, "Spam");
        ra.addFlashAttribute("success", "Đã khóa chat người dùng #" + id + " trong " + hours + " giờ.");
        // Redirect lại trang moderation với tab chat được active
        return "redirect:/admin/moderation?#chat-tab";
    }

    @PostMapping("/users/{id}/unban-chat")
    public String unbanUserChat(@PathVariable Long id, RedirectAttributes ra) {
        service.unbanChat(id);
        ra.addFlashAttribute("success", "Đã mở khóa chat cho người dùng #" + id);
        return "redirect:/admin/moderation?#chat-tab";
    }
}