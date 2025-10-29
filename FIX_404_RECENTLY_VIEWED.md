# 🔧 SỬA LỖI 404 - "Đã xem gần đây"

## ❌ LỖI

```
🔎 Không tìm thấy nội dung bạn yêu cầu (404)
Khi truy cập: /customer/recently-viewed
```

## ✅ NGUYÊN NHÂN

Code **đã đúng** nhưng **chưa restart ứng dụng**!

Spring Boot cần restart để:
1. Load controller method mới vào ApplicationContext
2. Register route `/customer/recently-viewed`
3. Map với handler method

## 🔍 ĐÃ KIỂM TRA

### 1. Controller Method ✅
**File:** `ProductCusController.java`
```java
@Controller
@RequestMapping("/customer")  // ✅ Base path
public class ProductCusController {
    
    @GetMapping("/recently-viewed")  // ✅ Method mapping
    public String recentlyViewed(...) {
        // Full path: /customer/recently-viewed ✅
    }
}
```

### 2. Security Config ✅
**File:** `SecurityConfig.java`
```java
.requestMatchers("/customer/**").hasAuthority("CUSTOMER")
```
Route `/customer/recently-viewed` được bảo vệ bởi CUSTOMER authority ✅

### 3. Template File ✅
**File:** `templates/customer/recently-viewed.html`
- Tồn tại ✅
- Syntax đúng ✅

### 4. Compile ✅
```bash
mvn clean compile -DskipTests
# [INFO] BUILD SUCCESS ✅
```

## 🚀 GIẢI PHÁP

### ⚠️ **QUAN TRỌNG: PHẢI RESTART ỨNG DỤNG**

#### Cách 1: Restart từ Terminal

**PowerShell:**
```powershell
# Dừng app hiện tại (Ctrl+C)
cd UTeaDrinkWebsite
mvn spring-boot:run
```

**Bash/Linux/Mac:**
```bash
# Dừng app hiện tại (Ctrl+C)
cd UTeaDrinkWebsite
mvn spring-boot:run
```

#### Cách 2: Restart từ IDE

**IntelliJ IDEA:**
1. Click nút Stop (hình vuông đỏ)
2. Click Run 'UteaApplication' (Shift+F10)

**VS Code:**
1. Stop Java application
2. Run → Start Debugging (F5)

#### Cách 3: Kill Process & Restart

```powershell
# Windows PowerShell
Get-Process -Name java | Stop-Process -Force
cd UTeaDrinkWebsite
mvn spring-boot:run
```

```bash
# Linux/Mac
pkill -9 java
cd UTeaDrinkWebsite
mvn spring-boot:run
```

### ✅ SAU KHI RESTART

1. **Đợi log khởi động:**
```
Started UteaApplication in X.XXX seconds
```

2. **Test route:**
```
http://localhost:8080/customer/recently-viewed
```

3. **Kết quả mong đợi:**
- ✅ Trang hiển thị đầy đủ
- ✅ Header customer
- ✅ Title "Đã xem gần đây"
- ✅ Grid sản phẩm (nếu đã xem sản phẩm)
- ✅ Empty state (nếu chưa xem sản phẩm nào)

## 🧪 KIỂM TRA SAU KHI RESTART

### Bước 1: Kiểm tra route có hoạt động không

```bash
# Test endpoint
curl http://localhost:8080/customer/recently-viewed
```

**Kết quả mong đợi:**
- ❌ 404 → Vẫn lỗi, cần kiểm tra thêm
- ✅ 302 (redirect to /login) → Route hoạt động! (chưa đăng nhập)
- ✅ 200 + HTML → Route hoạt động! (đã đăng nhập)

### Bước 2: Đăng nhập và test

1. Đăng nhập: `http://localhost:8080/login`
2. Click avatar → "Đã xem gần đây"
3. ✅ Trang hiển thị

### Bước 3: Xem sản phẩm để tạo dữ liệu

1. Vào Menu: `/customer/menu`
2. Click xem **5-10 sản phẩm**
3. Quay lại "Đã xem gần đây"
4. ✅ Hiển thị danh sách sản phẩm

## 🐛 NẾU VẪN LỖI 404 SAU KHI RESTART

### 1. Kiểm tra log khi khởi động

```
Mapped "{[/customer/recently-viewed],methods=[GET]}" onto public ...
```

**Nếu KHÔNG thấy log này** → Controller chưa được register

### 2. Kiểm tra compile

```powershell
cd UTeaDrinkWebsite
mvn clean compile -DskipTests
```

**Kiểm tra file compiled:**
```
target/classes/net/codejava/utea/customer/controller/ProductCusController.class
```

File này phải tồn tại và có timestamp mới nhất.

### 3. Kiểm tra Spring Boot version

File `pom.xml`:
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.x.x</version> <!-- Phải >= 3.0.0 -->
</parent>
```

### 4. Force rebuild

```powershell
cd UTeaDrinkWebsite
mvn clean install -DskipTests
mvn spring-boot:run
```

### 5. Clear cache & rebuild

```powershell
# Xóa target directory
rm -r target

# Rebuild
mvn clean package -DskipTests
mvn spring-boot:run
```

## 📝 DEBUG TIPS

### 1. Bật debug log

File `application.properties`:
```properties
# Bật debug cho Spring MVC
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.web.servlet.mvc.method.annotation=TRACE
```

Restart và xem log mapping:
```
RequestMappingHandlerMapping : Mapped "{[/customer/recently-viewed]..."
```

### 2. Thêm log vào method

```java
@GetMapping("/recently-viewed")
public String recentlyViewed(...) {
    System.out.println("=== RECENTLY VIEWED ACCESSED ===");
    // ... rest of code
}
```

Nếu log KHÔNG xuất hiện → Route chưa được call
Nếu log XUẤT HIỆN → Route OK, kiểm tra logic bên trong

### 3. Test với curl

```bash
# Test với header
curl -v http://localhost:8080/customer/recently-viewed \
  -H "Cookie: UTEA_TOKEN=your_jwt_token_here"
```

## ✅ CHECKLIST

Trước khi báo lỗi, đảm bảo đã làm:

- [ ] ✅ Compile code: `mvn clean compile`
- [ ] ✅ **RESTART ứng dụng** (quan trọng nhất!)
- [ ] ✅ Đợi log "Started UteaApplication..."
- [ ] ✅ Đăng nhập với tài khoản CUSTOMER
- [ ] ✅ Test truy cập `/customer/recently-viewed`
- [ ] ✅ Kiểm tra console log (không có lỗi)
- [ ] ✅ Kiểm tra browser console (F12)

## 🎯 TÓM TẮT

**Vấn đề:** 404 khi truy cập `/customer/recently-viewed`

**Nguyên nhân:** Chưa restart ứng dụng sau khi thêm code mới

**Giải pháp:** 
```powershell
# Dừng app (Ctrl+C)
cd UTeaDrinkWebsite
mvn spring-boot:run
# Đợi "Started UteaApplication..."
```

**Sau đó:**
1. Đăng nhập
2. Click avatar → "Đã xem gần đây"
3. ✅ Trang hiển thị!

---

**99% trường hợp lỗi 404 sau khi thêm controller method mới là do CHƯA RESTART!** 🔄

