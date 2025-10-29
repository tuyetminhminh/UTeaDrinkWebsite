# 👁️ CHỨC NĂNG "ĐÃ XEM GẦN ĐÂY" - HOÀN THÀNH

## 📋 TỔNG QUAN

Chức năng **"Đã xem gần đây"** (Recently Viewed Products) cho phép khách hàng xem lại các sản phẩm mà họ đã xem trước đó, giúp tăng trải nghiệm người dùng và khả năng quay lại mua hàng.

## ✅ CÁC FILE ĐÃ TRIỂN KHAI

### 1. Backend - Entity & Repository

#### `ViewedProduct.java` (Đã có sẵn)
**Location:** `UTeaDrinkWebsite/src/main/java/net/codejava/utea/engagement/entity/ViewedProduct.java`

**Đặc điểm:**
- Lưu thông tin user đã xem product nào
- Có field `lastSeenAt` để track thời gian xem gần nhất
- Unique constraint: 1 user chỉ có 1 record cho 1 product
- Có method `touch()` để update thời gian xem

#### `ViewedProductRepository.java` ✨ **MỚI**
**Location:** `UTeaDrinkWebsite/src/main/java/net/codejava/utea/engagement/repository/ViewedProductRepository.java`

**Chức năng:**
```java
// Tìm ViewedProduct theo user và product
Optional<ViewedProduct> findByUserAndProduct(User user, Product product);

// Lấy danh sách đã xem, sắp xếp theo thời gian mới nhất
Page<ViewedProduct> findByUser_IdOrderByLastSeenAtDesc(Long userId, Pageable pageable);
List<ViewedProduct> findByUser_IdOrderByLastSeenAtDesc(Long userId);

// Đếm và xóa
long countByUser_Id(Long userId);
void deleteByUser_Id(Long userId);
```

### 2. Backend - Service Layer

#### `ViewedProductService.java` & `ViewedProductServiceImpl.java` ✨ **MỚI**
**Location:** 
- `UTeaDrinkWebsite/src/main/java/net/codejava/utea/engagement/service/ViewedProductService.java`
- `UTeaDrinkWebsite/src/main/java/net/codejava/utea/engagement/service/impl/ViewedProductServiceImpl.java`

**Chức năng chính:**

```java
// Track khi user xem sản phẩm
void trackView(User user, Product product);
void trackView(Long userId, Long productId);

// Lấy danh sách đã xem
Page<ViewedProduct> getRecentlyViewed(Long userId, Pageable pageable);
List<Product> getRecentlyViewedProducts(Long userId, int limit);

// Xóa lịch sử
void clearHistory(Long userId);
```

**Logic:**
- Nếu user đã xem product → Update `lastSeenAt`
- Nếu chưa xem → Tạo record mới
- Chỉ trả về sản phẩm `AVAILABLE`
- Error handling không ảnh hưởng flow chính

### 3. Backend - Controller

#### `ProductCusController.java` ✅ **CẬP NHẬT**
**Location:** `UTeaDrinkWebsite/src/main/java/net/codejava/utea/customer/controller/ProductCusController.java`

**Thay đổi:**
```java
@GetMapping("/product/{id}")
public String productDetail(@PathVariable Long id,
                            ...
                            @AuthenticationPrincipal CustomUserDetails userDetails, // ← THÊM
                            Model model) {
    Product product = ...;
    
    // ✅ Track viewing (chỉ khi đã đăng nhập)
    if (userDetails != null && userDetails.getUser() != null) {
        viewedProductService.trackView(userDetails.getUser().getId(), id);
    }
    
    // ... rest of code
}
```

**Kết quả:** Mỗi khi user xem chi tiết sản phẩm, hệ thống tự động track.

#### `ViewedProductController.java` ✨ **MỚI**
**Location:** `UTeaDrinkWebsite/src/main/java/net/codejava/utea/engagement/controller/ViewedProductController.java`

**REST API Endpoints:**

**1. Lấy danh sách sản phẩm đã xem:**
```
GET /api/customer/recently-viewed?limit=10
```

**Response:**
```json
{
  "ok": true,
  "products": [
    {
      "id": 1,
      "name": "Trà sữa trân châu",
      "basePrice": 35000,
      "ratingAvg": 4.5,
      "soldCount": 120,
      "imageUrl": "...",
      "category": {
        "id": 1,
        "name": "Trà sữa"
      }
    }
  ],
  "total": 8
}
```

**2. Xóa lịch sử xem:**
```
DELETE /api/customer/recently-viewed/clear
```

**Response:**
```json
{
  "ok": true,
  "message": "Đã xóa lịch sử xem"
}
```

### 4. Frontend - UI

#### `customer-home.html` ✅ **CẬP NHẬT**
**Location:** `UTeaDrinkWebsite/src/main/resources/templates/home/customer-home.html`

**Thay đổi:**

**1. Thêm CSRF meta tags (dòng 10-12):**
```html
<meta name="_csrf" th:content="${_csrf != null ? _csrf.token : ''}">
<meta name="_csrf_header" th:content="${_csrf != null ? _csrf.headerName : 'X-CSRF-TOKEN'}">
```

**2. Thêm JavaScript functions:**

```javascript
// Load Recently Viewed Products
async function loadRecentlyViewed() {
    const response = await fetch('/api/customer/recently-viewed?limit=8');
    const data = await response.json();
    
    if (data.ok && data.products.length > 0) {
        const recentlyViewedHtml = renderRecentlyViewedSection(data.products);
        container.insertAdjacentHTML('afterbegin', recentlyViewedHtml);
    }
}

// Render UI Section
function renderRecentlyViewedSection(products) {
    return `
        <section class="section-container mb-5" id="recentlyViewedSection">
            <div class="section-header">
                <h2>👁️ Đã xem gần đây</h2>
                <button onclick="clearRecentlyViewed()">
                    <i class="fas fa-trash-alt"></i> Xóa lịch sử
                </button>
            </div>
            <div class="products-grid">
                ${products.map(product => renderProductCard(product)).join('')}
            </div>
        </section>
    `;
}

// Xóa lịch sử
async function clearRecentlyViewed() {
    if (confirm('Bạn có chắc muốn xóa lịch sử xem?')) {
        await fetch('/api/customer/recently-viewed/clear', { method: 'DELETE' });
        document.getElementById('recentlyViewedSection').remove();
    }
}
```

**3. Tự động load khi trang load:**
- Section "Đã xem gần đây" hiển thị **ở đầu trang** (trên các section khác)
- Chỉ hiển thị khi **đã đăng nhập** và **có sản phẩm đã xem**
- Hiển thị tối đa **8 sản phẩm**

## 🎨 THIẾT KẾ UI

### Section Header
- **Icon:** 👁️ (eye icon)
- **Màu chủ đạo:** Blue (#2196f3)
- **Background:** Gradient từ #e3f2fd đến transparent
- **Nút xóa lịch sử:** Góc phải của header

### Product Cards
- Sử dụng **cùng design** với các section khác
- Hiển thị: Ảnh, tên, giá, rating, số lượng đã bán
- Hover effect: Transform up + shadow
- Click card → Xem chi tiết sản phẩm

## 📊 LUỒNG HOẠT ĐỘNG

### 1. Track Viewing
```
User truy cập /customer/product/{id}
     ↓
ProductCusController.productDetail()
     ↓
ViewedProductService.trackView(userId, productId)
     ↓
Kiểm tra: Đã xem chưa?
     ↓
├─ ĐÃ XEM → Update lastSeenAt
└─ CHƯA XEM → Tạo record mới
```

### 2. Display Recently Viewed
```
User vào trang chủ (/customer/home)
     ↓
JavaScript: loadRecentlyViewed()
     ↓
Gọi API: GET /api/customer/recently-viewed?limit=8
     ↓
ViewedProductController.getRecentlyViewed()
     ↓
ViewedProductService.getRecentlyViewedProducts()
     ↓
Query database: ORDER BY lastSeenAt DESC
     ↓
Filter: Chỉ lấy AVAILABLE products
     ↓
Render UI section ở đầu trang
```

### 3. Clear History
```
User click "Xóa lịch sử"
     ↓
JavaScript: clearRecentlyViewed()
     ↓
Confirm dialog
     ↓
Gọi API: DELETE /api/customer/recently-viewed/clear
     ↓
ViewedProductService.clearHistory(userId)
     ↓
Xóa tất cả records trong database
     ↓
Remove section khỏi UI
```

## 🧪 HƯỚNG DẪN TEST

### Bước 1: Restart Application
```bash
cd UTeaDrinkWebsite
mvn clean compile
mvn spring-boot:run
```

### Bước 2: Đăng nhập
1. Truy cập: `http://localhost:8080/login`
2. Đăng nhập với tài khoản customer

### Bước 3: Xem sản phẩm
1. Vào trang Menu: `http://localhost:8080/customer/menu`
2. Click vào **ít nhất 3-5 sản phẩm khác nhau**
3. Xem chi tiết từng sản phẩm

### Bước 4: Kiểm tra Recently Viewed
1. Quay lại trang chủ: `http://localhost:8080/customer/home`
2. ✅ **Phải thấy section "Đã xem gần đây"** ở đầu trang
3. ✅ Các sản phẩm vừa xem hiển thị theo thứ tự **mới nhất trước**
4. ✅ Click vào sản phẩm → Chuyển đến trang chi tiết

### Bước 5: Test Xem lại cùng sản phẩm
1. Xem lại một sản phẩm đã xem trước đó
2. Quay lại trang chủ
3. ✅ Sản phẩm đó phải **lên đầu danh sách** (vì lastSeenAt được update)

### Bước 6: Test Xóa lịch sử
1. Ở section "Đã xem gần đây", click nút **"Xóa lịch sử"**
2. Confirm dialog xuất hiện
3. Click "OK"
4. ✅ Section biến mất
5. Reload trang
6. ✅ Section **không xuất hiện** nữa

### Bước 7: Test với Guest User (chưa đăng nhập)
1. Logout
2. Vào trang chủ
3. ✅ Section "Đã xem gần đây" **không hiển thị**

### Bước 8: Kiểm tra Database (Optional)
```sql
-- Xem tất cả records
SELECT * FROM viewed_products ORDER BY last_seen_at DESC;

-- Xem của 1 user cụ thể
SELECT vp.*, p.name as product_name 
FROM viewed_products vp
JOIN products p ON vp.product_id = p.id
WHERE vp.user_id = 1
ORDER BY vp.last_seen_at DESC;
```

## 🎯 TÍNH NĂNG

### ✅ Đã Implement
- [x] Track khi xem sản phẩm (tự động)
- [x] Lưu thời gian xem gần nhất
- [x] Hiển thị danh sách đã xem (phân trang)
- [x] Sắp xếp theo thời gian mới nhất
- [x] Xóa lịch sử xem
- [x] REST API endpoints
- [x] UI section đẹp mắt
- [x] Responsive design
- [x] Error handling
- [x] CSRF protection

### 🎨 UI/UX Features
- Section hiển thị ở **đầu trang** (prominent)
- Icon eye 👁️ dễ nhận biết
- Grid layout 4 cột (responsive)
- Hover effects mượt mà
- Nút xóa lịch sử dễ thấy
- Confirm dialog trước khi xóa

### 🔒 Security
- Chỉ user đã đăng nhập mới track được
- API check authentication
- CSRF protection cho DELETE request
- User chỉ xem/xóa được lịch sử của mình

## 🚀 NÂNG CAP TRONG TƯƠNG LAI (Optional)

### 1. Giới hạn số lượng
```java
// Chỉ lưu tối đa 50 sản phẩm gần nhất
// Tự động xóa các record cũ nhất khi vượt quá
```

### 2. Thêm filter theo category
```javascript
// Hiển thị tab filter: "Tất cả", "Trà sữa", "Cà phê", v.v.
```

### 3. Recommendation dựa trên viewed history
```java
// "Bạn có thể thích" - recommend sản phẩm tương tự
```

### 4. Analytics
```java
// Track view count cho mỗi product
// Phân tích sản phẩm nào được xem nhiều nhất
```

### 5. Sync across devices
```java
// Nếu user đăng nhập trên nhiều thiết bị
// → Lịch sử xem đồng bộ
```

## 📝 NOTES

### Performance
- EntityGraph được sử dụng để **eager load** product + images → Giảm N+1 queries
- Chỉ load **AVAILABLE** products → Không hiển thị sản phẩm đã ẩn/hết hàng
- Limit mặc định 8-10 sản phẩm → Không quá tải UI

### Error Handling
- Track viewing **không throw exception** nếu có lỗi → Không ảnh hưởng UX
- API trả về error codes rõ ràng: `NOT_LOGGED_IN`, `ERROR`
- Frontend gracefully handle API errors

### Database
- Unique constraint: 1 user + 1 product = 1 record
- Index trên `user_id` và `(user_id, product_id)` → Query nhanh
- lastSeenAt auto update qua method `touch()`

## 🎉 KẾT LUẬN

Chức năng "Đã xem gần đây" đã được triển khai **HOÀN CHỈNH** với:
- ✅ Backend logic robust
- ✅ RESTful API endpoints
- ✅ UI đẹp và responsive
- ✅ Security được đảm bảo
- ✅ Error handling tốt
- ✅ Easy to test

**Ready for production! 🚀**

