package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.order.service.OrderService;
import net.codejava.utea.payment.entity.enums.PaymentMethod;
import net.codejava.utea.payment.entity.enums.PaymentStatus;
import net.codejava.utea.payment.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/customer/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentTransactionRepository payRepo;
    private final OrderService orderService;
    // ====== TRANG QR THEO GATEWAY ======
    @GetMapping("/{method}/{orderCode}")
    public String qrPage(@PathVariable String method,
                         @PathVariable String orderCode,
                         @RequestParam(value = "error", required = false) String err,
                         Model model) {
        PaymentMethod pm = PaymentMethod.valueOf(method.toUpperCase());

        var txn = payRepo.findTopByOrderCodeAndMethodOrderByIdDesc(orderCode, pm)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        // Nếu đã thanh toán rồi -> về trang cảm ơn luôn (idempotent)
        if (txn.getStatus() == PaymentStatus.PAID) {
            return "redirect:/customer/orders/thank-you?order=" + orderCode;
        }

        model.addAttribute("orderCode", orderCode);
        model.addAttribute("method", pm.name());      // "MOMO" | "VNPAY"
        model.addAttribute("amount", txn.getAmount());
        model.addAttribute("error", err);

        // Chọn template theo gateway
        return (pm == PaymentMethod.MOMO) ? "customer/pay-momo" : "customer/pay-vnpay";
    }

    // ====== CALLBACK (GIẢ LẬP LOCAL) - THÀNH CÔNG ======
    @PostMapping("/{method}/{orderCode}/success")
    public RedirectView success(@PathVariable String method,
                                @PathVariable String orderCode,
                                RedirectAttributes ra) {
        PaymentMethod pm = PaymentMethod.valueOf(method.toUpperCase());
        var txn = payRepo.findTopByOrderCodeAndMethodOrderByIdDesc(orderCode, pm)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        // Idempotent: nếu đã PAID thì bỏ qua
        if (txn.getStatus() != PaymentStatus.PAID) {
            try {
                // 1) Đổi trạng thái giao dịch
                txn.setStatus(PaymentStatus.PAID);
                payRepo.save(txn);

                // 2) Đánh dấu đơn đã thanh toán + đối soát số tiền + gắn PaymentTransaction
                orderService.markPaid(orderCode, txn.getId().toString(), txn.getAmount());

                ra.addFlashAttribute("toastSuccess", "Thanh toán thành công. Cảm ơn bạn!");
            } catch (IllegalStateException amountMismatch) {
                // Số tiền không khớp với order.total ⇒ rollback trạng thái giao dịch
                txn.setStatus(PaymentStatus.FAILED);
                payRepo.save(txn);
                ra.addFlashAttribute("toastError",
                        "Thanh toán không hợp lệ (số tiền không khớp). Vui lòng liên hệ hỗ trợ.");
                return new RedirectView("/customer/checkout?error=PAY_AMOUNT_MISMATCH", true);
            } catch (Exception ex) {
                // Lỗi khác
                txn.setStatus(PaymentStatus.FAILED);
                payRepo.save(txn);
                ra.addFlashAttribute("toastError",
                        "Có lỗi khi xác nhận thanh toán. Bạn hãy thử lại hoặc chọn phương thức khác.");
                return new RedirectView("/customer/checkout?error=PAY_CONFIRM_ERROR", true);
            }
        }

        return new RedirectView("/customer/orders/thank-you?order=" + orderCode, true);
    }

    // ====== CALLBACK (GIẢ LẬP LOCAL) - THẤT BẠI ======
    @PostMapping("/{method}/{orderCode}/fail")
    public RedirectView fail(@PathVariable String method,
                             @PathVariable String orderCode,
                             RedirectAttributes ra) {
        PaymentMethod pm = PaymentMethod.valueOf(method.toUpperCase());
        payRepo.findTopByOrderCodeAndMethodOrderByIdDesc(orderCode, pm)
                .ifPresent(txn -> {
                    // Nếu chưa thanh toán, cho về FAILED
                    if (txn.getStatus() == PaymentStatus.PENDING) {
                        txn.setStatus(PaymentStatus.FAILED);
                        payRepo.save(txn);
                    }
                });

        ra.addFlashAttribute("toastError",
                "Thanh toán thất bại. Bạn có thể thử lại hoặc chọn phương thức khác.");
        return new RedirectView("/customer/checkout?error=PAY_FAILED", true);
    }
}
