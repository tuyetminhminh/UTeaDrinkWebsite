package net.codejava.utea.customer.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codejava.utea.order.service.OrderService;
import net.codejava.utea.payment.entity.enums.PaymentMethod;
import net.codejava.utea.payment.entity.enums.PaymentStatus;
import net.codejava.utea.payment.repository.PaymentTransactionRepository;
import net.codejava.utea.payment.service.PayOSService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/customer/pay")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentTransactionRepository payRepo;
    private final OrderService orderService;
    private final PayOSService payOSService;
    
    @Value("${server.port:8080}")
    private String serverPort;
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
        model.addAttribute("method", pm.name());
        model.addAttribute("amount", txn.getAmount());
        model.addAttribute("error", err);

        // Nếu là MBBANK, tạo payment link từ PayOS và lấy QR code
        if (pm == PaymentMethod.MBBANK) {
            try {
                String baseUrl = "http://localhost:" + serverPort;
                String returnUrl = baseUrl + "/customer/pay/mbbank/" + orderCode + "/return";
                String cancelUrl = baseUrl + "/customer/checkout?error=PAYMENT_CANCELLED";
                
                PayOSService.PayOSResponse paymentLink = payOSService.createPaymentLink(
                    orderCode,
                    txn.getAmount().longValue(),
                    "Thanh toán đơn hàng " + orderCode + " - UTeaDrink",
                    returnUrl,
                    cancelUrl,
                    null  // rawItems - để null, service sẽ tự tạo item default
                );
                
                // Lưu PayOS transaction ID vào gateway code
                txn.setGatewayTxnCode(String.valueOf(paymentLink.getOrderCode()));
                payRepo.save(txn);
                
                model.addAttribute("qrCodeUrl", paymentLink.getQrCode());
                model.addAttribute("checkoutUrl", paymentLink.getCheckoutUrl());
                
                log.info("Created PayOS payment link for order: {} with QR: {}", orderCode, paymentLink.getQrCode());
                
            } catch (Exception e) {
                log.error("Error creating PayOS payment link: {}", e.getMessage(), e);
                
                // Fallback: Vẫn hiển thị trang thanh toán nhưng không có QR
                model.addAttribute("error", "Không thể tạo mã QR PayOS: " + e.getMessage() + ". Vui lòng liên hệ hỗ trợ hoặc chọn phương thức thanh toán khác.");
                model.addAttribute("qrCodeUrl", null);
                model.addAttribute("checkoutUrl", null);
                
                // Log chi tiết để debug
                log.error("PayOS Configuration - Client ID exists: {}", payOSService != null);
                log.error("Order details - Code: {}, Amount: {}", orderCode, txn.getAmount());
            }
            return "customer/pay-mbbank";
        }

        // Chọn template theo gateway cho MOMO và VNPAY
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

    // ====== PAYOS WEBHOOK - TỰ ĐỘNG XÁC NHẬN THANH TOÁN ======
    @PostMapping("/payos/webhook")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> payosWebhook(@RequestBody String webhookBody,
                                                              @RequestHeader(value = "x-signature", required = false) String signature) {
        Map<String, Object> response = new HashMap<>();
        Gson gson = new Gson();
        
        try {
            log.info("Received PayOS webhook: {}", webhookBody);
            
            // Xác thực webhook từ PayOS
            if (signature != null && !payOSService.verifyWebhookSignature(webhookBody, signature)) {
                log.warn("Invalid webhook signature");
                response.put("success", false);
                response.put("message", "Invalid signature");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Parse webhook data
            JsonObject webhookData = gson.fromJson(webhookBody, JsonObject.class);
            String code = webhookData.has("code") ? webhookData.get("code").getAsString() : "";
            boolean success = "00".equals(code);
            
            if (success && webhookData.has("data")) {
                JsonObject data = webhookData.getAsJsonObject("data");
                long payosOrderCode = data.get("orderCode").getAsLong();
                
                // Tìm transaction theo PayOS order code
                var txnOpt = payRepo.findByGatewayTxnCode(String.valueOf(payosOrderCode));
                
                if (txnOpt.isPresent()) {
                    var txn = txnOpt.get();
                    String orderCode = txn.getOrderCode();
                    
                    // Kiểm tra nếu chưa được xử lý
                    if (txn.getStatus() == PaymentStatus.PENDING) {
                        // Cập nhật trạng thái giao dịch
                        txn.setStatus(PaymentStatus.PAID);
                        txn.setGatewayPayload(webhookBody);
                        payRepo.save(txn);
                        
                        // Đánh dấu đơn hàng đã thanh toán
                        orderService.markPaid(orderCode, txn.getId().toString(), txn.getAmount());
                        
                        log.info("✅ Payment confirmed via webhook for order: {}", orderCode);
                        
                        response.put("success", true);
                        response.put("message", "Payment confirmed successfully");
                        return ResponseEntity.ok(response);
                    } else {
                        log.info("Payment already processed for order: {}", orderCode);
                        response.put("success", true);
                        response.put("message", "Payment already processed");
                        return ResponseEntity.ok(response);
                    }
                } else {
                    log.warn("Transaction not found for PayOS order code: {}", payosOrderCode);
                    response.put("success", false);
                    response.put("message", "Transaction not found");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                log.warn("Payment failed or cancelled. Code: {}", code);
                response.put("success", false);
                response.put("message", "Payment failed");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error processing PayOS webhook", e);
            response.put("success", false);
            response.put("message", "Internal error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ====== CHECK PAYMENT STATUS (AJAX) ======
    @GetMapping("/mbbank/{orderCode}/check-status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> checkPaymentStatus(@PathVariable String orderCode) {
        Map<String, String> response = new HashMap<>();
        
        var txnOpt = payRepo.findTopByOrderCodeAndMethodOrderByIdDesc(orderCode, PaymentMethod.MBBANK);
        
        if (txnOpt.isPresent()) {
            var txn = txnOpt.get();
            response.put("status", txn.getStatus().name());
            response.put("orderCode", orderCode);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "NOT_FOUND");
            response.put("orderCode", orderCode);
            return ResponseEntity.notFound().build();
        }
    }

    // ====== PAYOS RETURN URL - SAU KHI THANH TOÁN ======
    @GetMapping("/mbbank/{orderCode}/return")
    public RedirectView payosReturn(@PathVariable String orderCode,
                                     @RequestParam(required = false) String status,
                                     RedirectAttributes ra) {
        
        log.info("PayOS return for order: {} with status: {}", orderCode, status);
        
        var txnOpt = payRepo.findTopByOrderCodeAndMethodOrderByIdDesc(orderCode, PaymentMethod.MBBANK);
        
        if (txnOpt.isEmpty()) {
            ra.addFlashAttribute("toastError", "Không tìm thấy giao dịch thanh toán");
            return new RedirectView("/customer/checkout?error=TRANSACTION_NOT_FOUND", true);
        }
        
        var txn = txnOpt.get();
        
        // Kiểm tra trạng thái thanh toán
        if (txn.getStatus() == PaymentStatus.PAID) {
            // Đã thanh toán thành công (webhook đã xử lý)
            ra.addFlashAttribute("toastSuccess", "Thanh toán thành công!");
            return new RedirectView("/customer/orders/thank-you?order=" + orderCode, true);
        } else if ("PAID".equalsIgnoreCase(status) || "00".equals(status)) {
            // Webhook có thể chưa đến, xử lý ngay
            try {
                txn.setStatus(PaymentStatus.PAID);
                payRepo.save(txn);
                
                orderService.markPaid(orderCode, txn.getId().toString(), txn.getAmount());
                
                ra.addFlashAttribute("toastSuccess", "Thanh toán thành công!");
                return new RedirectView("/customer/orders/thank-you?order=" + orderCode, true);
                
            } catch (Exception e) {
                log.error("Error confirming payment on return", e);
                ra.addFlashAttribute("toastError", "Có lỗi xác nhận thanh toán. Vui lòng liên hệ hỗ trợ.");
                return new RedirectView("/customer/checkout?error=PAYMENT_CONFIRM_ERROR", true);
            }
        } else {
            // Thanh toán thất bại hoặc bị hủy
            txn.setStatus(PaymentStatus.FAILED);
            payRepo.save(txn);
            
            ra.addFlashAttribute("toastError", "Thanh toán thất bại hoặc đã bị hủy");
            return new RedirectView("/customer/checkout?error=PAYMENT_FAILED", true);
        }
    }
}
