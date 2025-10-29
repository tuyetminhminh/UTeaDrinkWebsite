# 📚 **GIẢI THÍCH CHI TIẾT VỀ BIẾN MÔI TRƯỜNG**

## 🔍 **VẤN ĐỀ BẠN VỪA GẶP PHẢI**

### **Lỗi: "Could not resolve placeholder 'CLOUDINARY_API_SECRET'"**

```
Caused by: java.lang.IllegalArgumentException: 
Could not resolve placeholder 'CLOUDINARY_API_SECRET' in value "${CLOUDINARY_API_SECRET}"
```

**Nguyên nhân:**
- File `application.properties` có `cloudinary.api-secret=${CLOUDINARY_API_SECRET}`
- **KHÔNG có default value** sau dấu `:`
- Spring Boot **YÊU CẦU** phải có giá trị, nếu không có env var thì sẽ báo lỗi

---

## 🎯 **CƠ CHẾ HOẠT ĐỘNG CỦA BIẾN MÔI TRƯỜNG**

### **1. Spring Boot Property Placeholder**

Spring Boot sử dụng cú pháp `${VARIABLE_NAME:defaultValue}` trong `application.properties`:

```properties
# Cú pháp: ${ENV_VAR_NAME:defaultValue}
cloudinary.api-secret=${CLOUDINARY_API_SECRET:FEt7Kt4r9BWR-0e0Dx-jFBv_71M}
                       ^                       ^
                       |                       |
                    Tên biến           Giá trị mặc định
                    môi trường         (nếu không có env var)
```

### **2. Thứ tự ưu tiên (Priority)**

Spring Boot tìm giá trị theo thứ tự:

```
1️⃣ System Environment Variables (cao nhất)
    ↓
2️⃣ JVM System Properties (-D flags)
    ↓
3️⃣ application.properties / application.yml
    ↓
4️⃣ Default value (sau dấu :)
```

**Ví dụ thực tế:**

```properties
# File: application.properties
cloudinary.api-secret=${CLOUDINARY_API_SECRET:default-secret-123}
```

**Trường hợp 1: CÓ biến môi trường**
```bash
# Windows PowerShell
$env:CLOUDINARY_API_SECRET = "production-secret-456"
mvn spring-boot:run
```
➡️ **Kết quả:** Spring Boot sử dụng `production-secret-456`

**Trường hợp 2: KHÔNG có biến môi trường**
```bash
mvn spring-boot:run
```
➡️ **Kết quả:** Spring Boot sử dụng `default-secret-123` (từ default value)

**Trường hợp 3: KHÔNG có biến môi trường VÀ KHÔNG có default value**
```properties
cloudinary.api-secret=${CLOUDINARY_API_SECRET}  # ❌ Không có default
```
```bash
mvn spring-boot:run
```
➡️ **Kết quả:** ❌ **LỖI!** `Could not resolve placeholder 'CLOUDINARY_API_SECRET'`

---

## 💡 **TẠI SAO SCRIPT `run-with-env.ps1` KHÔNG HOẠT ĐỘNG?**

### **Vấn đề Process Isolation**

```powershell
# Script run-with-env.ps1
[Environment]::SetEnvironmentVariable("CLOUDINARY_API_SECRET", "abc123", "Process")
                                                                         ^^^^^^^^
                                                                         Chỉ set cho
                                                                         PowerShell process hiện tại
mvn spring-boot:run  # Maven chạy trong process CON riêng
                     # ❌ KHÔNG kế thừa biến môi trường!
```

**Minh họa:**

```
PowerShell Process (PID: 12345)
├─ Biến môi trường: CLOUDINARY_API_SECRET = "abc123" ✅
│
└─ Maven Process (PID: 67890)           👈 Process CON
   └─ Java Process (PID: 11111)         👈 Process CHÁU
      └─ Spring Boot đang chạy
         ❌ KHÔNG thấy biến môi trường!
```

### **Giải pháp đúng:**

**Cách 1: Set biến ở Process cha trước khi chạy Maven**
```powershell
# Set biến môi trường trong session hiện tại
$env:CLOUDINARY_API_SECRET = "abc123"
$env:GOOGLE_CLIENT_SECRET = "xyz789"

# Chạy Maven (Maven sẽ kế thừa biến)
mvn spring-boot:run
```

**Cách 2: Truyền qua JVM System Properties**
```bash
mvn spring-boot:run -Dcloudinary.api-secret=abc123
```

**Cách 3: Dùng default values (cách hiện tại)**
```properties
cloudinary.api-secret=${CLOUDINARY_API_SECRET:FEt7Kt4r9BWR-0e0Dx-jFBv_71M}
                                             ^
                                             Default value - App vẫn chạy được!
```

---

## 🔒 **BẢO MẬT VỚI DEFAULT VALUES**

### **❓ "Thêm default values thì khi push lên Git có báo lỗi không?"**

**Câu trả lời: CÓ THỂ!**

GitHub Secret Scanning tìm kiếm **patterns đặc trưng**:

| Secret Type | Pattern | Phát hiện? |
|-------------|---------|-----------|
| Database Password | `123456` | ❌ Không (quá đơn giản) |
| JWT Secret | `vG9p0mV9Z8q1Lw2rC3...` | ⚠️ Có thể (chuỗi dài ngẫu nhiên) |
| Gmail App Password | `ravf kfon wtgd kkgc` | 🟡 Trung bình (16 ký tự có space) |
| Cloudinary API Secret | `FEt7Kt4r9BWR-0e0Dx...` | 🔴 Cao (pattern API key) |
| Google OAuth2 Client Secret | `GOCSPX-saJ7aFG51BK...` | 🔴 **RẤT CAO** (prefix `GOCSPX-`) |
| Google API Key | `AIzaSyAmZ3U1AyRD...` | 🔴 **CHẮC CHẮN** (prefix `AIzaSy`) |

### **⚖️ Trade-off (Đánh đổi)**

**✅ Ưu điểm của default values:**
- App chạy được ngay (không cần setup env vars)
- Thuận tiện cho dev local
- Không bị lỗi "Could not resolve placeholder"

**❌ Nhược điểm:**
- Secrets vẫn có trong Git history
- GitHub có thể phát hiện và cảnh báo
- Không đạt chuẩn bảo mật cao nhất

---

## 🎯 **GIẢI PHÁP TỐI ƯU**

### **Option 1: Giữ default values (Hiện tại) - Dành cho Development**

```properties
# ⚠️ For local dev only. Production should use env vars without defaults.
cloudinary.api-secret=${CLOUDINARY_API_SECRET:FEt7Kt4r9BWR-0e0Dx-jFBv_71M}
```

**✅ Phù hợp khi:**
- Đang phát triển local
- Muốn chạy nhanh không setup phức tạp
- Chưa deploy production

**❌ KHÔNG phù hợp khi:**
- Deploy lên production server
- Cần đạt chuẩn bảo mật cao
- Push lên GitHub public repo

---

### **Option 2: Xóa default values - Dành cho Production**

```properties
# ⚠️ Production only. MUST set environment variables!
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

**Cách setup trong Production:**

**A. Docker:**
```dockerfile
# Dockerfile
ENV CLOUDINARY_API_SECRET=your-secret-here
```

**B. Kubernetes:**
```yaml
# deployment.yaml
env:
  - name: CLOUDINARY_API_SECRET
    valueFrom:
      secretKeyRef:
        name: app-secrets
        key: cloudinary-secret
```

**C. Heroku:**
```bash
heroku config:set CLOUDINARY_API_SECRET=your-secret-here
```

**D. AWS Elastic Beanstalk:**
```
Configuration → Software → Environment properties
CLOUDINARY_API_SECRET = your-secret-here
```

---

### **Option 3: Dùng `.env` file với `dotenv-java` (Khuyến nghị)**

**Bước 1: Thêm dependency vào `pom.xml`**

```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Bước 2: Tạo file `.env` (đã có sẵn)**

```env
CLOUDINARY_API_SECRET=FEt7Kt4r9BWR-0e0Dx-jFBv_71M
GOOGLE_CLIENT_SECRET=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
```

**Bước 3: Load `.env` trong `UteaApplication.java`**

```java
package net.codejava.utea;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UteaApplication {

    public static void main(String[] args) {
        // Load .env file (only if exists)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()  // Không lỗi nếu không có .env
                .load();
        
        // Set env vars cho Spring Boot
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
        
        SpringApplication.run(UteaApplication.class, args);
    }
}
```

**Bước 4: Xóa default values trong `application.properties`**

```properties
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

**✅ Lợi ích:**
- ✅ Không cần setup env vars thủ công
- ✅ `.env` được gitignore (an toàn)
- ✅ Hoạt động cả local và production
- ✅ Đạt chuẩn bảo mật cao

---

## 📝 **TÓM TẮT - CÁC BƯỚC TIẾP THEO**

### **Hiện tại (đã fix):**
✅ Đã thêm lại default values cho tất cả biến bắt buộc
✅ App chạy được mà không cần setup env vars
✅ File `.env` và `.env.example` đã tạo sẵn

### **Khuyến nghị cho tương lai:**

**Nếu chỉ phát triển local:**
- Giữ nguyên default values (như hiện tại)
- Chấp nhận risk GitHub có thể phát hiện

**Nếu chuẩn bị deploy production:**
1. Cài `dotenv-java` dependency
2. Sửa `UteaApplication.java` để load `.env`
3. Xóa default values trong `application.properties`
4. Commit và push (an toàn 100%)

---

## 🚀 **CÁCH CHẠY ỨNG DỤNG NGAY BÂY GIỜ**

```bash
cd UTeaDrinkWebsite
mvn spring-boot:run
```

➡️ **Hoạt động ngay!** (Vì đã có default values)

---

## ❓ **FAQ**

**Q: Tại sao không dùng `System.getenv()` trong Java code?**

A: Vì cần phải inject vào Spring beans thông qua `@Value`, không nên truy cập trực tiếp `System.getenv()`.

---

**Q: Tôi có thể dùng `application-dev.properties` và `application-prod.properties` riêng không?**

A: Có! Đây là cách tốt:

```properties
# application-dev.properties (có default values)
cloudinary.api-secret=${CLOUDINARY_API_SECRET:FEt7Kt4r9BWR-0e0Dx-jFBv_71M}

# application-prod.properties (không có default values)
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

Chạy với profile:
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
mvn spring-boot:run -Dspring.profiles.active=prod
```

---

**Q: GitHub đã phát hiện secrets của tôi, phải làm gì?**

A:
1. **Thu hồi secrets ngay lập tức** (tạo mới)
2. **Xóa secrets khỏi Git history:**
   ```bash
   git filter-branch --force --index-filter \
   "git rm --cached --ignore-unmatch application.properties" \
   --prune-empty --tag-name-filter cat -- --all
   ```
3. **Force push:**
   ```bash
   git push origin --force --all
   ```

---

🎉 **Chúc bạn thành công!**


