package net.codejava.utea.payment.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class PayOSService {

    // ✅ Đã sửa: Khai báo để Spring inject
    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.api-url}")
    private String payosApiUrl; // Dùng cấu hình API URL từ application.properties

    // ✅ Đã sửa: Constructor injection cho RestTemplate (Spring tự động inject)
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();

    public PayOSService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        log.info("PayOSService initialized with RestTemplate");
    }

    /**
     * Tạo link thanh toán PayOS
     * @param internalOrderCode: Mã đơn hàng nội bộ của UTea
     * @param amount: Tổng số tiền (VND)
     */
    public PayOSResponse createPaymentLink(String internalOrderCode, long amount,
                                            String description, String returnUrl,
                                            String cancelUrl, List<Map<String, Object>> rawItems) {
        try {
            // PayOS Order Code phải là long (10 chữ số)
            long payosOrderCode = generateOrderNumber(internalOrderCode);

            // 1. Prepare data (trước khi tạo signature)
            int amountInt = (int) amount;
            String desc = description != null ? description : "Thanh toán đơn hàng " + internalOrderCode;
            List<Map<String, Object>> items = (rawItems != null && !rawItems.isEmpty()) ? rawItems :
                    List.of(Map.of("name", "Đơn hàng " + internalOrderCode, "quantity", 1, "price", amountInt));

            // 2. Tạo SIGNATURE (PayOS v2 YÊU CẦU signature trong request body)
            // Data to sign: các field theo thứ tự ALPHABET: amount, cancelUrl, description, orderCode, returnUrl
            String dataToSign = String.format("amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
                    amountInt, cancelUrl, desc, payosOrderCode, returnUrl);
            String signature = hmacSHA256(dataToSign, checksumKey);

            // 3. Tạo REQUEST BODY
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderCode", payosOrderCode);
            requestBody.put("amount", amountInt);
            requestBody.put("description", desc);
            requestBody.put("returnUrl", returnUrl);
            requestBody.put("cancelUrl", cancelUrl);
            requestBody.put("signature", signature);  // ✅ THÊM SIGNATURE VÀO REQUEST BODY
            requestBody.put("items", items);
            
            // ✅ Log cho debugging
            log.info("╔═════════════════ PAYOS REQUEST ═════════════════");
            log.info("║ URL: {}/v2/payment-requests", payosApiUrl);
            log.info("║ Client-ID: {}", clientId);
            log.info("║ API-Key: {}...", apiKey.substring(0, 10));
            log.info("║ Order Code: {}", payosOrderCode);
            log.info("║ Amount: {}", amount);
            log.info("║ Data to Sign: {}", dataToSign);
            log.info("║ Signature: {}", signature);
            log.info("║ Request Body: {}", gson.toJson(requestBody));
            log.info("╚═════════════════════════════════════════════════");

            // 4. GỌI API với signature
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey); 
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                payosApiUrl + "/v2/payment-requests",
                HttpMethod.POST,
                entity,
                String.class
            );

            // 3. XỬ LÝ PHẢN HỒI
            String responseBody = response.getBody();
            log.info("PayOS Response Status: {}", response.getStatusCode());
            log.info("PayOS Response Body: {}", responseBody);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                String responseCode = jsonResponse.has("code") ? jsonResponse.get("code").getAsString() : "99"; // Mặc định là lỗi
                
                if (!responseCode.equals("00")) {
                    String errorMsg = jsonResponse.has("desc") ? jsonResponse.get("desc").getAsString() : "Lỗi không xác định";
                    log.error("PayOS API Error: Code={}, Message={}", responseCode, errorMsg);
                    throw new RuntimeException("PayOS Error: " + errorMsg);
                }

                if (jsonResponse.has("data") && !jsonResponse.get("data").isJsonNull()) {
                    JsonObject data = jsonResponse.getAsJsonObject("data");
                    
                    String checkoutUrl = data.has("checkoutUrl") && !data.get("checkoutUrl").isJsonNull() 
                        ? data.get("checkoutUrl").getAsString() : "";
                    String qrCode = data.has("qrCode") && !data.get("qrCode").isJsonNull()
                        ? data.get("qrCode").getAsString() : "";

                    log.info("Created PayOS payment link for order: {} (PayOS ID: {})", internalOrderCode, payosOrderCode);
                    
                    return new PayOSResponse(checkoutUrl, qrCode, payosOrderCode);
                } else {
                    log.error("PayOS API response missing 'data' field or data is null");
                    throw new RuntimeException("PayOS response format invalid - data field is missing or null");
                }
            } else {
                log.error("PayOS API returned non-success status: {}", response.getStatusCode());
                throw new RuntimeException("PayOS API returned error status: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("PayOS API HTTP Error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi kết nối PayOS (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("PayOS API Connection Error", e);
            throw new RuntimeException("Không thể kết nối đến PayOS API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating PayOS payment link for order: " + internalOrderCode, e);
            throw new RuntimeException("Không thể tạo link thanh toán PayOS: " + e.getMessage(), e);
        }
    }
    
    /**
     * Xác thực webhook từ PayOS (Sử dụng cho callback/webhook)
     */
    public boolean verifyWebhookSignature(String webhookBody, String receivedSignature) {
        try {
            // PayOS yêu cầu sign trực tiếp từ JSON Body (là string) của webhook, KHÔNG cần sort
            String calculatedSignature = hmacSHA256(webhookBody, checksumKey);
            return calculatedSignature.equals(receivedSignature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * HMAC SHA256
     */
    private String hmacSHA256(String data, String key) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * Convert bytes to hex
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Generate order number từ order code (đã tối ưu để đảm bảo duy nhất và định dạng)
     */
    private long generateOrderNumber(String internalOrderCode) {
        // Dùng timestamp (12 digits) + hash của orderCode (4 digits) = 16 digits
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        int hash = Math.abs(internalOrderCode.hashCode() % 10000);
        String combined = timestamp + String.format("%04d", hash);
        return Long.parseLong(combined);
    }

    /**
     * Response class
     */
    public static class PayOSResponse {
        private final String checkoutUrl;
        private final String qrCode;
        private final long orderCode; // PayOS order code

        public PayOSResponse(String checkoutUrl, String qrCode, long orderCode) {
            this.checkoutUrl = checkoutUrl;
            this.qrCode = qrCode;
            this.orderCode = orderCode;
        }

        public String getCheckoutUrl() {
            return checkoutUrl;
        }

        public String getQrCode() {
            return qrCode;
        }

        public long getOrderCode() {
            return orderCode;
        }
    }
}