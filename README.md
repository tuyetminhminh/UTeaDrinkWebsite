# UTeaDrinkWebsite
# PHÂN CÔNG NHIỆM VỤ - NHÓM 9

## Thành viên 1: Phạm Thiên Hoàng
**Công việc:**
- [ ] Setup project Spring Boot
- [ ] Cấu hình Spring Security + JWT
- [ ] Authentication & Authorization
- [ ] Email Service với OTP
- [ ] WebSocket configuration
- [ ] Mã hóa mật khẩu

**Entity phụ trách:**
- User, Role, Permission
- OTP, Email Template

## Thành viên 2: Phạm Thị Tuyết MInh 
**Công việc:**
- [ ] Trang chủ, danh mục sản phẩm
- [ ] Tìm kiếm, lọc, phân trang
- [ ] Chi tiết sản phẩm
- [ ] Giỏ hàng, Wishlist
- [ ] Profile user management
- [ ] Lịch sử xem sản phẩm

**Entity phụ trách:**
- Product, Category, ProductImage
- Cart, CartItem, Wishlist
- UserProfile, Address

## Thành viên 3: Đặng Công Quân
**Công việc:**
- [ ] Order management system
- [ ] Payment integration (VNPay, Momo)
- [ ] Review & Comment system
- [ ] Coupon & Discount
- [ ] Order history & tracking

**Entity phụ trách:**
- Order, OrderItem, OrderStatus
- Payment, PaymentMethod
- Review, Comment
- Coupon, Discount

## Thành viên 4: Cáp Thanh Nhàn
**Công việc:**
- [ ] Vendor shop management
- [ ] Admin dashboard
- [ ] Category management
- [ ] Promotion management
- [ ] Revenue statistics
- [ ] Shipping provider management

**Entity phụ trách:**
- Shop, Vendor
- Promotion, Revenue
- ShippingProvider
- AdminSettings

## Timeline tháng 10
**Từ ngày 8 - 10:** Thiên Hoàng
**Từ ngày 11 - 13:** Tuyết Minh
**Từ ngày 14 - 16:** Công Quân
**Từ ngày 14 - 16:** Thanh Nhàn

🗓️ LỊCH TRÌNH CHI TIẾT HOÀN THÀNH ĐẾN 23/10
📅 LỊCH TRÌNH NÉN - 2.5 TUẦN (7/10 - 23/10)
TUẦN 1 (7/10 - 13/10): CORE FOUNDATION
Ngày 7-8/10: PROJECT SETUP (Thiên Hoàng)

**BẮT BUỘC HOÀN THÀNH:**
- [x] Spring Boot project với đầy đủ dependencies
- [x] Database configuration (MySQL)
- [x] Basic entity structure (User, Role, Product, Category)
- [x] Spring Security + JWT setup
- [x] Git repository organization
Ngày 9-10/10: AUTHENTICATION SYSTEM (Thiên Hoàng)

**CỐT LÕI:**
- [ ] Complete JWT authentication
- [ ] User registration với OTP email
- [ ] Login/Logout functionality
- [ ] Password encryption (BCrypt)
- [ ] Forgot password với OTP

**MERGE vào main ngày 10/10**
Ngày 11-12/10: PRODUCT SYSTEM (Tuyết Minh)

**CỐT LÕI:**
- [ ] Product, Category entities hoàn chỉnh
- [ ] Product repository + service
- [ ] Homepage controller + basic template
- [ ] Product listing với pagination
- [ ] Search & filter functionality

**Bootstrap template áp dụng**
Ngày 13/10: INTEGRATION & TESTING

**CẢ NHÓM:**
- [ ] Merge all code vào main
- [ ] Fix conflicts + integration issues
- [ ] Basic testing authentication + product display
- [ ] Database seeding với sample data
TUẦN 2 (14/10 - 20/10): USER FEATURES & ORDER SYSTEM
Ngày 14-15/10: USER PROFILE & CART (Tuyết Minh + Công Quân)

**TUYẾT MINH:**
- [ ] User profile management
- [ ] Address management (multiple addresses)
- [ ] Product detail page
- [ ] Wishlist functionality

**CÔNG QUÂN:**
- [ ] Cart system (database storage)
- [ ] CartItem entity + relationships
- [ ] Add/remove/update cart items
- [ ] Cart UI với Bootstrap
Ngày 16-17/10: ORDER SYSTEM (Công Quân)

**CỐT LÕI:**
- [ ] Order + OrderItem entities
- [ ] Order status workflow (6 trạng thái)
- [ ] Order management service
- [ ] Order history với filtering
- [ ] Basic checkout process

**MERGE ngày 17/10**
Ngày 18-19/10: PAYMENT & REVIEWS (Công Quân + Tuyết Minh)

**CÔNG QUÂN:**
- [ ] Payment integration (COD + VNPay)
- [ ] Payment validation + processing

**TUYẾT MINH:**
- [ ] Review system (50+ characters validation)
- [ ] Comment system với image upload
- [ ] Product rating functionality
- [ ] Viewed products history
Ngày 20/10: INTEGRATION WEEK 2

**CẢ NHÓM:**
- [ ] Merge complete user features
- [ ] End-to-end testing: Register → Browse → Cart → Checkout → Pay
- [ ] Fix critical bugs
- [ ] Responsive design check
TUẦN 2.5 (21/10 - 23/10): VENDOR & ADMIN + FINALIZATION
Ngày 21/10: VENDOR SYSTEM (Thanh Nhàn)

**CỐT LÕI:**
- [ ] Shop registration + management
- [ ] Vendor dashboard
- [ ] Product management for vendors
- [ ] Vendor order management

**ƯU TIÊN: Shop registration + product management**
Ngày 22/10: ADMIN SYSTEM (Thanh Nhàn)

**CỐT LÕI:**
- [ ] Admin dashboard
- [ ] User management
- [ ] Category management
- [ ] Product management across shops
- [ ] Shipping provider management

**ƯU TIÊN: User management + category management**
Ngày 23/10: FINAL INTEGRATION & DEPLOYMENT

**CẢ NHÓM:**
- [ ] FINAL MERGE tất cả tính năng
- [ ] Comprehensive testing tất cả role
- [ ] Fix critical bugs cuối cùng
- [ ] Database seeding đầy đủ
- [ ] Preparation for demonstration
- [ ] Documentation finalization
🎯 PHÂN CÔNG THEO TÍNH NĂNG BẮT BUỘC
THIÊN HOÀNG: Authentication & Security

**PHẢI HOÀN THÀNH TRƯỚC 10/10:**
- [ ] JWT Authentication ✅
- [ ] OTP Email registration ✅  
- [ ] Password reset với OTP ✅
- [ ] Role-based authorization ✅
- [ ] Security configuration ✅
TUYẾT MINH: Product & User Features

**PHẢI HOÀN THÀNH TRƯỚC 15/10:**
- [ ] Product system (CRUD) ✅
- [ ] Search & filter ✅
- [ ] Pagination (20 products) ✅
- [ ] User profile + addresses ✅
- [ ] Product reviews + comments ✅
- [ ] Wishlist + viewed history ✅
CÔNG QUÂN: Order & Payment

**PHẢI HOÀN THÀNH TRƯỚC 19/10:**
- [ ] Cart system (database) ✅
- [ ] Order management ✅
- [ ] Payment integration (COD + VNPay) ✅
- [ ] Order status workflow ✅
- [ ] Order history + tracking ✅
THANH NHÀN: Vendor & Admin

**PHẢI HOÀN THÀNH TRƯỚC 22/10:**
- [ ] Vendor shop registration ✅
- [ ] Vendor product/order management ✅
- [ ] Admin dashboard ✅
- [ ] User/category management ✅
- [ ] Shipping provider management ✅
⚡ CHIẾN LƯỢC TỐI ƯU CHO DEADLINE 23/10
1. Ưu tiên tính năng CỐT LÕI:

**MUST-HAVE (90% điểm):**
- Authentication + OTP ✅
- Product browsing + search ✅  
- Cart + Order + Payment ✅
- User profile + reviews ✅
- Vendor shop management ✅
- Admin basic management ✅

**NICE-TO-HAVE (10% điểm):**
- Shipper role ❌ (BỎ QUA nếu không kịp)
- Complex revenue statistics ❌ (Simplified)
- Multiple payment gateways ❌ (Chỉ VNPay)
2. Parallel Development Strategy:

**TUẦN 1:** Thiên Hoàng (Auth) → Tuyết Minh (Product)
**TUẦN 2:** Tuyết Minh (User) + Công Quân (Cart/Order) PARALLEL
**TUẦN 2.5:** Thanh Nhàn (Vendor/Admin) + Others testing
3. Daily Integration:

**MỖI TỐI 9PM:** 
- Merge code vào development branch
- Quick testing session
- Plan for next day

**WEEKEND 12-13/10 & 19-20/10:** 
- Intensive coding sessions
- Critical integration points
🚨 CHECKLIST HOÀN THÀNH THEO NGÀY
10/10 CHECKPOINT:

- [ ] Users can register với OTP ✅
- [ ] Users can login/logout ✅  
- [ ] Basic product display ✅
- [ ] Database relationships working ✅
15/10 CHECKPOINT:

- [ ] Full product browsing + search ✅
- [ ] User profile management ✅
- [ ] Shopping cart functional ✅
- [ ] Responsive Bootstrap UI ✅
20/10 CHECKPOINT:

- [ ] Complete order workflow ✅
- [ ] Payment integration working ✅
- [ ] Product reviews + ratings ✅
- [ ] End-to-end user journey tested ✅
23/10 FINAL CHECKPOINT:

- [ ] Vendor system operational ✅
- [ ] Admin management working ✅
- [ ] All role functionalities tested ✅
- [ ] Final documentation ✅
- [ ] Demo preparation ✅
📊 ĐẢM BẢO CHẤT LƯỢNG VỚI THỜI GIAN NGẮN
Code Quality Shortcuts:
java
// Sử dụng Lombok để giảm boilerplate code
@Entity @Data @Builder
public class User {
    private String email;
    private String password;
}

// Sử dụng Spring Data JPA methods thay v custom query
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findTop20ByOrderByCreatedAtDesc();
    List<Product> findByNameContaining(String keyword);
}
Template Strategy:
html
<!-- Sử dụng Bootstrap 5 template có sẵn -->
<!-- Tập trung vào functionality thay vì custom design -->
Với lịch trình này, nhóm bạn có thể HOÀN THÀNH 95% yêu cầu và ĐẠT 9+ ĐIỂM trước deadline 23/10! 🎯🚀




