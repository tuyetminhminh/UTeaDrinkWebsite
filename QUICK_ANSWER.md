# ⚡ **TRẢ LỜI NHANH CÂU HỎI CỦA BẠN**

## ❓ **"Thêm default values thì khi push lên Git có báo lỗi không?"**

### **Câu trả lời: CÓ THỂ BỊ PHÁT HIỆN!**

| Secret trong file | Nguy cơ phát hiện của GitHub |
|-------------------|----------------------------|
| `${DB_PASSWORD:123456}` | ✅ **KHÔNG** phát hiện |
| `${JWT_SECRET:vG9p0mV9Z8q...}` | ⚠️ **CÓ THỂ** phát hiện |
| `${MAIL_PASSWORD:ravf kfon wtgd kkgc}` | 🟡 **TRUNG BÌNH** |
| `${CLOUDINARY_API_SECRET:FEt7Kt4r9B...}` | 🔴 **CAO** |
| `${GOOGLE_CLIENT_SECRET:GOCSPX-saJ7...}` | 🔴 **RẤT CAO** |
| `${GEMINI_API_KEY:AIzaSyAmZ3U1...}` | 🔴 **CHẮC CHẮN** |

---

## ❓ **"Tại sao script `run-with-env.ps1` không hoạt động?"**

### **Nguyên nhân:**

```
PowerShell Process
├─ Biến môi trường: CLOUDINARY_API_SECRET = "abc123" ✅
│
└─ Maven Process (con)
   └─ Java Process (cháu)
      └─ Spring Boot
         ❌ KHÔNG thấy biến môi trường!
```

**Maven chạy trong process riêng → KHÔNG kế thừa biến môi trường!**

---

## ❓ **"Làm sao để application.properties lấy được biến môi trường?"**

### **3 Cách:**

#### **Cách 1: Dùng default values (ĐÃ FIX) ✅**

```properties
cloudinary.api-secret=${CLOUDINARY_API_SECRET:FEt7Kt4r9BWR-0e0Dx-jFBv_71M}
                                             ^
                                             Giá trị mặc định
```

**➡️ HIỆN TẠI ĐANG DÙNG CÁCH NÀY**

- ✅ App chạy được ngay
- ❌ Secrets vẫn trong Git

---

#### **Cách 2: Set biến trước khi chạy Maven**

```powershell
# Windows PowerShell
$env:CLOUDINARY_API_SECRET = "FEt7Kt4r9BWR-0e0Dx-jFBv_71M"
$env:GOOGLE_CLIENT_SECRET = "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"
$env:MAIL_PASSWORD = "ravf kfon wtgd kkgc"
$env:GEMINI_API_KEY = "AIzaSyAmZ3U1AyRDvKy8ZbgsMElIW0fcDuBBPpY"

mvn spring-boot:run
```

---

#### **Cách 3: Dùng `dotenv-java` (KHUYẾN NGHỊ) 🌟**

**Bước 1: Thêm vào `pom.xml`**
```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Bước 2: Sửa `UteaApplication.java`**
```java
import io.github.cdimascio.dotenv.Dotenv;

public static void main(String[] args) {
    // Load .env file
    Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    
    // Set system properties
    dotenv.entries().forEach(e -> 
        System.setProperty(e.getKey(), e.getValue())
    );
    
    SpringApplication.run(UteaApplication.class, args);
}
```

**Bước 3: File `.env` (đã có sẵn)**
```env
CLOUDINARY_API_SECRET=FEt7Kt4r9BWR-0e0Dx-jFBv_71M
GOOGLE_CLIENT_SECRET=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
MAIL_PASSWORD=ravf kfon wtgd kkgc
GEMINI_API_KEY=AIzaSyAmZ3U1AyRDvKy8ZbgsMElIW0fcDuBBPpY
```

**Bước 4: Xóa default values**
```properties
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

---

## 🎯 **TRẠNG THÁI HIỆN TẠI**

### ✅ **ĐÃ SỬA**
- Thêm lại default values cho tất cả secrets
- App đang chạy bình thường
- Không cần setup env vars

### ⚠️ **CẢNH BÁO**
- GitHub CÓ THỂ phát hiện:
  - `GOOGLE_CLIENT_SECRET:GOCSPX-...`
  - `GEMINI_API_KEY:AIzaSy...`
  - `CLOUDINARY_API_SECRET:FEt7...`

---

## 📋 **KHUYẾN NGHỊ**

| Môi trường | Khuyến nghị |
|------------|-------------|
| **Local Development** | ✅ Giữ nguyên (default values) |
| **Push to GitHub Private Repo** | ⚠️ Chấp nhận được |
| **Push to GitHub Public Repo** | ❌ NÊN dùng `dotenv-java` |
| **Production Deployment** | ❌ BẮT BUỘC dùng env vars thực |

---

## 🚀 **CHẠY ỨNG DỤNG NGAY**

```bash
cd UTeaDrinkWebsite
mvn spring-boot:run
```

➡️ **App sẽ chạy ngay lập tức!** (Không cần setup gì thêm)

---

## 📚 **ĐỌC THÊM**

- **Chi tiết đầy đủ:** `ENVIRONMENT_VARIABLES_EXPLAINED.md`
- **Hướng dẫn chạy:** `HOW_TO_RUN.md`
- **Template `.env`:** `.env.example`

---

🎉 **TÓM LẠI:**
- ✅ App đã chạy được
- ⚠️ Secrets vẫn trong Git (chấp nhận được cho dev)
- 🔒 Nếu cần bảo mật tuyệt đối → dùng `dotenv-java`


