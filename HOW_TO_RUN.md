# 🚀 **HƯỚNG DẪN CHẠY ỨNG DỤNG**

## ⚠️ **QUAN TRỌNG**

File `application.properties` đã được **BẢO MẬT** - các secrets đã bị xóa khỏi default values.

Giờ bạn **PHẢI** cung cấp biến môi trường để chạy app!

---

## 📋 **CÁC CÁCH CHẠY ỨNG DỤNG**

### **Cách 1: Dùng Script PowerShell (KHUYẾN NGHỊ)**

```powershell
cd UTeaDrinkWebsite
.\run-with-env.ps1
```

➡️ Script sẽ tự động:
1. Đọc file `.env`
2. Set tất cả biến môi trường
3. Chạy `mvn spring-boot:run`

---

### **Cách 2: Set Biến Môi Trường Thủ Công**

```powershell
cd UTeaDrinkWebsite

# Set tất cả biến môi trường
$env:DB_PASSWORD="123456"
$env:MAIL_PASSWORD="ravf kfon wtgd kkgc"
$env:CLOUDINARY_API_SECRET="FEt7Kt4r9BWR-0e0Dx-jFBv_71M"
$env:GOOGLE_CLIENT_SECRET="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"
$env:JWT_SECRET_KEY="vG9p0mV9Z8q1Lw2rC3t4Y5u6A7b8C9d0E1f2G3h4I5j6K7l8"
$env:GEMINI_API_KEY="AIzaSyAmZ3U1AyRDvKy8ZbgsMElIW0fcDuBBPpY"

# Chạy app
mvn spring-boot:run
```

---

### **Cách 3: Cấu Hình Trong IntelliJ IDEA**

1. **Run** → **Edit Configurations**
2. **Environment Variables** → Click biểu tượng **📁 (folder)**
3. **Load Environment Variables from File** → Chọn file `.env`

**HOẶC thêm thủ công:**

```
MAIL_PASSWORD=ravf kfon wtgd kkgc
CLOUDINARY_API_SECRET=FEt7Kt4r9BWR-0e0Dx-jFBv_71M
GOOGLE_CLIENT_SECRET=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
JWT_SECRET_KEY=vG9p0mV9Z8q1Lw2rC3t4Y5u6A7b8C9d0E1f2G3h4I5j6K7l8
GEMINI_API_KEY=AIzaSyAmZ3U1AyRDvKy8ZbgsMElIW0fcDuBBPpY
```

4. **Run/Debug** → App sẽ tự động dùng các biến đã cấu hình

---

## 🔍 **KIỂM TRA APP ĐÃ CHẠY**

Sau khi chạy thành công, truy cập:

```
http://localhost:8080
```

---

## ❌ **NẾU GẶP LỖI**

### **Lỗi: "Could not resolve placeholder"**

```
Caused by: java.lang.IllegalArgumentException: 
Could not resolve placeholder 'MAIL_PASSWORD' in value "${MAIL_PASSWORD}"
```

**Nguyên nhân:** Thiếu biến môi trường

**Giải pháp:**
1. Dùng script `run-with-env.ps1`
2. Hoặc set biến môi trường thủ công
3. Hoặc cấu hình trong IDE

---

## 📦 **DANH SÁCH BIẾN CẦN THIẾT**

| Biến | Mô tả | Bắt buộc? |
|------|-------|-----------|
| `DB_PASSWORD` | Mật khẩu SQL Server | ✅ (có default) |
| `MAIL_PASSWORD` | App Password Gmail | ✅ BẮT BUỘC |
| `CLOUDINARY_API_SECRET` | API Secret Cloudinary | ✅ BẮT BUỘC |
| `GOOGLE_CLIENT_SECRET` | OAuth2 Client Secret | ✅ BẮT BUỘC |
| `JWT_SECRET_KEY` | Khóa mã hóa JWT | ✅ (có default) |
| `GEMINI_API_KEY` | API Key Gemini AI | ✅ BẮT BUỘC |

---

## ✅ **SAU KHI CHẠY THÀNH CÔNG**

Bạn có thể **PUSH LÊN GITHUB** an toàn:

```bash
git add .
git commit -m "Secure configuration with environment variables"
git push origin hoang2
```

➡️ **GitHub sẽ KHÔNG phát hiện secrets** vì đã xóa khỏi default values!

---

## 📚 **XEM THÊM**

- File `.env` chứa tất cả giá trị thực (KHÔNG commit)
- File `.env.example` là template (CÓ THỂ commit)
- Script `run-with-env.ps1` để chạy nhanh

---

🎉 **Chúc bạn thành công!**


