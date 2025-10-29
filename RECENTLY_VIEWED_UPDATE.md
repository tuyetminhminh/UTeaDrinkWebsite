# 👁️ CẬP NHẬT: CHỨC NĂNG "ĐÃ XEM GẦN ĐÂY"

## 🔄 THAY ĐỔI THIẾT KẾ

### ❌ Thiết kế cũ (KHÔNG dùng):
- Section hiển thị trên trang chủ
- Tự động load khi vào trang chủ

### ✅ Thiết kế mới (ÁP DỤNG):
- **Link trong header** của customer (dropdown menu)
- **Trang riêng** `/customer/recently-viewed`
- Header đầy đủ như các trang customer khác
- Phân trang 12 sản phẩm/trang

---

## 📋 CÁC FILE ĐÃ THAY ĐỔI

### 1. Header Customer - Thêm Link

**File:** `UTeaDrinkWebsite/src/main/resources/templates/fragments/header-customer.html`

**Thêm mới (dòng 338-341):**
```html
<a th:href="@{/customer/recently-viewed}" class="customer-dropdown-item">
    <i class="bi bi-eye-fill"></i>
    Đã xem gần đây
</a>
```

**Vị trí:** Trong dropdown menu của user, giữa "Kho voucher" và "Hỗ trợ"

### 2. Customer Home - Xóa Code Section

**File:** `UTeaDrinkWebsite/src/main/resources/templates/home/customer-home.html`

**Đã xóa:**
- Function `loadRecentlyViewed()`
- Function `renderRecentlyViewedSection()`
- Function `clearRecentlyViewed()`
- Tất cả code liên quan đến section "Đã xem gần đây"

**Kết quả:** Trang chủ không còn hiển thị section "Đã xem gần đây"

### 3. Controller - Thêm Route Mới

**File:** `UTeaDrinkWebsite/src/main/java/net/codejava/utea/customer/controller/ProductCusController.java`

**Thêm method mới (dòng 285-330):**
```java
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
    Page<ViewedProduct> viewedPage = viewedProductService.getRecentlyViewed(userId, pageable);
    
    // Lấy products từ ViewedProduct
    List<Product> products = viewedPage.getContent().stream()
            .map(ViewedProduct::getProduct)
            .filter(p -> p != null && "AVAILABLE".equals(p.getStatus()))
            .toList();
    
    // Lấy rating và sold count
    Map<Long, Double> ratingMap = new HashMap<>();
    Map<Long, Integer> soldMap = new HashMap<>();
    
    for (Product p : products) {
        Double rating = reviewService.avgRating(p.getId());
        ratingMap.put(p.getId(), rating);
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
```

**Chức năng:**
- Check authentication (redirect /login nếu chưa đăng nhập)
- Phân trang 12 sản phẩm/trang
- Lấy rating và sold count cho từng sản phẩm
- Render view `customer/recently-viewed.html`

### 4. View Template - Trang Riêng

**File:** `UTeaDrinkWebsite/src/main/resources/templates/customer/recently-viewed.html` ✨ **MỚI**

**Cấu trúc:**
```html
<!DOCTYPE html>
<html>
<head>
    <!-- Bootstrap, FontAwesome, Icons -->
    <!-- CSRF meta tags -->
    <!-- Custom styles -->
</head>
<body>
    <!-- Customer Header (fragment) -->
    
    <!-- Page Header -->
    <div class="page-header">
        <h1>👁️ Đã xem gần đây</h1>
        <p>Bạn đã xem X sản phẩm</p>
        <button onclick="clearHistory()">Xóa lịch sử</button>
    </div>
    
    <!-- Products Grid (4 cột) -->
    <div class="product-grid">
        <!-- Product cards với hover effects -->
    </div>
    
    <!-- Empty State (nếu chưa xem sản phẩm nào) -->
    <div class="empty-state">
        <i class="bi bi-eye-slash"></i>
        <h2>Chưa có sản phẩm nào</h2>
        <a href="/customer/menu">Khám phá menu</a>
    </div>
    
    <!-- Pagination -->
    <div class="pag-wrap">
        <!-- Previous, page numbers, next -->
    </div>
    
    <!-- Footer & Chatbot -->
    
    <script>
        // clearHistory() function
    </script>
</body>
</html>
```

**Thiết kế UI:**

1. **Page Header:**
   - Background: Dark gradient (#0f172a → #1e293b)
   - Màu chữ: Trắng
   - Icon: 👁️ (eye)
   - Nút "Xóa lịch sử" màu đỏ (hover effect)

2. **Product Grid:**
   - 4 cột responsive (4 → 3 → 2 → 1 tùy màn hình)
   - Card design giống trang Menu
   - Hover: Transform up + shadow
   - Badge "Bán chạy" / "Yêu thích"
   - Overlay với nút "Xem chi tiết"

3. **Product Card Info:**
   - Tên sản phẩm (2 dòng tối đa)
   - Giá (màu đỏ accent)
   - Rating (sao vàng)
   - Số lượng đã bán

4. **Empty State:**
   - Icon eye-slash lớn
   - Tiêu đề "Chưa có sản phẩm nào"
   - Nút "Khám phá menu" màu cam

5. **Pagination:**
   - Nút Previous/Next
   - Page numbers
   - Active page highlight
   - Disabled state

**Responsive:**
- Desktop: 4 cột
- Tablet: 3 cột
- Mobile: 2 cột
- Small mobile: 1 cột

---

## 🎯 LUỒNG NGƯỜI DÙNG

### 1. Truy cập trang "Đã xem gần đây"

```
User click vào avatar → Dropdown menu hiện
     ↓
User click "Đã xem gần đây"
     ↓
Browser navigate to /customer/recently-viewed
     ↓
ProductCusController.recentlyViewed()
     ↓
Check authentication
     ↓
├─ CHƯA ĐĂNG NHẬP → redirect /login
└─ ĐÃ ĐĂNG NHẬP → Continue
     ↓
ViewedProductService.getRecentlyViewed(userId, page)
     ↓
Query database: ORDER BY lastSeenAt DESC, LIMIT 12
     ↓
Get rating & sold count for each product
     ↓
Render view: customer/recently-viewed.html
```

### 2. Hiển thị trang

```
Page load
     ↓
Show customer header (với active dropdown)
     ↓
Show page header (title + total count + clear button)
     ↓
├─ CÓ SẢN PHẨM → Show product grid + pagination
└─ KHÔNG CÓ → Show empty state với nút "Khám phá menu"
```

### 3. Xóa lịch sử

```
User click "Xóa lịch sử"
     ↓
JavaScript: clearHistory()
     ↓
Confirm dialog: "Bạn có chắc...?"
     ↓
├─ CANCEL → Do nothing
└─ OK → Continue
     ↓
DELETE /api/customer/recently-viewed/clear
     ↓
ViewedProductService.clearHistory(userId)
     ↓
Delete all records from database
     ↓
Response: {ok: true, message: "Đã xóa..."}
     ↓
alert("Đã xóa lịch sử xem")
     ↓
window.location.reload()
     ↓
Show empty state
```

---

## 🧪 HƯỚNG DẪN TEST

### Bước 1: Restart Application
```bash
cd UTeaDrinkWebsite
mvn clean compile
mvn spring-boot:run
```

### Bước 2: Đăng nhập
1. Vào `http://localhost:8080/login`
2. Đăng nhập với tài khoản customer

### Bước 3: Xem sản phẩm để tạo lịch sử
1. Vào Menu: `http://localhost:8080/customer/menu`
2. Click xem **ít nhất 5-10 sản phẩm khác nhau**
3. Xem chi tiết từng sản phẩm

### Bước 4: Kiểm tra link trong header
1. Ở bất kỳ trang nào, click vào **avatar** góc trên bên phải
2. ✅ Dropdown menu hiện ra
3. ✅ Thấy mục **"Đã xem gần đây"** với icon eye
4. Hover vào → ✅ Background highlight

### Bước 5: Truy cập trang "Đã xem gần đây"
1. Click vào "Đã xem gần đây"
2. ✅ Chuyển đến `/customer/recently-viewed`
3. ✅ Thấy header với title "Đã xem gần đây"
4. ✅ Thấy text "Bạn đã xem X sản phẩm"
5. ✅ Grid 4 cột hiển thị sản phẩm
6. ✅ Sắp xếp theo thời gian **mới nhất trước**

### Bước 6: Kiểm tra Product Cards
1. ✅ Hiển thị đầy đủ: Ảnh, tên, giá, rating, số lượng bán
2. ✅ Có badge "Bán chạy" / "Yêu thích" (nếu đủ điều kiện)
3. Hover vào card:
   - ✅ Card nổi lên (transform up)
   - ✅ Shadow đậm hơn
   - ✅ Overlay đen hiện ra
   - ✅ Nút "Xem chi tiết" xuất hiện
4. Click vào card → ✅ Chuyển đến trang chi tiết sản phẩm

### Bước 7: Test Pagination (nếu có > 12 sản phẩm)
1. ✅ Thấy pagination ở dưới grid
2. Click "Sau »" → ✅ Chuyển trang
3. ✅ URL update: `?page=1`
4. Click số trang → ✅ Jump to page
5. Click "« Trước" → ✅ Về trang trước

### Bước 8: Test Xóa lịch sử
1. Click nút **"Xóa lịch sử"** (màu đỏ, góc phải header)
2. ✅ Confirm dialog xuất hiện
3. Click "Cancel" → ✅ Không có gì xảy ra
4. Click "Xóa lịch sử" lại → Click "OK"
5. ✅ Alert "Đã xóa lịch sử xem"
6. ✅ Trang reload
7. ✅ Hiển thị **empty state** với icon eye-slash
8. ✅ Text: "Chưa có sản phẩm nào"
9. ✅ Nút "Khám phá menu"

### Bước 9: Test Empty State
1. Từ empty state, click **"Khám phá menu"**
2. ✅ Chuyển đến `/customer/menu`
3. Xem 3-5 sản phẩm mới
4. Quay lại "Đã xem gần đây"
5. ✅ Hiển thị products trở lại

### Bước 10: Test với user chưa đăng nhập
1. Logout
2. Truy cập trực tiếp: `http://localhost:8080/customer/recently-viewed`
3. ✅ **Redirect** về `/login`
4. ✅ Không thể truy cập khi chưa đăng nhập

### Bước 11: Test Responsive
1. Resize browser window:
   - Desktop (>1200px) → ✅ 4 cột
   - Laptop (992-1199px) → ✅ 3 cột
   - Tablet (768-991px) → ✅ 2 cột
   - Mobile (<768px) → ✅ 2 cột hoặc 1 cột

---

## 📊 SO SÁNH TRƯỚC/SAU

| Tiêu chí | Trước | Sau |
|----------|-------|-----|
| **Vị trí** | Section tự động ở trang chủ | Link trong header → Trang riêng |
| **Hiển thị** | Tự động (nếu có) | User chủ động click |
| **Số lượng** | 8 sản phẩm cố định | 12 sản phẩm/trang, có phân trang |
| **Header** | Không có | Có header đầy đủ |
| **Empty State** | Không hiển thị gì | Có empty state đẹp |
| **Xóa lịch sử** | Nút nhỏ trong section | Nút lớn ở header trang |
| **UX** | Passive (nhìn thấy tự động) | Active (user chủ động xem) |

---

## ✅ ƯU ĐIỂM THIẾT KẾ MỚI

1. **Không làm loãng trang chủ:** Trang chủ chỉ hiển thị featured products quan trọng
2. **Đầy đủ hơn:** Có header, pagination, empty state
3. **Phù hợp pattern:** Giống các trang khác (Orders, Vouchers, Account)
4. **Có thể mở rộng:** Dễ thêm filter, sort, search sau này
5. **Better UX:** User chủ động xem, không bị "force" thông tin

---

## 🎨 STYLE GUIDE

### Colors
- **Primary:** #e53935 (Red accent)
- **Header BG:** #0f172a → #1e293b (Dark gradient)
- **Text:** #111 (Black)
- **Muted text:** #64748b
- **Border:** rgba(0,0,0,0.04)
- **Shadow:** rgba(0,0,0,0.08)

### Typography
- **Font family:** 'Segoe UI', Tahoma, sans-serif
- **Page title:** 2rem, 800 weight
- **Product name:** 1.05rem, 700 weight
- **Price:** 1.2rem, 800 weight

### Spacing
- **Container max-width:** 1200px
- **Grid gap:** 20px
- **Card padding:** 1rem
- **Page header padding:** 2.5rem vertical

### Effects
- **Hover transform:** translateY(-8px)
- **Transition:** 0.3-0.4s ease
- **Border radius:** 12-16px
- **Shadow on hover:** 0 16px 32px rgba(0,0,0,.15)

---

## 🚀 HOÀN THÀNH

Chức năng "Đã xem gần đây" đã được **HOÀN THIỆN** theo yêu cầu mới:
- ✅ Link trong header customer
- ✅ Trang riêng với URL `/customer/recently-viewed`
- ✅ Header đầy đủ như các trang khác
- ✅ Grid 4 cột responsive
- ✅ Phân trang 12 sản phẩm/trang
- ✅ Empty state đẹp
- ✅ Xóa lịch sử hoạt động tốt
- ✅ Authentication check
- ✅ Responsive design

**Ready for production! 🎉**

