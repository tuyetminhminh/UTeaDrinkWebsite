# ✅ PAYOS TEST CHECKLIST

## 🚀 TRƯỚC KHI TEST

### 1. Kiểm tra Database
```sql
-- Kiểm tra xem 2 columns mới đã được thêm chưa
SELECT TOP 1 qr_code_url, checkout_url FROM payment_transactions;
```

**Nếu báo lỗi "Invalid column name":**
- Restart Spring Boot application (Hibernate sẽ tự động thêm columns)
- Hoặc chạy SQL manually (xem PAYOS_FIX_SUMMARY.md)

### 2. Kiểm tra PayOS Config
Mở `application.properties` và xác nhận:
```properties
payos.client-id=c3053ac8-0674-4659-b4e3-0bc3cbc08378
payos.api-key=d74a34cc-d298-4691-96b9-e0e2b7038809
payos.checksum-key=d652838717c969da3060709b4b4a1b56fc294d77ca45f3cdeecac0b87103cd73
payos.api-url=https://api-sandbox.payos.vn  # Sandbox URL
```

✅ Đảm bảo đang dùng **Sandbox** để test!

---

## 📝 TEST CASES

### ✅ Test 1: QR Code hiển thị đúng

**Steps:**
1. Đăng nhập vào website
2. Thêm 1 sản phẩm vào giỏ hàng
3. Checkout → Chọn **PayOS**
4. Click "Đặt hàng"

**Expected Result:**
- ✅ Chuyển đến trang thanh toán PayOS
- ✅ Có đếm ngược 10:00
- ✅ QR code **HIỂN THỊ** (không loading mãi)
- ✅ QR code có thể quét được
- ✅ Có nút xanh "Mở trang thanh toán PayOS"

**Actual Result:** [ PASS / FAIL ]

**Screenshot:** (nếu fail, attach screenshot)

---

### ✅ Test 2: Thanh toán thành công

**Steps:**
1. Làm theo Test 1
2. Click nút "Mở trang thanh toán PayOS" (hoặc quét QR)
3. Thanh toán trên PayOS

**Expected Result:**
- ✅ Trang polling tự động chuyển về "Cảm ơn" sau khi thanh toán
- ✅ Order status = PAID trong database
- ✅ Giỏ hàng bị xóa items đã chọn

**Actual Result:** [ PASS / FAIL ]

---

### ✅ Test 3: Items total validation

**Steps:**
1. Mở console/terminal để xem log
2. Tạo đơn hàng PayOS
3. Kiểm tra log

**Expected Result:**
```
║ Items detail:
║   [0] name='Tra sua tran chau', qty=2, price=25000
║   [1] name='Phi van chuyen va dich vu', qty=1, price=15000
╠═══════════════════════════════════════════
║ Final items total: 65000 (must equal amount: 65000)
```

- ✅ Không có dòng "CRITICAL: Items total still not equal to amount"
- ✅ Final items total = amount

**Actual Result:** [ PASS / FAIL ]

**Log output:** (copy log ở đây nếu fail)

---

### ✅ Test 4: Discount/Voucher handling

**Steps:**
1. Tạo đơn hàng với voucher giảm giá
2. Chọn PayOS
3. Kiểm tra log items

**Expected Result:**
- ✅ Items được điều chỉnh đúng (giá giảm)
- ✅ Items total vẫn bằng amount
- ✅ PayOS API không trả về lỗi code 20

**Actual Result:** [ PASS / FAIL ]

---

### ✅ Test 5: MoMo và VNPay không bị ảnh hưởng

**Steps:**
1. Tạo đơn hàng với **MoMo**
2. Kiểm tra QR MoMo vẫn hiển thị bình thường
3. Tạo đơn hàng với **VNPay**
4. Kiểm tra QR VNPay vẫn hiển thị bình thường

**Expected Result:**
- ✅ MoMo hoạt động như cũ (không bị ảnh hưởng)
- ✅ VNPay hoạt động như cũ (không bị ảnh hưởng)

**Actual Result:** [ PASS / FAIL ]

---

## 🐛 COMMON ISSUES

### Issue 1: QR không hiển thị (loading mãi)

**Nguyên nhân:**
- PayOS API không trả về `qrCode`
- Database chưa có columns mới

**Fix:**
1. Kiểm tra log: `PayOS Response - Body:`
2. Kiểm tra `txn.getQrCodeUrl()` có giá trị không
3. Restart application để Hibernate tạo columns

---

### Issue 2: PayOS trả về lỗi code 20

**Nguyên nhân:**
- Items total ≠ amount
- Items name có ký tự đặc biệt

**Fix:**
- ✅ ĐÃ FIX: Code tự động điều chỉnh items total
- ✅ ĐÃ FIX: Normalize tên sản phẩm (xóa dấu)

Nếu vẫn lỗi → Kiểm tra log `Items detail:` để debug

---

### Issue 3: PayOS trả về lỗi 401/403

**Nguyên nhân:**
- Sai client-id hoặc api-key
- Tài khoản PayOS chưa active

**Fix:**
1. Kiểm tra lại keys trong `application.properties`
2. Đăng nhập PayOS dashboard để activate account
3. Đảm bảo dùng đúng môi trường (sandbox vs production)

---

## 📊 TEST RESULT SUMMARY

| Test Case | Status | Notes |
|-----------|--------|-------|
| Test 1: QR hiển thị | [ ] PASS / [ ] FAIL | |
| Test 2: Thanh toán thành công | [ ] PASS / [ ] FAIL | |
| Test 3: Items validation | [ ] PASS / [ ] FAIL | |
| Test 4: Discount handling | [ ] PASS / [ ] FAIL | |
| Test 5: MoMo/VNPay OK | [ ] PASS / [ ] FAIL | |

**Overall:** [ ] ✅ ALL PASSED / [ ] ❌ SOME FAILED

---

## 📸 SCREENSHOTS REQUIRED

Nếu test thành công, chụp screenshot:
1. Trang thanh toán PayOS với QR code hiển thị
2. Trang "Cảm ơn" sau khi thanh toán thành công
3. Console log hiển thị items validation đúng

---

## 🎉 NEXT STEPS

Sau khi tất cả test PASS:
1. Test trên production environment (với PayOS production keys)
2. Test với nhiều sản phẩm, nhiều voucher khác nhau
3. Test trên mobile và desktop
4. Stress test: tạo nhiều payment link cùng lúc

---

**Good luck! 🚀**

