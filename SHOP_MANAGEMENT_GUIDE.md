# 🛍️ HƯỚNG DẪN SỬ DỤNG: QUẢN LÝ CỬA HÀNG

## 📖 MỤC LỤC
1. [Đăng ký Shop](#1-đăng-ký-shop)
2. [Quản lý Thông tin Shop](#2-quản-lý-thông-tin-shop)
3. [Quản lý Banner](#3-quản-lý-banner)
4. [Quản lý Section](#4-quản-lý-section)

---

## 1. Đăng ký Shop

### Bước 1: Truy cập trang đăng ký
- URL: `http://localhost:8080/manager/shop/register`
- Hoặc từ `/manager/shop` → Click "Đăng ký cửa hàng ngay"

### Bước 2: Điền thông tin
- **Tên cửa hàng:** 3-200 ký tự (bắt buộc)
- **Địa chỉ:** 10-400 ký tự (bắt buộc)
- **Số điện thoại:** 10 chữ số, bắt đầu bằng 0 (bắt buộc)
- Tích vào "Đồng ý với điều khoản"

### Bước 3: Đăng ký
- Click "Đăng ký ngay"
- Chờ xác nhận
- Tự động chuyển về trang quản lý shop

### ⚠️ Lưu ý
- Mỗi Manager chỉ đăng ký được **1 shop duy nhất**
- Nếu đã có shop, sẽ tự động chuyển về trang quản lý

---

## 2. Quản lý Thông tin Shop

### Truy cập
- URL: `http://localhost:8080/manager/shop`

### Các thao tác
1. **Cập nhật thông tin:**
   - Sửa tên, địa chỉ, số điện thoại
   - Click "Lưu thay đổi"

2. **Thay đổi trạng thái:**
   - 🟢 **OPEN** - Đang mở cửa
   - 🔴 **CLOSED** - Đã đóng cửa
   - 🟡 **MAINTENANCE** - Đang bảo trì

3. **Quick Actions:**
   - "Quản lý Banner" → Đi đến trang banner
   - "Quản lý Section" → Đi đến trang section

---

## 3. Quản lý Banner

### Truy cập
- URL: `http://localhost:8080/manager/shop/banners`
- Hoặc từ Shop Management → "Quản lý Banner"

### Thêm Banner mới

1. Click **"Thêm Banner"**
2. Điền thông tin:
   - **Tiêu đề:** Tên banner (bắt buộc)
   - **URL Hình ảnh:** Link ảnh banner (bắt buộc)
   - **Link chuyển hướng:** URL khi click (không bắt buộc)
   - **Thứ tự:** Số thứ tự hiển thị (mặc định: 0)
   - **Trạng thái:** Hiển thị / Ẩn
3. Xem preview ảnh
4. Click **"Lưu Banner"**

### Sửa Banner
1. Click nút **"Sửa"** trên banner card
2. Chỉnh sửa thông tin
3. Click **"Lưu Banner"**

### Xóa Banner
1. Click nút **"Xóa"** trên banner card
2. Xác nhận xóa

### 💡 Tips
- Banner có số thứ tự nhỏ sẽ hiển thị trước
- Dùng ảnh có kích thước phù hợp (khuyến nghị: 1200x400px)
- Link ảnh phải public và accessible

---

## 4. Quản lý Section

### Truy cập
- URL: `http://localhost:8080/manager/shop/sections`
- Hoặc từ Shop Management → "Quản lý Section"

### Thêm Section mới

1. Click **"Thêm Section"**
2. Điền thông tin:
   - **Tiêu đề:** Tên section (bắt buộc)
   - **Loại Section:** Chọn từ dropdown (bắt buộc)
     - 🌟 FEATURED - Sản phẩm nổi bật
     - 📈 TOP_SELLING - Bán chạy nhất
     - 🆕 NEW_ARRIVALS - Sản phẩm mới
     - 💰 BEST_DEALS - Ưu đãi tốt nhất
     - 🎄 SEASONAL - Theo mùa
     - ✨ CUSTOM - Tùy chỉnh
   - **Nội dung JSON:** Định nghĩa sản phẩm (không bắt buộc)
   - **Thứ tự:** Số thứ tự hiển thị (mặc định: 0)
   - **Trạng thái:** Hiển thị / Ẩn
3. Click **"Lưu Section"**

### Content JSON Examples

**Hiển thị sản phẩm theo IDs:**
```json
{
  "productIds": [1, 2, 3, 4, 5],
  "limit": 10
}
```

**Hiển thị theo danh mục:**
```json
{
  "categoryId": 3,
  "limit": 8
}
```

**Tùy chỉnh nâng cao:**
```json
{
  "productIds": [10, 11, 12],
  "categoryId": 5,
  "limit": 12,
  "sortBy": "popularity"
}
```

### Sửa Section
1. Click nút **"Sửa"** trên dòng section
2. Chỉnh sửa thông tin
3. Click **"Lưu Section"**

### Xóa Section
1. Click nút **"Xóa"** trên dòng section
2. Xác nhận xóa

### ⚠️ Lưu ý JSON
- JSON phải đúng format (validate tự động)
- Nếu để trống, section sẽ không có nội dung cụ thể

---

## 🚀 DEMO WORKFLOW

### Workflow đăng ký và quản lý đầy đủ:

```
1. Đăng nhập với role MANAGER
   ↓
2. Truy cập /manager/shop
   → Nếu chưa có shop → Đăng ký shop
   → Nếu đã có shop → Quản lý shop
   ↓
3. Cập nhật thông tin shop
   - Đổi tên, địa chỉ, SĐT
   - Thay đổi trạng thái
   ↓
4. Tạo Banner
   - Thêm 3-5 banner quảng cáo
   - Sắp xếp thứ tự
   - Set active/inactive
   ↓
5. Tạo Section
   - Featured Products
   - Top Selling
   - New Arrivals
   - Định nghĩa content JSON
   ↓
6. Hoàn tất! Shop đã sẵn sàng
```

---

## 🎨 UI COLOR SCHEME

| Màu | Hex Code | Mục đích |
|-----|----------|----------|
| Purple Gradient | `#667eea → #764ba2` | Primary buttons, headers |
| Light Gray | `#f5f7fa → #e8eef5` | Background |
| Green Pastel | `#d4f4dd → #c3f0d0` | Success states |
| Yellow Pastel | `#fef3c7 → #fde68a` | Warning states |
| Red Pastel | `#fecaca → #fca5a5` | Danger states |
| Blue Pastel | `#dbeafe → #bfdbfe` | Info states |

---

## 🐛 TROUBLESHOOTING

### Lỗi "Manager đã đăng ký shop rồi!"
**Nguyên nhân:** Mỗi manager chỉ đăng ký được 1 shop  
**Giải pháp:** Truy cập `/manager/shop` để quản lý shop hiện tại

### Banner không hiển thị ảnh
**Nguyên nhân:** URL ảnh không valid hoặc bị block CORS  
**Giải pháp:**
- Kiểm tra URL ảnh có truy cập được không
- Dùng ảnh public (Imgur, Cloudinary, CDN)
- Kiểm tra CORS settings

### JSON validation error
**Nguyên nhân:** Content JSON không đúng format  
**Giải pháp:**
- Dùng JSON validator online
- Kiểm tra dấu ngoặc, dấu phẩy
- Ví dụ đúng: `{"productIds": [1, 2, 3]}`

### Validation form fails
**Nguyên nhân:** Không đáp ứng yêu cầu (độ dài, format)  
**Giải pháp:**
- Tên shop: 3-200 ký tự
- Địa chỉ: 10-400 ký tự
- SĐT: đúng 10 số, bắt đầu bằng 0

---

## 📞 SUPPORT

Nếu gặp vấn đề, kiểm tra:
1. Console log (F12) xem có lỗi không
2. Network tab xem API response
3. Backend logs xem exception

---

## ✅ CHECKLIST HOÀN TẤT SETUP

- [ ] Đã đăng ký shop thành công
- [ ] Đã cập nhật đầy đủ thông tin shop
- [ ] Đã thêm ít nhất 1 banner
- [ ] Đã tạo ít nhất 1 section
- [ ] Đã test tất cả chức năng CRUD
- [ ] Shop status là OPEN

---

*Hướng dẫn này dành cho Manager sử dụng hệ thống UTea.*  
*Cập nhật lần cuối: 2025*

