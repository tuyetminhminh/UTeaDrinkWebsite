# UTeaDrinkWebsite
# PHÂN CÔNG NHIỆM VỤ - NHÓM 9

## Thành viên 1: Phạm Thiên Hoàng : Authentication & Security
**CÔNG VIỆC CHÍNH:**
- Authentication system (8-12/10)
- User profile management (16-17/10)
- WebSocket notifications (18-19/10)
- Security testing (23/10)

## Thành viên 2: Phạm Thị Tuyết Minh : Product & User Features
**CÔNG VIỆC CHÍNH:**
- Product system (9-15/10)
- Review & rating (16-17/10)
- Search & filtering (18-19/10)
- UI refinement (22/10)

## Thành viên 3: Đặng Công Quân : Order & Payment
**CÔNG VIỆC CHÍNH:**
- Cart system (11-15/10)
- Payment integration (16-17/10)
- Order management (18-19/10)
- Payment testing (23/10)

## Thành viên 4: Cáp Thanh Nhàn
**CÔNG VIỆC CHÍNH:**
- Management entities (9-10/10)
- Vendor system (13-17/10)
- Admin system (18-19/10)
- Final testing (23/10)

🗓️ LỊCH TRÌNH CHI TIẾT HOÀN THÀNH ĐẾN 24/10

📅 LỊCH TRÌNH NÉN - 2.5 TUẦN (9/10 - 24/10)

Ngày 9-10/10: PROJECT SETUP & ENTITIES

**TẤT CẢ CÙNG LÀM ENTITIES:**

THIÊN HOÀNG (Auth):
- [ ] User, Role, OTP, Address entities
- [ ] Spring Security config
- [ ] JWT dependencies

TUYẾT MINH (Product):
- [ ] Product, Category, ProductImage entities
- [ ] Review, Comment entities
- [ ] Wishlist, ViewedProduct entities

CÔNG QUÂN (Order):
- [ ] Cart, CartItem, Order, OrderItem entities
- [ ] Payment, Coupon entities
- [ ] OrderStatus enum

THANH NHÀN (Management):
- [ ] Shop, Vendor, AdminSettings entities
- [ ] Promotion, ShippingProvider entities
- [ ] Revenue, Statistics entities

→ MERGE CHUNG ngày 10/10 tối

**Ngày 11-12/10: REPOSITORY & SERVICE LAYER

**MỖI NGƯỜI LÀM PHẦN MÌNH:**

THIÊN HOÀNG:
- [ ] UserRepository, UserService
- [ ] AuthService, EmailService
- [ ] JWT utility class

TUYẾT MINH:
- [ ] ProductRepository, ProductService
- [ ] CategoryRepository, CategoryService
- [ ] ReviewRepository, ReviewService

CÔNG QUÂN:
- [ ] CartRepository, CartService
- [ ] OrderRepository, OrderService
- [ ] PaymentRepository, PaymentService

THANH NHÀN:
- [ ] ShopRepository, ShopService
- [ ] VendorRepository, VendorService
- [ ] AdminRepository, AdminService

→ MERGE ngày 12/10 tối

Ngày 13-15/10: CONTROLLER & BASIC UI

**PHÁT TRIỂN SONG SONG:**

THIÊN HOÀNG:
- [ ] AuthController (login, register, logout)
- [ ] OTPController (send, verify)
- [ ] Basic auth pages với Bootstrap

TUYẾT MINH:
- [ ] ProductController (list, detail, search)
- [ ] CategoryController
- [ ] Homepage, product list pages

CÔNG QUÂN:
- [ ] CartController (add, remove, update)
- [ ] Basic cart UI
- [ ] OrderController skeleton

THANH NHÀN:
- [ ] VendorController (register, dashboard)
- [ ] AdminController (dashboard, user management)
- [ ] Basic admin/vendor templates

→ MERGE & TEST ngày 15/10 cuối tuần

TUẦN 2: FEATURE COMPLETION (16/10 - 21/10)

Ngày 16-17/10: USER FEATURES & PAYMENT

**PHÂN CÔNG CÂN BẰNG:**

THIÊN HOÀNG:
- [ ] User profile management
- [ ] Address management (multiple addresses)
- [ ] Profile edit pages

TUYẾT MINH:
- [ ] Product review system (50+ chars validation)
- [ ] Comment system với image upload
- [ ] Product rating functionality

CÔNG QUÂN:
- [ ] Payment integration (VNPay)
- [ ] Order completion flow
- [ ] Payment success/failure handling

THANH NHÀN:
- [ ] Vendor product management (CRUD)
- [ ] Vendor order management
- [ ] Shop management interface

→ MERGE ngày 17/10 tối

Ngày 18-19/10: ADVANCED FEATURES

**TIẾP TỤC SONG SONG:**

THIÊN HOÀNG:
- [ ] WebSocket notifications
- [ ] Real-time order updates
- [ ] Security refinements

TUYẾT MINH:
- [ ] Advanced search & filtering
- [ ] Product pagination (20 items)
- [ ] Product sorting (new, popular, etc.)

CÔNG QUÂN:
- [ ] Order history với status tracking
- [ ] Order status management (6 trạng thái)
- [ ] Order detail pages

THANH NHÀN:
- [ ] Admin category management
- [ ] Shipping provider management
- [ ] System promotion management

→ MERGE ngày 19/10 tối

Ngày 20-21/10: INTEGRATION & TESTING

**CẢ NHÓM CÙNG TEST:**

THIÊN HOÀNG: Test auth flow end-to-end
TUYẾT MINH: Test product/review flow
CÔNG QUÂN: Test order/payment flow
THANH NHÀN: Test vendor/admin flow

→ FIX BUGS CRITICAL ngày 21/10

TUẦN 2.5: POLISHING (22/10 - 24/10)

Ngày 22/10: UI/UX IMPROVEMENT

**MỖI NGƯỜI HOÀN THIỆN PHẦN MÌNH:**

THIÊN HOÀNG: Responsive auth pages
TUYẾT MINH: Product UI refinement
CÔNG QUÂN: Order/payment UI polish
THANH NHÀN: Admin/vendor dashboard UI

→ Responsive design check

Ngày 23/10: FINAL TESTING & DOCUMENTATION

**TEST TOÀN BỘ HỆ THỐNG:**

THIÊN HOÀNG: 
- [ ] Security testing
- [ ] Auth documentation

TUYẾT MINH:
- [ ] Product feature testing
- [ ] User feature documentation

CÔNG QUÂN:
- [ ] Order/payment testing
- [ ] Payment flow documentation

THANH NHÀN:
- [ ] Vendor/admin testing
- [ ] Management documentation

→ FINAL BUG FIXES

Ngày 24/10: DEPLOYMENT READY

**CẢ NHÓM:**
- [ ] Final merge
- [ ] Database seeding
- [ ] Demo preparation
- [ ] Project submission
