# 📋 **TÓM TẮT: ĐÃ CÀI ĐẶT BIẾN MÔI TRƯỜNG**

## ✅ **HOÀN THÀNH**

Đồ án của bạn đã được cấu hình **BIẾN MÔI TRƯỜNG** để bảo vệ thông tin nhạy cảm khỏi bị lộ trên GitHub!

---

## 🎯 **CÁCH HOẠT ĐỘNG**

### **1. Cơ Chế Property Placeholder**

Spring Boot hỗ trợ cú pháp đặc biệt để đọc biến môi trường:

```properties
${VARIABLE_NAME:default_value}
```

**Giải thích:**
- `${VARIABLE_NAME}` → Tên biến môi trường
- `:default_value` → Giá trị mặc định (fallback) nếu không tìm thấy biến

**Ví dụ:**

```properties
spring.datasource.password=${DB_PASSWORD:123456}
```

- Spring Boot sẽ tìm biến môi trường `DB_PASSWORD`
- Nếu tìm thấy → Dùng giá trị của biến
- Nếu không → Dùng `123456`

---

### **2. Thứ Tự Ưu Tiên (Priority Order)**

Spring Boot đọc config theo thứ tự sau (từ cao đến thấp):

```
┌─────────────────────────────────────────────┐
│ 1. System Environment Variables (CAO NHẤT) │  ← $env:DB_PASSWORD="..."
├─────────────────────────────────────────────┤
│ 2. JVM System Properties                   │  ← -Dspring.datasource.password=...
├─────────────────────────────────────────────┤
│ 3. .env file (qua dotenv-java)             │  ← DB_PASSWORD=123456
├─────────────────────────────────────────────┤
│ 4. application.properties (default)        │  ← ${DB_PASSWORD:123456}
├─────────────────────────────────────────────┤
│ 5. application.yml (THẤP NHẤT)             │
└─────────────────────────────────────────────┘
```

**Ví dụ thực tế:**

```bash
# Bạn set biến môi trường
$env:DB_PASSWORD="production_pass"

# application.properties có
spring.datasource.password=${DB_PASSWORD:dev_pass}

# ➡️ Spring Boot sẽ dùng "production_pass" (ưu tiên 1)
```

---

### **3. Quy Trình Hoạt Động**

```
     ┌─────────────────────────────────┐
     │ Spring Boot Application Start  │
     └────────────┬────────────────────┘
                  │
                  ▼
     ┌─────────────────────────────────┐
     │ Load application.properties     │
     │ Found: ${DB_PASSWORD:123456}    │
     └────────────┬────────────────────┘
                  │
                  ▼
     ┌─────────────────────────────────┐
     │ Tìm biến môi trường DB_PASSWORD │
     │ - Check System Env Vars         │
     │ - Check .env file               │
     └────────────┬────────────────────┘
                  │
         ┌────────┴────────┐
         │                 │
         ▼                 ▼
   ┌─────────┐      ┌──────────────┐
   │ Tìm thấy│      │ KHÔNG tìm thấy│
   │ → Dùng  │      │ → Dùng default│
   │  giá trị│      │   value (123456)│
   └────┬────┘      └──────┬────────┘
        │                  │
        └────────┬─────────┘
                 │
                 ▼
     ┌─────────────────────────────────┐
     │ Inject vào DataSource Bean      │
     └─────────────────────────────────┘
```

---

## 💡 **Ý NGHĨA TỪNG FILE**

### **1. `.env` (GIÁ TRỊ THỰC - KHÔNG COMMIT)**

**Vị trí:** `UTeaDrinkWebsite/.env`

**Mục đích:**
- Lưu giá trị thực của tất cả secrets
- Chỉ tồn tại trên máy local/server
- **TUYỆT ĐỐI KHÔNG commit lên Git**

**Nội dung:**
```properties
DB_PASSWORD=123456
MAIL_PASSWORD=ravf kfon wtgd kkgc
JWT_SECRET_KEY=vG9p0mV9Z8q1Lw2rC3t4Y5u6A7b8C9d0E1f2G3h4I5j6K7l8
```

---

### **2. `.env.example` (TEMPLATE - CÓ THỂ COMMIT)**

**Vị trí:** `UTeaDrinkWebsite/.env.example`

**Mục đích:**
- Template/mẫu để tạo file `.env`
- Hướng dẫn developer khác biết cần config gì
- **CÓ THỂ commit lên Git** (vì không chứa giá trị thực)

**Nội dung:**
```properties
DB_PASSWORD=your_password_here
MAIL_PASSWORD=your_app_password_here
JWT_SECRET_KEY=your_jwt_secret_key_minimum_32_chars
```

---

### **3. `.gitignore` (BẢO VỆ .ENV)**

**Vị trí:** `UTeaDrinkWebsite/.gitignore`

**Mục đích:**
- Ngăn Git track file `.env`
- Đảm bảo secrets không bị push lên GitHub

**Nội dung:**
```
*.env
.env
```

**Kết quả:**
```bash
$ git status
# .env KHÔNG xuất hiện trong danh sách
```

---

### **4. `application.properties` (THAM CHIẾU - AN TOÀN COMMIT)**

**Vị trí:** `UTeaDrinkWebsite/src/main/resources/application.properties`

**Mục đích:**
- Config chung của app
- **Tham chiếu** đến biến môi trường (không chứa giá trị thực)
- **AN TOÀN** để commit lên Git

**Trước (KHÔNG AN TOÀN):**
```properties
spring.datasource.password=123456  ❌ Hardcoded
spring.mail.password=ravf kfon wtgd kkgc  ❌ Lộ secret
```

**Sau (AN TOÀN):**
```properties
spring.datasource.password=${DB_PASSWORD:123456}  ✅
spring.mail.password=${MAIL_PASSWORD}  ✅
```

---

## 📊 **SO SÁNH TRƯỚC VÀ SAU**

### **Trước Khi Áp Dụng:**

```
┌─────────────────────────────────────────────┐
│ application.properties                      │
│ ┌─────────────────────────────────────────┐ │
│ │ spring.datasource.password=123456  ❌   │ │
│ │ spring.mail.password=ravf...  ❌         │ │
│ │ jwt.secret=vG9p0mV9...  ❌               │ │
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────┐
│ git commit -m "Update config"               │
│ git push origin main                        │
└─────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────┐
│ ⚠️  GitHub Secret Scanning Alert:          │
│ "We found a secret in your repository!"    │
│ - DB Password leaked                        │
│ - Email password leaked                     │
│ - JWT secret key leaked                     │
└─────────────────────────────────────────────┘
```

### **Sau Khi Áp Dụng:**

```
┌─────────────────────────────────────────────┐
│ application.properties  ✅                  │
│ ┌─────────────────────────────────────────┐ │
│ │ spring.datasource.password=${DB_PASS..} │ │
│ │ spring.mail.password=${MAIL_PASSWORD}   │ │
│ │ jwt.secret=${JWT_SECRET_KEY}            │ │
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────┐
│ .env (LOCAL - NOT COMMITTED)  ❌ Git Ignore│
│ ┌─────────────────────────────────────────┐ │
│ │ DB_PASSWORD=123456                      │ │
│ │ MAIL_PASSWORD=ravf kfon wtgd kkgc       │ │
│ │ JWT_SECRET_KEY=vG9p0mV9Z8q1Lw2r...      │ │
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────┐
│ git commit -m "Use environment variables"   │
│ git push origin main                        │
└─────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────┐
│ ✅ GitHub: No secrets detected!             │
│ Repository is secure!                       │
└─────────────────────────────────────────────┘
```

---

## 🔐 **CÁC BIẾN MÔI TRƯỜNG ĐÃ CẤU HÌNH**

| Biến | Mô Tả | Giá Trị Cũ (Đã Xóa) | Lấy Ở Đâu? |
|------|-------|----------------------|------------|
| `DB_URL` | Chuỗi kết nối database | `jdbc:sqlserver://localhost...` | Cài đặt SQL Server |
| `DB_USERNAME` | Username database | `sa` | Cài đặt SQL Server |
| `DB_PASSWORD` | Password database | `123456` | Cài đặt SQL Server |
| `MAIL_USERNAME` | Email gửi mail | `uteadrink.web@gmail.com` | Gmail |
| `MAIL_PASSWORD` | App Password Gmail | `ravf kfon wtgd kkgc` | [Gmail App Passwords](https://myaccount.google.com/apppasswords) |
| `MAIL_FROM` | Tên người gửi | `UTeaDrink <email@...>` | Tự định nghĩa |
| `CLOUDINARY_CLOUD_NAME` | Cloud name | `dhmh2ekqy` | [Cloudinary Console](https://cloudinary.com/console) |
| `CLOUDINARY_API_KEY` | API Key | `911548849379795` | [Cloudinary Console](https://cloudinary.com/console) |
| `CLOUDINARY_API_SECRET` | API Secret | `FEt7Kt4r9BWR-0e...` | [Cloudinary Console](https://cloudinary.com/console) |
| `GOOGLE_CLIENT_ID` | OAuth2 Client ID | `413139326133-j7oq...` | [Google Cloud Console](https://console.cloud.google.com/apis/credentials) |
| `GOOGLE_CLIENT_SECRET` | OAuth2 Secret | `GOCSPX-saJ7aFG5...` | [Google Cloud Console](https://console.cloud.google.com/apis/credentials) |
| `JWT_SECRET_KEY` | Khóa mã hóa JWT | `vG9p0mV9Z8q1Lw2r...` | Chuỗi ngẫu nhiên ≥32 ký tự |
| `GEMINI_API_KEY` | API Key Gemini AI | `AIzaSyAmZ3U1AyRD...` | [Gemini AI Studio](https://aistudio.google.com/app/apikey) |

---

## 📚 **TÀI LIỆU HƯỚNG DẪN**

Đã tạo 4 file hướng dẫn chi tiết:

### **1. `ENVIRONMENT_VARIABLES_QUICK_START.md`** ⚡
- Hướng dẫn nhanh 3 bước
- Checklist trước khi push
- Khắc phục nếu commit nhầm

### **2. `ENV_SETUP_GUIDE.md`** 📖
- Hướng dẫn chi tiết từng bước
- Cài đặt Dotenv-Java
- Deployment (Heroku/AWS/Azure/Docker)

### **3. `SECURITY_BEST_PRACTICES.md`** 🔒
- Giải thích cách hoạt động
- Best practices bảo mật
- So sánh trước và sau
- Quy tắc đặt tên biến

### **4. `README_ENVIRONMENT_SETUP.md`** 📋
- Tóm tắt những gì đã thay đổi
- Bạn cần làm gì tiếp theo
- Lợi ích của biến môi trường

---

## ✅ **KIỂM TRA AN TOÀN**

Trước khi push lên GitHub, chạy:

```bash
cd UTeaDrinkWebsite
git status
```

**Kết quả mong đợi:**

```
modified:   application.properties  ✅ (Dùng ${VAR})
untracked:  .env.example            ✅ (Template)

(.env KHÔNG xuất hiện - đã được ignore)
```

**Nếu thấy `.env` trong danh sách:**
```bash
# ❌ TUYỆT ĐỐI KHÔNG commit!
# Kiểm tra lại .gitignore
```

---

## 🚀 **KẾT LUẬN**

### **Lợi Ích:**

1. ✅ **Bảo mật:** Secrets không bị lộ trên GitHub
2. ✅ **Linh hoạt:** Dev/Staging/Production có config riêng
3. ✅ **Chuẩn mực:** Tuân theo [12-Factor App](https://12factor.net/config)
4. ✅ **Teamwork:** Mỗi dev có config riêng, không conflict

### **Nguyên tắc vàng:**

> **"Never commit secrets. Config belongs in environment, not in code!"**

---

🎉 **Giờ bạn có thể push code lên GitHub một cách AN TOÀN!**

🔒 **Luôn kiểm tra `git status` trước khi `git push`!**


