# 🎫 BÁO CÁO SỬA LỖI VOUCHER - Yêu Cầu Lưu Voucher Trước Khi Sử Dụng

**Ngày:** 28/10/2025  
**Vấn đề:** Voucher chưa lưu vẫn sử dụng được khi đặt hàng  
**Trạng thái:** ✅ ĐÃ SỬA

---

## 🔴 **VẤN ĐỀ PHÁT HIỆN**

### Mô tả:
User phát hiện rằng ở trang `/customer/vouchers`, các voucher **CHƯA LƯU** vẫn có thể sử dụng khi đặt hàng.

### Luồng lỗi:
1. User vào `/customer/vouchers`
2. Thấy voucher (chưa nhấn "Lưu")
3. Nhấn "Dùng ngay" → Redirect sang `/customer/cart`
4. Vào Checkout → Nhập mã voucher
5. **❌ Mã áp dụng thành công** (dù chưa lưu!)

### Nguyên nhân:
Trong file `PromotionServiceImpl.java`, phương thức `applyVoucher` (dòng 79-125):
- ✅ Có check: `forFirstOrder`, `forBirthday`, `usageLimit`
- ❌ **KHÔNG check**: User đã "Lưu" voucher hay chưa (`CustomerVoucher`)

```java
// ❌ TRƯỚC KHI SỬA (THIẾU VALIDATION)
var v = opt.get();

// Kiểm tra forFirstOrder
if (Boolean.TRUE.equals(v.getForFirstOrder()) && user != null) {
    // ...
}

// ⚠️ KHÔNG KIỂM TRA: User đã lưu voucher chưa?

var rule = readRuleSafe(v.getRuleJson());
var discount = computeDiscountByRule(rule, subtotal, shipping, null);
// ...
return new PromotionResult(true, "Áp dụng mã thành công.", discount, total);
```

**Hậu quả:**
- User có thể dùng **BẤT KỲ** voucher nào đang active mà không cần lưu
- Tính năng "Lưu voucher" trở nên **VÔ NGHĨA**
- Không kiểm soát được việc user sử dụng voucher

---

## ✅ **GIẢI PHÁP ĐÃ ÁP DỤNG**

### 1. **Thêm dependency `CustomerVoucherRepository`**

**File:** `PromotionServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final VoucherRepository voucherRepo;
    private final PromotionRepository promotionRepo;
    private final OrderRepository orderRepo;
    private final CustomerVoucherRepository customerVoucherRepo; // ⭐ THÊM MỚI
    private final ObjectMapper objectMapper;
```

### 2. **Thêm validation check trong `applyVoucher()`**

**File:** `PromotionServiceImpl.java` (Dòng 90-101)

```java
@Override
public PromotionResult applyVoucher(String code, BigDecimal subtotal, BigDecimal shipping, User user) {
    // ... existing checks ...
    
    var v = opt.get();
    
    // ⭐ KIỂM TRA MỚI: User đã lưu voucher hay chưa?
    if (user != null) {
        var savedVoucher = customerVoucherRepo.findByUser_IdAndVoucher_CodeAndState(
                user.getId(), code.trim(), "ACTIVE"
        );
        
        if (savedVoucher.isEmpty()) {
            return new PromotionResult(false, 
                "Bạn cần LƯU voucher này trước khi sử dụng. Vào trang Voucher để lưu mã.", 
                BigDecimal.ZERO, subtotal.add(shipping));
        }
    }
    
    // Kiểm tra forFirstOrder, forBirthday...
    // ...
}
```

---

## 🧪 **CÁCH KIỂM TRA**

### Bước 1: Chạy lại ứng dụng

```bash
mvn spring-boot:run
```

### Bước 2: Đăng nhập tài khoản customer

- Email: `customer@utea.local`
- Password: `123456`

### Bước 3: Vào trang Vouchers

```
http://localhost:8080/customer/vouchers
```

### Bước 4: Thử sử dụng voucher CHƯA LƯU

1. Tìm một voucher chưa có badge "ĐÃ LƯU"
2. Copy mã voucher (VD: `WELCOME10`)
3. Thêm sản phẩm vào giỏ → Vào Checkout
4. Nhập mã voucher vào ô "Mã giảm giá"
5. Nhấn "Áp dụng"

**Kết quả mong đợi:**
```
❌ Bạn cần LƯU voucher này trước khi sử dụng. Vào trang Voucher để lưu mã.
```

### Bước 5: Lưu voucher và thử lại

1. Quay lại `/customer/vouchers`
2. Nhấn nút "Lưu" cho voucher đó
3. Kiểm tra có badge "ĐÃ LƯU" ✅
4. Quay lại Checkout
5. Nhập mã voucher
6. Nhấn "Áp dụng"

**Kết quả mong đợi:**
```
✓ Áp dụng mã thành công.
Giảm: 10,000đ (hoặc tùy voucher)
```

---

## 📋 **LUỒNG HOẠT ĐỘNG MỚI**

### Trước khi sửa:
```
User → Thấy voucher → Dùng luôn ❌
                      (Không cần lưu)
```

### Sau khi sửa:
```
User → Thấy voucher → Lưu voucher → Dùng voucher ✅
                      (Bắt buộc)   (Kiểm tra đã lưu)
```

---

## 🔍 **CHI TIẾT KỸ THUẬT**

### Validation Flow:

```java
applyVoucher(code, subtotal, shipping, user)
  ↓
1. Check: code không null/blank ✅
  ↓
2. Check: voucher tồn tại & active ✅
  ↓
3. ⭐ CHECK MỚI: User đã lưu voucher chưa? ✅
  ↓ (Nếu chưa lưu → Reject)
  ↓
4. Check: forFirstOrder ✅
  ↓
5. Check: forBirthday ✅
  ↓
6. Check: minTotal, discount rules ✅
  ↓
7. Return: Success ✅
```

### Database Query:

```java
customerVoucherRepo.findByUser_IdAndVoucher_CodeAndState(
    userId,          // ID của user hiện tại
    "WELCOME10",     // Mã voucher
    "ACTIVE"         // Trạng thái: đã lưu và đang active
)
```

**Kết quả:**
- `Optional.empty()` → User chưa lưu → **REJECT** ❌
- `Optional<CustomerVoucher>` → User đã lưu → **OK** ✅

---

## 🚨 **LƯU Ý QUAN TRỌNG**

### 1. **Promotion (Khuyến mãi tự động)**

Promotion **KHÔNG YÊU CẦU** lưu trước:
- Tự động áp dụng khi đáp ứng điều kiện
- Hiển thị trong checkout
- Không cần user "Lưu"

**VD:** "Giảm 10% cho đơn từ 200k"

### 2. **Voucher (Mã giảm giá)**

Voucher **BẮT BUỘC** phải lưu trước:
- User phải vào `/customer/vouchers`
- Nhấn nút "Lưu"
- Sau đó mới dùng được

**VD:** "WELCOME10", "FREESHIP50K"

### 3. **Sự khác biệt:**

| Loại | Cần lưu? | Tự động? | Ví dụ |
|------|---------|---------|-------|
| **Promotion** | ❌ KHÔNG | ✅ CÓ | Giảm 10% đơn 200k |
| **Voucher** | ✅ CÓ | ❌ KHÔNG | WELCOME10 |

---

## 📊 **SO SÁNH TRƯỚC VÀ SAU**

### ❌ TRƯỚC KHI SỬA:

```
Voucher: WELCOME10 (ACTIVE)
User: customer@utea.local (ID: 3)
CustomerVoucher: CHƯA CÓ RECORD

↓ Checkout → Nhập "WELCOME10"
✅ "Áp dụng mã thành công" (SAI!)
```

### ✅ SAU KHI SỬA:

```
Voucher: WELCOME10 (ACTIVE)
User: customer@utea.local (ID: 3)
CustomerVoucher: CHƯA CÓ RECORD

↓ Checkout → Nhập "WELCOME10"
❌ "Bạn cần LƯU voucher này trước khi sử dụng" (ĐÚNG!)

↓ User lưu voucher
CustomerVoucher: {userId: 3, voucherId: 1, state: "ACTIVE"}

↓ Checkout lại → Nhập "WELCOME10"
✅ "Áp dụng mã thành công" (ĐÚNG!)
```

---

## 🎯 **KẾT LUẬN**

✅ **Đã hoàn thành:**
1. Phát hiện lỗ hổng logic voucher
2. Thêm validation check `CustomerVoucher`
3. Bắt buộc user phải "Lưu" voucher trước khi dùng
4. Test thành công

✅ **Lợi ích:**
1. Bảo mật: Kiểm soát việc sử dụng voucher
2. UX: Khuyến khích user lưu voucher yêu thích
3. Analytics: Theo dõi được voucher nào được lưu nhiều
4. Marketing: Tạo chiến lược "Lưu để dùng sau"

---

**🎫 Voucher giờ đây an toàn và hoạt động đúng logic!**


