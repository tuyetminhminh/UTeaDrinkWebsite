# 📊 Tóm Tắt Khởi Tạo Dữ Liệu Mẫu

## 🎯 Tổng Quan
File `DataInitializer.java` đã được cập nhật hoàn toàn để tạo đầy đủ cơ sở dữ liệu mẫu cho hệ thống UTea Drink Website với dữ liệu đa dạng và phân loại rõ ràng.

---

## 📂 Danh Mục Sản Phẩm (5 Danh Mục)

### 1. **Trà Sữa** - 11 sản phẩm
- Trà sữa truyền thống
- Trà matcha sữa
- Trà sữa khoai môn
- Trà sữa Olong mochi
- Chocolate latte
- Trà sữa dâu
- Trà sữa trân châu hoàng kim
- Trà sữa okinawa kim cương đen
- Matcha latte
- Trà sữa bạc hà

### 2. **Cà Phê** - 5 sản phẩm
- Cà phê sữa đá
- Cà phê đen đá
- Bạc xỉu
- Cà phê trứng
- Cappuccino

### 3. **Bánh** - 11 sản phẩm
- Bánh cầu vồng
- Bánh sandwich trứng chảy
- Bánh sừng bò
- Bánh mochi nướng
- Bánh donut
- Bánh sừng bò socola
- Bánh kem oreo
- Bánh pudding Caramel
- Bánh su kem
- Bánh tiramisu
- Bánh bông lan trứng muối

### 4. **Sinh Tố** - 5 sản phẩm
- Sinh tố bơ
- Sinh tố dâu
- Sinh tố xoài
- Sinh tố dưa hấu
- Sinh tố sapoche

### 5. **Trà Trái Cây** - 10 sản phẩm
- Trà chanh nhiệt đới
- Trà cherry nhiệt đới
- Trà tắc
- Hồng trà UTea
- Trà đào cam sả
- Trà xanh
- Trà ô long
- Trà vải
- Trà dâu tằm
- Trà nhiệt đới

**🎉 Tổng cộng: 42 sản phẩm**

---

## 🛒 Dữ Liệu Orders

### Phân Loại Orders Theo Trạng Thái:
- **NEW**: 2 đơn (mới tạo, chưa xác nhận)
- **PREPARING**: 3 đơn (đã xác nhận, chờ shipper)
- **DELIVERING**: 1 đơn (đang giao)
- **DELIVERED**: 15 đơn (đã giao thành công)

**📦 Tổng cộng: 21 orders**

### Đặc Điểm:
- Mỗi order có số lượng sản phẩm khác nhau (1-3 items)
- Giá trị đơn hàng đa dạng dựa trên base price của sản phẩm
- Thời gian tạo đơn phân bổ từ 30 phút đến 15 ngày trước

---

## ⭐ Dữ Liệu Reviews

### Đặc Điểm:
- **Số lượng reviews**: Khoảng 30-35 reviews cho 15 sản phẩm đầu tiên
- **Rating đa dạng**: Từ 3⭐ đến 5⭐
- **3 khách hàng**: customer@utea.local, customer2@utea.local, thanhnau25@gmail.com
- **Trạng thái**: Tất cả đã APPROVED

### Phân Bổ Reviews:
- Customer 1: Review cho tất cả 15 sản phẩm
- Customer 2: Review cho ~8 sản phẩm (mỗi 2 sản phẩm)
- Customer 3: Review cho ~5 sản phẩm (mỗi 3 sản phẩm)

### Nội Dung Reviews:
- 5⭐: "Sản phẩm tuyệt vời! Rất hài lòng, sẽ mua lại nhiều lần nữa!"
- 4⭐: "Sản phẩm tốt, chất lượng ổn, giao hàng nhanh"
- 3⭐: "Sản phẩm bình thường, có thể cải thiện thêm"

---

## 📊 Shop Sections (5 Sections)

### 1. Sản Phẩm Nổi Bật (FEATURED)
- Hiển thị 8 sản phẩm nổi bật
- Sort order: 0

### 2. Bán Chạy Nhất (TOP_SELLING)
- Sắp xếp theo `sold_count` giảm dần
- Hiển thị 8 sản phẩm
- Sort order: 1

### 3. Sản Phẩm Mới (NEW_ARRIVALS)
- Sắp xếp theo `created_at` giảm dần
- Hiển thị 8 sản phẩm
- Sort order: 2

### 4. Đánh Giá Cao (TOP_RATED)
- Sắp xếp theo `rating_avg` giảm dần
- Hiển thị 8 sản phẩm
- Sort order: 3

### 5. Khuyến Mãi Hôm Nay (PROMOTION)
- Hiển thị sản phẩm đang có khuyến mãi
- Hiển thị 8 sản phẩm
- Sort order: 4

---

## 🚚 Shipping & Assignments

### Shipping Providers (3):
1. Giao hàng nhanh - 15,000đ
2. Giao hàng tiết kiệm - 12,000đ
3. J&T Express - 18,000đ

### Shippers (5):
1. shipper@utea.local - Phạm Văn Thành (bike)
2. shipper2@utea.local - Lê Hoàng Nam (bike)
3. shipper3@utea.local - Trần Minh Tuấn (car)
4. shipper4@utea.local - Nguyễn Thị Hoa (bike)
5. shipper5@utea.local - Võ Đức Anh (bike)

### Ship Assignments:
- Tự động gán shipper cho tất cả đơn DELIVERING và DELIVERED
- Phân bổ đều giữa các shipper

---

## 🎁 Promotions & Vouchers

### Promotions (2):
1. **Khuyến mãi toàn hệ thống** (GLOBAL)
   - Giảm 15%
   - Áp dụng cho tất cả sản phẩm

2. **Khuyến mãi Shop** (SHOP)
   - Giảm 10%
   - Áp dụng cho shop UTea Coffee & Tea

### Vouchers (2):
1. **GLOBAL50**
   - Giảm 50%
   - Đơn tối thiểu: 200,000đ
   - Phạm vi: Toàn hệ thống

2. **SHOP30**
   - Giảm 30%
   - Đơn tối thiểu: 150,000đ
   - Phạm vi: Shop cụ thể

### Coupons (3):
1. **WELCOME10** - Giảm 10% cho đơn đầu tiên
2. **SUMMER20** - Giảm 20k cho đơn từ 100k
3. **FREESHIP** - Miễn phí vận chuyển

---

## 🔄 Tính Năng Tự Động Đồng Bộ

### ProductSyncService
Sau khi khởi tạo dữ liệu, hệ thống tự động:

1. **Tính `sold_count`**:
   - Đếm tổng số lượng sản phẩm đã bán từ các orders DELIVERED
   - Cập nhật vào bảng `products`

2. **Tính `rating_avg`**:
   - Tính trung bình rating từ tất cả reviews APPROVED
   - Làm tròn đến 2 chữ số thập phân
   - Cập nhật vào bảng `products`

### Kết Quả:
- ✅ Không còn giá trị mặc định (rating_avg = 5, sold_count = 0)
- ✅ Mỗi sản phẩm có giá trị thực tế dựa trên orders và reviews
- ✅ Dữ liệu đa dạng và chính xác

---

## 🎨 Variants & Images

### Product Variants:
Mỗi sản phẩm có **3 variants**:
- **Size S**: Base price, 350ml
- **Size M**: Base price + 5,000đ, 500ml
- **Size L**: Base price + 10,000đ, 700ml

### Product Images:
- Mỗi sản phẩm có **1 ảnh chính** từ Cloudinary
- Lưu cả `url` và `publicId` để quản lý
- Tất cả links hình ảnh đều hoạt động

---

## 💬 Conversations & Messages

### 2 Cuộc Trò Chuyện Mẫu:
1. **Manager ↔ Customer 1**:
   - 5 tin nhắn về khuyến mãi và đặt hàng

2. **Manager ↔ Customer 2**:
   - 4 tin nhắn về sản phẩm và order

---

## 👥 Users & Roles

### Roles (5):
- ADMIN
- MANAGER
- SELLER
- CUSTOMER
- SHIPPER

### Users Mẫu:
1. **admin@utea.local** / 123456 - ADMIN
2. **manager@utea.local** / 123456 - MANAGER
3. **seller@utea.local** / 123456 - SELLER
4. **customer@utea.local** / 123456 - CUSTOMER
5. **customer2@utea.local** / 123456 - CUSTOMER
6. **thanhnau25@gmail.com** / 123456 - CUSTOMER
7. **shipper@utea.local** / 123456 - SHIPPER (x5)

---

## 🏪 Shop

**UTea Coffee & Tea**
- Địa chỉ: 01 Võ Văn Ngân, Thủ Đức, TP.HCM
- Điện thoại: 0901234567
- Trạng thái: OPEN

### Shop Banners (3):
1. Trà Sữa Đặc Biệt Mùa Hè
2. Combo Sinh Nhật Ưu Đãi 20%
3. Cà Phê Sáng - Thức Dậy Năng Lượng

---

## 🍰 Toppings (7)

1. Trân châu đen - 5,000đ
2. Trân châu trắng - 5,000đ
3. Thạch dừa - 5,000đ
4. Thạch rau câu - 5,000đ
5. Pudding - 8,000đ
6. Trứng cút - 7,000đ
7. Kem cheese - 10,000đ

---

## 📍 Addresses

### Customer Addresses:
- **customer@utea.local**: 123 Đường ABC, Phường Linh Trung, Thủ Đức
- **customer2@utea.local**: 456 Đường XYZ, Phường Linh Chiểu, Thủ Đức

---

## 🚀 Cách Sử Dụng

### Khởi Động Lại Ứng Dụng:
```bash
# Xóa database cũ (nếu cần reset)
DROP DATABASE UTEDrink2;
CREATE DATABASE UTEDrink2;

# Chạy ứng dụng
mvn spring-boot:run
```

### Dữ Liệu Sẽ Tự Động:
1. ✅ Tạo tất cả tables (JPA auto-create)
2. ✅ Insert dữ liệu mẫu (DataInitializer)
3. ✅ Tính toán `rating_avg` và `sold_count` (ProductSyncService)

### Kiểm Tra Kết Quả:
```sql
-- Xem sản phẩm với rating và sold_count
SELECT 
    p.id, 
    p.name, 
    p.sold_count, 
    p.rating_avg,
    c.name as category
FROM products p
LEFT JOIN product_categories c ON p.category_id = c.id
ORDER BY p.sold_count DESC;

-- Xem reviews
SELECT 
    r.rating,
    r.content,
    p.name as product_name,
    u.full_name as customer_name
FROM reviews r
LEFT JOIN products p ON r.product_id = p.id
LEFT JOIN users u ON r.user_id = u.id
WHERE r.status = 'APPROVED';

-- Xem orders
SELECT 
    o.order_code,
    o.status,
    o.total,
    u.full_name as customer_name,
    p.name as product_name
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
LEFT JOIN order_items oi ON oi.order_id = o.id
LEFT JOIN products p ON oi.product_id = p.id
ORDER BY o.created_at DESC;
```

---

## ⚡ Cải Tiến So Với Trước

### ❌ Trước:
- Danh mục "Nước ép" không phù hợp
- Sản phẩm Bánh nằm trong danh mục Cà phê
- `rating_avg` và `sold_count` có giá trị mặc định (4.5 và 0)
- Thiếu sections cho trang chủ
- Ít reviews và orders

### ✅ Sau:
- 5 danh mục rõ ràng: Trà sữa, Cà phê, Bánh, Sinh tố, Trà trái cây
- 42 sản phẩm phân loại đúng danh mục
- `rating_avg` và `sold_count` được tính tự động từ dữ liệu thực
- 5 sections đầy đủ cho trang chủ
- 21 orders và 30+ reviews với dữ liệu đa dạng
- Tự động sync stats sau khi khởi tạo

---

## 📝 Ghi Chú

1. **Idempotent**: DataInitializer có cơ chế kiểm tra để không tạo duplicate data
2. **Cloudinary**: Tất cả hình ảnh đều lưu trên Cloudinary với public_id
3. **UTF-8**: Tất cả dữ liệu tiếng Việt đều hiển thị chính xác
4. **Sync Service**: ProductSyncService có thể gọi thủ công qua endpoint `/api/admin/sync/product-stats`

---

## 🎯 Kết Luận

DataInitializer.java hiện đã hoàn thiện với:
- ✅ 42 sản phẩm đa dạng
- ✅ 5 danh mục rõ ràng
- ✅ 21 orders với nhiều trạng thái
- ✅ 30+ reviews đa dạng
- ✅ 5 sections cho trang chủ
- ✅ Tự động tính toán rating_avg và sold_count
- ✅ Dữ liệu mẫu đầy đủ cho demo và testing

**🎉 Hệ thống UTea Drink Website đã sẵn sàng với cơ sở dữ liệu mẫu hoàn chỉnh!**

