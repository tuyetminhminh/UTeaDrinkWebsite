# 🛒 BÁO CÁO SỬA LỖI GIỎ HÀNG

## ❌ VẤN ĐỀ TRƯỚC KHI SỬA

### 1. Lỗi Logic Tìm Sản Phẩm Trùng Lặp
**Triệu chứng:** Khi thêm sản phẩm Cafe mới vào giỏ hàng, hệ thống lại tăng số lượng sản phẩm Cafe cũ đã có trong giỏ.

**Nguyên nhân:** Logic tìm kiếm sản phẩm đã tồn tại trong giỏ hàng **KHÔNG KIỂM TRA `productId`**, chỉ kiểm tra `variant`:

```java
// Code SAI (trước khi sửa)
existing = itemRepo.findByCartIdAndVariant(cart.getId(), null)
// ❌ Chỉ check cartId + variant=null
// ❌ KHÔNG check productId
// ➡️ Kết quả: Tìm thấy BẤT KỲ sản phẩm nào có variant=null trong giỏ
```

**Hậu quả:**
- Thêm Cafe A vào giỏ
- Thêm Cafe B vào giỏ
- ❌ Hệ thống tăng số lượng Cafe A thay vì thêm Cafe B mới

### 2. Lỗi Logic Category
**Triệu chứng:** 
- Sản phẩm **Bánh** lại hiển thị size và topping (SAI!)
- Sản phẩm **Cafe** lại KHÔNG có size và topping (SAI!)

**Nguyên nhân:** Code check sai ID category:

Theo dữ liệu khởi tạo (`DataInitializer.java`):
- ID 1: Trà sữa
- ID 2: **Cà phê** ← Đang bị force không có size/topping (SAI!)
- ID 3: **Bánh** ← Đây mới phải là danh mục không có size/topping
- ID 4: Sinh tố
- ID 5: Trà trái cây

```java
// Code SAI (trước khi sửa)
if (p.getCategory() != null && p.getCategory().getId() == 2L) {
    variantId = null;    // ❌ Force Cafe không có size
    toppingIds = null;   // ❌ Force Cafe không có topping
}
```

---

## ✅ GIẢI PHÁP ĐÃ ÁP DỤNG

### 1. Sửa Logic Tìm Sản Phẩm Trùng Lặp

#### a) Thêm phương thức mới vào `CartItemRepository.java`

**LƯU Ý:** Tên phương thức phải match format với các phương thức đã có (dùng `ProductId` không underscore)

```java
// ✅ Tìm sản phẩm không có variant và không có topping
@EntityGraph(attributePaths = {"product", "product.images", "variant"})
Optional<CartItem> findByCartIdAndProductIdAndVariant_IdIsNullAndToppingsJsonIsNull(
    Long cartId, 
    Long productId
);

// ✅ Tìm sản phẩm có variant nhưng không có topping  
@EntityGraph(attributePaths = {"product", "product.images", "variant"})
Optional<CartItem> findByCartIdAndProductIdAndVariant_IdAndToppingsJsonIsNull(
    Long cartId, 
    Long productId, 
    Long variantId
);
```

#### b) Sửa logic trong `CartServiceImpl.java`

**File:** `UTeaDrinkWebsite/src/main/java/net/codejava/utea/customer/service/impl/CartServiceImpl.java`

**Phương thức `addItem()` (dòng 82-92):**
```java
// ✅ Code ĐÚNG (sau khi sửa)
if (variant == null) {
    existing = (topsJson == null)
        ? itemRepo.findByCartIdAndProductIdAndVariant_IdIsNullAndToppingsJsonIsNull(
              cart.getId(), productId)  // ✅ Check cả productId
        : itemRepo.findByCartIdAndProductIdAndVariant_IdIsNullAndToppingsJson(
              cart.getId(), productId, topsJson);
} else {
    existing = (topsJson == null)
        ? itemRepo.findByCartIdAndProductIdAndVariant_IdAndToppingsJsonIsNull(
              cart.getId(), productId, variant.getId())  // ✅ Check cả productId
        : itemRepo.findByCartIdAndProductIdAndVariant_IdAndToppingsJson(
              cart.getId(), productId, variant.getId(), topsJson);
}
```

**Phương thức `updateToppings()` (dòng 224-234):** Áp dụng logic tương tự.

### 2. Sửa Logic Category (từ 2L → 3L)

#### a) `CartController.java` (dòng 109-112)
```java
// ✅ ĐÚNG: Bánh (id=3) không có size và topping
if (p.getCategory() != null && p.getCategory().getId() == 3L) {
    variantId = null;
    toppingIds = null;
}
```

#### b) `CartApiController.java` (dòng 59)
```java
// ✅ ĐÚNG: Bánh (id=3) không có size và topping
boolean forceNoVariant = p.getCategory() != null && 
                        Objects.equals(p.getCategory().getId(), 3L);
```

#### c) `CartServiceImpl.java` (dòng 213-215)
```java
// ✅ ĐÚNG: Bánh (id=3) không có size và topping
if (product.getCategory() != null && product.getCategory().getId() == 3L) {
    toppingIds = null;
}
```

#### d) `cart.html` (2 chỗ)

**Dòng 80:** Hiển thị dropdown đổi size
```html
<!-- ✅ Ẩn dropdown size cho Bánh (id=3) -->
<form th:if="${... and item.product.category.id != 3}" ...>
```

**Dòng 105:** Hiển thị nút "Chỉnh topping"
```html
<!-- ✅ Ẩn nút topping cho Bánh (id=3) -->
<button th:if="${... and item.product.category.id != 3}" ...>
    Chỉnh topping
</button>
```

---

## 📋 DANH SÁCH FILE ĐÃ SỬA

### Backend (Java)
1. ✅ `UTeaDrinkWebsite/src/main/java/net/codejava/utea/customer/repository/CartItemRepository.java`
   - Thêm 2 phương thức mới

2. ✅ `UTeaDrinkWebsite/src/main/java/net/codejava/utea/customer/service/impl/CartServiceImpl.java`
   - Sửa `addItem()` (dòng 82-92)
   - Sửa `updateToppings()` (dòng 224-234)
   - Xóa import không dùng

3. ✅ `UTeaDrinkWebsite/src/main/java/net/codejava/utea/customer/controller/CartController.java`
   - Sửa check category từ `2L` → `3L` (dòng 110)

4. ✅ `UTeaDrinkWebsite/src/main/java/net/codejava/utea/customer/controller/CartApiController.java`
   - Sửa check category từ `2L` → `3L` (dòng 59)

### Frontend (HTML)
5. ✅ `UTeaDrinkWebsite/src/main/resources/templates/customer/cart.html`
   - Sửa check category từ `!= 2` → `!= 3` (2 chỗ: dòng 80 và 105)

---

## 🎯 KẾT QUẢ SAU KHI SỬA

### ✅ Logic Giỏ Hàng Hoạt Động Đúng

Khi thêm sản phẩm vào giỏ hàng, hệ thống sẽ:

1. ✅ **Kiểm tra đúng sản phẩm** (productId)
2. ✅ **Kiểm tra đúng size/variant** (variantId)  
3. ✅ **Kiểm tra đúng topping** (toppingsJson)
4. ✅ **Chỉ tăng số lượng** khi CẢ 3 điều kiện trên giống nhau
5. ✅ **Tạo dòng mới** nếu có bất kỳ điều kiện nào khác nhau

**Ví dụ:**
- Thêm Cafe A (size M, topping trân châu) → Tạo dòng mới
- Thêm Cafe A (size M, topping trân châu) → Tăng số lượng dòng trên
- Thêm Cafe A (size L, topping trân châu) → Tạo dòng mới (vì khác size)
- Thêm Cafe B (size M, topping trân châu) → Tạo dòng mới (vì khác sản phẩm)

### ✅ Logic Category Đúng

| Danh mục | Size/Variant | Topping |
|----------|--------------|---------|
| Trà sữa (id=1) | ✅ CÓ | ✅ CÓ |
| **Cà phê (id=2)** | ✅ CÓ | ✅ CÓ |
| **Bánh (id=3)** | ❌ KHÔNG | ❌ KHÔNG |
| Sinh tố (id=4) | ✅ CÓ | ✅ CÓ |
| Trà trái cây (id=5) | ✅ CÓ | ✅ CÓ |

---

## ⚠️ LƯU Ý QUAN TRỌNG

### 🔄 PHẢI RESTART ỨNG DỤNG

**Các thay đổi backend (Java) chỉ có hiệu lực sau khi restart!**

```bash
# Dừng ứng dụng nếu đang chạy
# Sau đó chạy lại:
cd UTeaDrinkWebsite
mvn spring-boot:run
```

### 🧪 KIỂM TRA SAU KHI SỬA

1. **Kiểm tra Cafe:**
   - Truy cập trang sản phẩm Cafe
   - ✅ Phải hiển thị chọn size (S/M/L)
   - ✅ Phải hiển thị chọn topping
   - Thêm Cafe A (size M) vào giỏ
   - Thêm Cafe B (size M) vào giỏ
   - ✅ Giỏ hàng phải có 2 dòng riêng biệt

2. **Kiểm tra Bánh:**
   - Truy cập trang sản phẩm Bánh
   - ✅ KHÔNG hiển thị chọn size
   - ✅ KHÔNG hiển thị chọn topping
   - Thêm Bánh A vào giỏ
   - Thêm Bánh B vào giỏ
   - ✅ Giỏ hàng phải có 2 dòng riêng biệt

3. **Kiểm tra trong giỏ hàng:**
   - Item Cafe: ✅ Có dropdown đổi size + nút "Chỉnh topping"
   - Item Bánh: ✅ KHÔNG có dropdown size + KHÔNG có nút topping

---

## 📝 GHI CHÚ KỸ THUẬT

### Tại Sao Cần Check ProductId?

Trong database, một sản phẩm có thể có:
- **productId**: ID sản phẩm (Cafe A, Cafe B, ...)
- **variantId**: ID biến thể/size (S, M, L)
- **toppingsJson**: JSON topping đã chọn

**Logic cũ chỉ check variant** → Tìm nhầm sản phẩm khác có cùng variant
**Logic mới check cả productId** → Tìm đúng sản phẩm + variant + topping

### Tại Sao Check Theo Tên Category An Toàn Hơn?

Trong `ProductCusController.java`, biến `isBakery` được check theo **TÊN**:
```java
boolean isBakery = product.getCategory() != null && 
                  "Bánh".equalsIgnoreCase(product.getCategory().getName());
```

✅ An toàn hơn vì không phụ thuộc vào ID (ID có thể thay đổi nếu reset database)
✅ Rõ ràng hơn về mục đích

**Khuyến nghị:** Nên refactor các chỗ check `category.getId() == 3L` thành check theo tên.

---

## ✨ TỔNG KẾT

| Trước khi sửa | Sau khi sửa |
|---------------|-------------|
| ❌ Thêm Cafe B tăng số lượng Cafe A | ✅ Thêm Cafe B tạo dòng mới |
| ❌ Bánh có size và topping | ✅ Bánh không có size và topping |
| ❌ Cafe không có size và topping | ✅ Cafe có size và topping |

**Kết luận:** Tất cả các vấn đề về giỏ hàng đã được khắc phục hoàn toàn! 🎉

