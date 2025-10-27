package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.promotion.entity.CustomerVoucher;
import net.codejava.utea.promotion.repository.CustomerVoucherRepository;
import net.codejava.utea.promotion.repository.VoucherRepository;
import net.codejava.utea.promotion.service.CustomerVaultQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/customer/vouchers")
@RequiredArgsConstructor
public class CustomerVoucherController {

    private final CustomerVaultQueryService queryService;
    private final VoucherRepository voucherRepo;
    private final CustomerVoucherRepository cvRepo;

    @GetMapping
    public String index(@AuthenticationPrincipal CustomUserDetails cud,
                        @RequestParam(name="q", required=false) String q,
                        @RequestParam(name="page", defaultValue="0") int page,
                        @RequestParam(name="size", defaultValue="10") int size,
                        @RequestParam(name="sort", defaultValue="newest") String sort,
                        @RequestParam(name="saved", defaultValue="false") boolean saved,
                        Model model) {

        Long userId = cud.getUser().getId();
        var items = queryService.list(userId, q, sort, saved, PageRequest.of(page, size));

        model.addAttribute("items", items);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("saved", saved);
        model.addAttribute("savedCount", cvRepo.findSavedCodesByUserId(userId).size());

        return "customer/vouchers";
    }

    /** Lưu hoặc kích hoạt lại nếu đã có bản ghi (state=REMOVED) */
    @PostMapping("/save")
    public String save(@AuthenticationPrincipal CustomUserDetails cud,
                       @RequestParam("code") String code,
                       @RequestParam(name="q", required=false) String q,
                       @RequestParam(name="sort", defaultValue="newest") String sort,
                       @RequestParam(name="saved", defaultValue="false") boolean saved) {

        Long userId = cud.getUser().getId();
        if (code != null && !code.isBlank()) {
            voucherRepo.findByCodeActiveNow(code.trim(), LocalDateTime.now()).ifPresent(v -> {
                cvRepo.findByUser_IdAndVoucher_Id(userId, v.getId())
                        .ifPresentOrElse(cv -> {
                            // Đã có record (kể cả REMOVED) -> kích hoạt lại
                            cv.setState("ACTIVE");
                            cv.setSavedAt(LocalDateTime.now());
                            cvRepo.save(cv);
                        }, () -> {
                            // Chưa có -> tạo mới
                            cvRepo.save(CustomerVoucher.builder()
                                    .user(cud.getUser())
                                    .voucher(v)
                                    .savedAt(LocalDateTime.now())
                                    .state("ACTIVE")
                                    .build());
                        });
            });
        }
        return "redirect:/customer/vouchers?q="+safe(q)+"&sort="+sort+"&saved="+saved+"&page=0&size=10";
    }

    /** Bỏ lưu -> đổi state=REMOVED */
    @PostMapping("/unsave")
    public String unsave(@AuthenticationPrincipal CustomUserDetails cud,
                         @RequestParam("code") String code,
                         @RequestParam(name="q", required=false) String q,
                         @RequestParam(name="sort", defaultValue="newest") String sort,
                         @RequestParam(name="saved", defaultValue="false") boolean saved) {

        Long userId = cud.getUser().getId();
        if (code != null && !code.isBlank()) {
            cvRepo.findByUser_IdAndVoucher_CodeAndState(userId, code.trim(), "ACTIVE")
                    .ifPresent(cv -> { cv.setState("REMOVED"); cvRepo.save(cv); });
        }
        return "redirect:/customer/vouchers?q="+safe(q)+"&sort="+sort+"&saved="+saved+"&page=0&size=10";
    }

    private String safe(String s){ return (s == null) ? "" : s.replace(" ", "%20"); }
}
