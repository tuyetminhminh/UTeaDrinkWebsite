package net.codejava.utea.config;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.*;
import net.codejava.utea.catalog.entity.enums.Size;
import net.codejava.utea.catalog.repository.*;
import net.codejava.utea.chat.entity.Conversation;
import net.codejava.utea.chat.entity.Message;
import net.codejava.utea.chat.entity.enums.ConversationScope;
import net.codejava.utea.chat.repository.ConversationRepository;
import net.codejava.utea.chat.repository.MessageRepository;
import net.codejava.utea.common.entity.Address;
import net.codejava.utea.common.entity.Role;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.AddressRepository;
import net.codejava.utea.common.repository.RoleRepository;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.customer.entity.Coupon;
import net.codejava.utea.customer.entity.enums.CouponType;
import net.codejava.utea.customer.repository.CouponRepository;
import net.codejava.utea.customer.repository.SizeRepository;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopBanner;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.manager.repository.ShopBannerRepository;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.order.entity.Order;
import net.codejava.utea.order.entity.OrderItem;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderRepository;
import net.codejava.utea.promotion.entity.Promotion;
import net.codejava.utea.promotion.entity.Voucher;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.entity.enums.PromoType;
import net.codejava.utea.promotion.repository.PromotionRepository;
import net.codejava.utea.promotion.repository.VoucherRepository;
import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;
import net.codejava.utea.shipping.entity.ShipAssignment;
import net.codejava.utea.shipping.entity.ShippingProvider;
import net.codejava.utea.shipping.entity.ShipperProfile;
import net.codejava.utea.shipping.repository.ShipAssignmentRepository;
import net.codejava.utea.shipping.repository.ShipperProfileRepository;
import net.codejava.utea.shipping.repository.ShippingProviderRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final ShopRepository shopRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final ShopBannerRepository bannerRepo;
    private final ProductCategoryRepository categoryRepo;
    private final SizeRepository sizeRepo;
    private final ProductRepository productRepo;
    private final ToppingRepository toppingRepo;
    private final CouponRepository couponRepo;
    private final VoucherRepository voucherRepo;
    private final PromotionRepository promotionRepo;
    private final AddressRepository addressRepo;
    private final OrderRepository orderRepo;
    private final ReviewRepository reviewRepo;
    private final ShippingProviderRepository shippingProviderRepo;
    private final ShipperProfileRepository shipperProfileRepo;
    private final ShipAssignmentRepository shipAssignmentRepo;

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("=== BẮT ĐẦU KHỞI TẠO DỮ LIỆU MẪU ===");

        // 1. Roles & Users
        initRolesAndUsers();

        // 2. Shops
        Shop shop1 = initShops();
        
        // 2.1. Shop Managers (gán manager vào shop)
        initShopManagers(shop1);
        
        // 2.2. Shop Banners (banner cho trang chủ)
        initBanners(shop1);

        // 3. Product Categories
        initProductCategories();

        // 4. Sizes
        initSizes();

        // 5. Products với variants và images
        initProducts(shop1);

        // 6. Toppings
        initToppings(shop1);

        // 7. Coupons
        initCoupons();

        // 8. Vouchers
        initVouchers(shop1);

        // 9. Promotions
        initPromotions(shop1);

        // 10. Addresses
        initAddresses();

        // 11. Orders
        initOrders(shop1);

        // 12. Reviews
        initReviews();

        // 13. Shipping Providers
        initShippingProviders();

        // 14. Shipper Profiles
        initShipperProfiles();

        // 15. Ship Assignments (gán shipper vào đơn hàng)
        initShipAssignments(shop1);

        // 16. Cuộc trò chuyện mẫu giữa Manager và Customer
        initConversationsAndMessages();


        System.out.println("=== HOÀN THÀNH KHỞI TẠO DỮ LIỆU MẪU ===");
    }

    // ==================== 1. ROLES & USERS ====================
    private void initRolesAndUsers() {
        System.out.println("→ Khởi tạo Roles & Users...");
        
        ensureRole("ADMIN", "Quản trị");
        ensureRole("MANAGER", "Quản lý");
        ensureRole("SELLER", "Người bán");
        ensureRole("CUSTOMER", "Khách hàng");
        ensureRole("SHIPPER", "Tài xế");

        // Users
        ensureUserWithRole("admin@utea.local", "admin", "Admin Seed", "123456", "ACTIVE", "ADMIN");
        ensureUserWithRole("manager@utea.local", "manager", "Manager Seed", "123456", "ACTIVE", "MANAGER");
        ensureUserWithRole("seller@utea.local", "seller", "Seller Seed", "123456", "ACTIVE", "SELLER");
        ensureUserWithRole("customer@utea.local", "customer", "Nguyễn Văn A", "123456", "ACTIVE", "CUSTOMER");
        ensureUserWithRole("customer2@utea.local", "customer2", "Trần Thị B", "123456", "ACTIVE", "CUSTOMER");
        ensureUserWithRole("thanhnau25@gmail.com", "customer3", "Lê Văn C", "123456", "ACTIVE", "CUSTOMER");
        
        // Shippers
        ensureUserWithRole("shipper@utea.local", "shipper", "Phạm Văn Thành", "123456", "ACTIVE", "SHIPPER");
        ensureUserWithRole("shipper2@utea.local", "shipper2", "Lê Hoàng Nam", "123456", "ACTIVE", "SHIPPER");
        ensureUserWithRole("shipper3@utea.local", "shipper3", "Trần Minh Tuấn", "123456", "ACTIVE", "SHIPPER");
        ensureUserWithRole("shipper4@utea.local", "shipper4", "Nguyễn Thị Hoa", "123456", "ACTIVE", "SHIPPER");
        ensureUserWithRole("shipper5@utea.local", "shipper5", "Võ Đức Anh", "123456", "ACTIVE", "SHIPPER");
    }

    private Role ensureRole(String code, String name) {
        return roleRepo.findByCode(code)
                .orElseGet(() -> roleRepo.save(Role.builder().code(code).build()));
    }

    private void ensureUserWithRole(String email, String username, String fullName, 
                                    String rawPassword, String status, String roleCode) {
        Role role = ensureRole(roleCode, roleCode);
        
        // ✅ FIX: Check cả EMAIL và USERNAME để tránh duplicate key error
        User existingUser = userRepo.findByEmail(email).orElse(null);
        
        if (existingUser != null) {
            // User đã tồn tại theo email → chỉ cần update role nếu thiếu
            if (existingUser.getRoles() == null || !existingUser.getRoles().contains(role)) {
                existingUser.getRoles().add(role);
                userRepo.save(existingUser);
                System.out.println("  → Updated role for existing user: " + email);
            }
        } else {
            // Kiểm tra xem username đã tồn tại chưa (tránh duplicate key)
            User existingByUsername = userRepo.findByUsername(username).orElse(null);
            
            if (existingByUsername != null) {
                // Username đã tồn tại nhưng email khác → update email và role
                existingByUsername.setEmail(email);
                existingByUsername.setFullName(fullName);
                if (existingByUsername.getRoles() == null || !existingByUsername.getRoles().contains(role)) {
                    existingByUsername.getRoles().add(role);
                }
                userRepo.save(existingByUsername);
                System.out.println("  → Updated existing user (by username): " + username + " -> " + email);
            } else {
                // User hoàn toàn mới → tạo mới
                User u = User.builder()
                        .email(email)
                        .username(username)
                        .fullName(fullName)
                        .passwordHash(passwordEncoder.encode(rawPassword))
                        .status(status)
                        .roles(Set.of(role))
                        .build();
                userRepo.save(u);
                System.out.println("  → Created new user: " + email);
            }
        }
    }

    // ==================== 2. SHOPS ====================
    private Shop initShops() {
        System.out.println("→ Khởi tạo Shops...");
        
        return shopRepo.findByName("UTea Coffee & Tea")
                .orElseGet(() -> shopRepo.save(Shop.builder()
                        .name("UTea Coffee & Tea")
                        .address("01 Võ Văn Ngân, Thủ Đức, TP.HCM")
                        .phone("0901234567")
                        .status("OPEN")
                        .build()));
    }

    // ==================== 2.1. SHOP MANAGERS ====================
    private void initShopManagers(Shop shop) {
        System.out.println("→ Khởi tạo Shop Managers...");
        
        User manager = userRepo.findByEmail("manager@utea.local").orElse(null);
        if (manager == null) {
            System.out.println("  ⚠ WARNING: Manager user not found!");
            return;
        }
        
        System.out.println("  → Found manager user: " + manager.getEmail() + " (ID: " + manager.getId() + ")");
        
        if (!shopManagerRepo.existsByManager_Id(manager.getId())) {
            ShopManager shopManager = ShopManager.builder()
                    .shop(shop)
                    .manager(manager)
                    .build();
            shopManagerRepo.save(shopManager);
            System.out.println("  ✓ Đã gán Manager vào shop: " + shop.getName() + " (ShopManager ID: " + shopManager.getId() + ")");
        } else {
            System.out.println("  → Manager đã được gán vào shop rồi");
        }
    }

    // ==================== 2.2. SHOP BANNERS ====================
    private void initBanners(Shop shop) {
        System.out.println("→ Khởi tạo Shop Banners...");
        
        if (bannerRepo.findByShopIdOrderBySortOrderAsc(shop.getId()).isEmpty()) {
            createBanner(shop, "Trà Sữa Đặc Biệt Mùa Hè", 
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760047323/1-tuan-nen-uong-bao-nhieu-tra-sua-1000x690_slbfnn.jpg",
                "/customer/menu", 0, true);
                
            createBanner(shop, "Combo Sinh Nhật Ưu Đãi 20%",
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760047294/tra-sua-da-lat-topbanner_sdeac5.jpg",
                "/customer/menu?category=combo", 1, true);
                
            createBanner(shop, "Cà Phê Sáng - Thức Dậy Năng Lượng",
                "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=1400&h=500&fit=crop",
                "/customer/menu?category=coffee", 2, true);
                
            System.out.println("  ✓ Đã tạo 3 banners mẫu cho shop");
        } else {
            System.out.println("  → Banners đã tồn tại");
        }
    }
    
    private void createBanner(Shop shop, String title, String imageUrl, String link, int sortOrder, boolean active) {
        ShopBanner banner = ShopBanner.builder()
                .shop(shop)
                .title(title)
                .imageUrl(imageUrl)
                .link(link)
                .sortOrder(sortOrder)
                .active(active)
                .build();
        bannerRepo.save(banner);
    }

    // ==================== 3. PRODUCT CATEGORIES ====================
    private void initProductCategories() {
        System.out.println("→ Khởi tạo Product Categories...");
        
        ensureCategory("Trà sữa", "Các loại trà sữa thơm ngon");
        ensureCategory("Cà phê", "Các loại cà phê rang xay");
        ensureCategory("Sinh tố", "Sinh tố trái cây tươi");
        ensureCategory("Trà trái cây", "Trà trái cây thanh mát");
        ensureCategory("Đá xay", "Các loại đá xay mát lạnh");
    }

    private ProductCategory ensureCategory(String name, String description) {
        return categoryRepo.findByName(name)
                .orElseGet(() -> categoryRepo.save(ProductCategory.builder()
                        .name(name)
                        .description(description)
                        .status("ACTIVE")
                        .build()));
    }

    // ==================== 4. SIZES ====================
    private void initSizes() {
        System.out.println("→ Khởi tạo Sizes...");
        
        ensureSize("S", "Nhỏ", new BigDecimal("0"));
        ensureSize("M", "Vừa", new BigDecimal("5000"));
        ensureSize("L", "Lớn", new BigDecimal("10000"));
    }

    private net.codejava.utea.customer.entity.Size ensureSize(String code, String name, BigDecimal extraPrice) {
        return sizeRepo.findByCode(code)
                .orElseGet(() -> sizeRepo.save(
                        net.codejava.utea.customer.entity.Size.builder()
                                .code(code)
                                .name(name)
                                .extraPrice(extraPrice)
                                .status("ACTIVE")
                                .build()));
    }

    // ==================== 5. PRODUCTS ====================
    private void initProducts(Shop shop) {
        System.out.println("→ Khởi tạo Products với Variants và Images...");
        
        ProductCategory traSua = categoryRepo.findByName("Trà sữa").orElse(null);
        ProductCategory caPhe = categoryRepo.findByName("Cà phê").orElse(null);
        ProductCategory nuocEp = categoryRepo.findByName("Nước ép").orElse(null);

        // ============ TRÀ SỮA ============
        createProduct(shop, traSua, 
                "Trà sữa truyền thống", 
                "Hương vị trà sữa truyền thống đi kèm trân châu đường đen", 
                new BigDecimal("25000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1759979084/t%E1%BA%A3i_xu%E1%BB%91ng_1_ha1t8k.jpg",
                "trasuatruyenthong_wk6cgc");

        createProduct(shop, traSua, 
                "Trà matcha sữa", 
                "Matcha đậm vị, hòa quyện sữa tươi ngọt nhẹ.", 
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1759979120/tra-sua-matcha_anwkom.jpg",
                "tra-sua-matcha_anwkom");

        createProduct(shop, traSua, 
                "Trà sữa khoai môn", 
                "Vị khoai môn, mang lại cảm giác ngon miệng", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013931/trasua7_zt08kb.png",
                "trasua7_zt08kb");

        createProduct(shop, traSua, 
                "Trà sữa Olong mochi", 
                "Vị trà sữa Olong, có mochi ăn kèm làm tăng vị giác", 
                new BigDecimal("39000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013931/trasua6_eh482a.png",
                "trasua6_eh482a");

        createProduct(shop, traSua, 
                "Chocolate latte", 
                "Hương latte chocolate thơm ngon", 
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013929/trasua4_ezijz7.png",
                "trasua4_ezijz7");

        createProduct(shop, traSua, 
                "Trà sữa dâu", 
                "Hương vị dâu ngọt ngào đi kèm chân châu dâu làm bùng nổ vị giác", 
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013926/trasua3_eopkiy.png",
                "trasua3_eopkiy");

        createProduct(shop, traSua, 
                "Trà sữa trân châu hoàng kim", 
                "Vị ngon khó cưỡng đi kèm trân châu hoàng kim giòn tan", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013925/trasua2_ainkeb.png",
                "trasua2_ainkeb");

        createProduct(shop, traSua, 
                "Trà sữa okinawa kim cương đen", 
                "Mang hương vị trà okinawa kết hợp trân châu đen giòn", 
                new BigDecimal("39000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013924/trasua1_mtufkn.png",
                "trasua1_mtufkn");

        createProduct(shop, traSua, 
                "Matcha latte", 
                "Hương matcha latte ngọt dịu kết hợp với sữa gấu tạo hương vị khó tả", 
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013914/matcha_latte_dfuacy.png",
                "matcha_latte_dfuacy");

        createProduct(shop, traSua, 
                "Trà sữa bạc hà", 
                "Trà sữa mang hương vị ngọt ngào kết hợp vị thơm của bạc hà tạo nên hương vị tuyệt đỉnh", 
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013915/daxay1_m2cmdw.png",
                "daxay1_m2cmdw");

        createProduct(shop, traSua, 
                "Bánh cầu vồng", 
                "Thiết kế dễ thương, mỗi màu cầu vồng là một hương vị làm cho bánh có vị độc đáo và ngon miệng", 
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042330/banh_cau_vong_jlqx1n.jpg",
                "banh_cau_vong_jlqx1n");

        // ============ CÀ PHÊ & BÁNH NGỌT ============
        createProduct(shop, caPhe, 
                "Cà phê sữa đá", 
                "Cà phê phin truyền thống pha cùng sữa đặc thơm béo.", 
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1759979300/ca_phe_sua_da_gxhjg7.jpg",
                "ca_phe_sua_da_gxhjg7");

        createProduct(shop, caPhe, 
                "Bánh sanwid trứng chảy", 
                "Vỏ bánh mềm, trứng chảy làm bùng nổ vị giác.", 
                new BigDecimal("49000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042371/sandwich_mhqntr.png",
                "sandwich_mhqntr");

        createProduct(shop, caPhe, 
                "Bánh sừng bò", 
                "Loại bánh nổi tiếng từ nước Pháp, vỏ bánh giòn, nhân chery bùng nổ vị giác.", 
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042364/roissant_giam_bong_c3eaxh.png",
                "roissant_giam_bong_c3eaxh");

        createProduct(shop, caPhe, 
                "Bánh mochi nướng", 
                "Loại bánh đến từ Nhật Bản, vỏ bánh mềm, nhân khoai môn tan chảy.", 
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042357/mochi_nuong_lcgjow.png",
                "mochi_nuong_lcgjow");

        createProduct(shop, caPhe, 
                "Bánh donut", 
                "Vỏ bánh ngon, được phủ một lớp kem dâu ngon lành", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042350/donut_socola_pucmiy.png",
                "donut_socola_pucmiy");

        createProduct(shop, caPhe, 
                "Bánh sùng bò socola", 
                "Vỏ bánh giòn, hương socola bùng nổ vị giác.", 
                new BigDecimal("39000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042334/banh_croissant_vsmahb.png",
                "banh_croissant_vsmahb");

        createProduct(shop, caPhe, 
                "Bánh kem oreo", 
                "Bánh kem ngon, bơ kem béo ngậy", 
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042333/banh_kem_oreo_r5k9y2.png",
                "banh_kem_oreo_r5k9y2");

        createProduct(shop, caPhe, 
                "Bánh pudding Carameo", 
                "Hương carameo ngọt ngào, bánh mềm.", 
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042334/caramen_wtbkde.png",
                "caramen_wtbkde");

        createProduct(shop, caPhe, 
                "Bánh su kem ", 
                "Vỏ bánh giòn, nhân kem ngọt ngào bùng nổ vị giác", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042334/banh_su_kem_nea1hj.png",
                "banh_su_kem_nea1hj");

        createProduct(shop, caPhe, 
                "Bánh tiramisu", 
                "Bánh mềm, nhân kem kết hợp trứng làm gia tăng hương vị", 
                new BigDecimal("39000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042332/banh_tiramisu_dgv2zi.png",
                "banh_tiramisu_dgv2zi");

        // ============ NƯỚC ÉP & TRÀ TRÁI CÂY ============
        createProduct(shop, nuocEp, 
                "Nước ép cam", 
                "Nước ép cam tươi nguyên chất, bổ sung vitamin C.", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1759979083/Nuoc-ep-Cam-Tuoi-300x300_qz1l3d.png",
                "Nuoc-ep-Cam-Tuoi-300x300_qz1l3d");

        createProduct(shop, nuocEp, 
                "Trà chanh nhiệt đới", 
                "Hương chanh mát lạnh, đánh tan cơn nóng mùa hè", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013925/tra8_kqmw06.png",
                "tra8_kqmw06");

        createProduct(shop, nuocEp, 
                "Nước chery nhiệt đới", 
                "Hương chery mát lạnh, bùng nổ vị giác ngày hè", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013922/Tra9_pmuarx.png",
                "Tra9_pmuarx");

        createProduct(shop, nuocEp, 
                "NƯớc ép xoài", 
                "Hương thơm của xoài, bùng nổ vị giác", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013921/tra7_dqcfgr.png",
                "tra7_dqcfgr");

        createProduct(shop, nuocEp, 
                "Nước ép dâu tằm", 
                "Vị ngọt của dâu tằm, đánh tan cái nóng mùa hè", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013919/tra6_n5ldnj.png",
                "tra6_n5ldnj");

        createProduct(shop, nuocEp, 
                "Nước ép cam tươi", 
                "Nước ép cam tươi nguyên chất, bổ sung vitamin C.", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013917/tra2_fxdxpc.png",
                "tra2_fxdxpc");

        createProduct(shop, nuocEp, 
                "Nước ép nhiệt đới", 
                "Mang lại cảm giác sảng khoái, đánh tan cơn nóng Sài Gòn", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013917/tra5_ekeiow.png",
                "tra5_ekeiow");

        createProduct(shop, nuocEp, 
                "Trà tắc", 
                "Thức uống phổ biến ngày hè, vị ngon khó cưỡng", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013917/tra4_vvnmry.png",
                "tra4_vvnmry");

        createProduct(shop, nuocEp, 
                "Hồng trà utea", 
                "Thức uống phổ biến ngày hè, vị ngon khó cưỡng", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013916/tra1_j3r8fr.png",
                "tra1_j3r8fr");

        createProduct(shop, nuocEp, 
                "Trà đào cam sả", 
                "Thức uống phổ biến ngày hè, vị ngon khó cưỡng", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760045845/Tr%C3%A0-%C4%90%C3%A0o-Cam-s%E1%BA%A3_rmwgcn.jpg",
                "Trà-Đào-Cam-sả_rmwgcn");

        createProduct(shop, nuocEp, 
                "Trà xanh", 
                "Hương vị ngọt thanh, mát mẻ của trà xanh", 
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013917/tra4_vvnmry.png",
                "Matcha-Tea-Latte-2_fwjetm");
    }

    private void createProduct(Shop shop, ProductCategory category, String name, 
                              String description, BigDecimal basePrice, String imageUrl, String publicId) {
        if (productRepo.findByNameAndShopId(name, shop.getId()).isEmpty()) {
            Product product = Product.builder()
                    .shop(shop)
                    .category(category)
                    .name(name)
                    .description(description)
                    .basePrice(basePrice)
                    .soldCount(0)
                    .ratingAvg(new BigDecimal("4.5"))
                    .status("AVAILABLE")
                    .build();
            product = productRepo.save(product);

            // Thêm ảnh
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .url(imageUrl)
                    .publicId(publicId)
                    .sortOrder(0)
                    .build();
            product.getImages().add(image);

            // Thêm variants (S, M, L)
            createVariant(product, Size.S, basePrice, 350);
            createVariant(product, Size.M, basePrice.add(new BigDecimal("5000")), 500);
            createVariant(product, Size.L, basePrice.add(new BigDecimal("10000")), 700);

            productRepo.save(product);
        }
    }

    private void createVariant(Product product, Size size, BigDecimal price, int volumeMl) {
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .size(size)
                .price(price)
                .volumeMl(volumeMl)
                .build();
        product.getVariants().add(variant);
    }

    // ==================== 6. TOPPINGS ====================
    private void initToppings(Shop shop) {
        System.out.println("→ Khởi tạo Toppings...");
        
        createTopping(shop, "Trân châu đen", new BigDecimal("5000"));
        createTopping(shop, "Trân châu trắng", new BigDecimal("5000"));
        createTopping(shop, "Thạch dừa", new BigDecimal("5000"));
        createTopping(shop, "Thạch rau câu", new BigDecimal("5000"));
        createTopping(shop, "Pudding", new BigDecimal("8000"));
        createTopping(shop, "Trứng cút", new BigDecimal("7000"));
        createTopping(shop, "Kem cheese", new BigDecimal("10000"));
    }

    private void createTopping(Shop shop, String name, BigDecimal price) {
        if (toppingRepo.findByShopId(shop.getId()).stream()
                .noneMatch(t -> t.getName().equals(name))) {
            toppingRepo.save(Topping.builder()
                    .shop(shop)
                    .name(name)
                    .price(price)
                    .status("ACTIVE")
                    .build());
        }
    }

    // ==================== 7. COUPONS ====================
    private void initCoupons() {
        System.out.println("→ Khởi tạo Coupons...");
        
        createCoupon("WELCOME10", "Giảm 10% cho đơn hàng đầu tiên", 
                CouponType.ORDER_PERCENT, new BigDecimal("10"), 
                new BigDecimal("50000"), new BigDecimal("50000"), 100);

        createCoupon("SUMMER20", "Giảm 20k cho đơn từ 100k", 
                CouponType.ORDER_AMOUNT, new BigDecimal("20000"), 
                new BigDecimal("100000"), null, 200);

        createCoupon("FREESHIP", "Miễn phí vận chuyển", 
                CouponType.SHIPPING_AMOUNT, new BigDecimal("15000"), 
                new BigDecimal("80000"), null, 150);
    }

    private void createCoupon(String code, String title, CouponType type, 
                             BigDecimal discountValue, BigDecimal minOrderValue, 
                             BigDecimal maxDiscount, Integer usageLimit) {
        couponRepo.findByCodeActiveNow(code).orElseGet(() -> 
            couponRepo.save(Coupon.builder()
                    .code(code)
                    .title(title)
                    .type(type)
                    .discountValue(discountValue)
                    .minOrderValue(minOrderValue)
                    .maxDiscount(maxDiscount)
                    .usageLimit(usageLimit)
                    .usedCount(0)
                    .startDate(LocalDateTime.now().minusDays(1))
                    .endDate(LocalDateTime.now().plusMonths(3))
                    .active(true)
                    .build())
        );
    }

    // ==================== 8. VOUCHERS ====================
    private void initVouchers(Shop shop) {
        System.out.println("→ Khởi tạo Vouchers...");
        
        createVoucher("GLOBAL50", PromoScope.GLOBAL, null, 50, 200000);
        createVoucher("SHOP30", PromoScope.SHOP, shop, 30, 150000);
    }

    private void createVoucher(String code, PromoScope scope, Shop shop,
                               int percentOff, long minTotal) {
        voucherRepo.findByCodeActiveNow(code, LocalDateTime.now()).orElseGet(() ->
                voucherRepo.save(Voucher.builder()
                        .code(code)
                        .scope(scope)
                        .shop(shop)
                        .ruleJson(String.format("{\"percentOff\":%d,\"minTotal\":%d}", percentOff, minTotal))
                        .forFirstOrder(false)
                        .forBirthday(false)
                        .activeFrom(LocalDateTime.now().minusDays(1))
                        .activeTo(LocalDateTime.now().plusMonths(2))
                        .status("ACTIVE")
                        .usageLimit(100)
                        .usedCount(0)
                        .build())
        );
    }


    // ==================== 9. PROMOTIONS ====================
    private void initPromotions(Shop shop) {
        System.out.println("→ Khởi tạo Promotions...");
        
        createPromotion("Khuyến mãi toàn hệ thống", PromoScope.GLOBAL, null, 
                PromoType.PERCENT, 15);
        createPromotion("Khuyến mãi Shop", PromoScope.SHOP, shop, 
                PromoType.PERCENT, 10);
    }

    private void createPromotion(String title, PromoScope scope, Shop shop, 
                                PromoType type, int percentOff) {
        if (promotionRepo.findByScope(scope).stream()
                .noneMatch(p -> p.getTitle().equals(title))) {
            promotionRepo.save(Promotion.builder()
                    .scope(scope)
                    .shop(shop)
                    .type(type)
                    .ruleJson(String.format("{\"percentOff\":%d}", percentOff))
                    .title(title)
                    .description("Chương trình khuyến mãi đặc biệt")
                    .activeFrom(LocalDateTime.now().minusDays(1))
                    .activeTo(LocalDateTime.now().plusMonths(1))
                    .status("ACTIVE")
                    .build());
        }
    }

    // ==================== 10. ADDRESSES ====================
    private void initAddresses() {
        System.out.println("→ Khởi tạo Addresses...");
        
        User customer = userRepo.findByEmail("customer@utea.local").orElse(null);
        if (customer != null && addressRepo.findByUserId(customer.getId()).isEmpty()) {
            addressRepo.save(Address.builder()
                    .user(customer)
                    .receiverName("Nguyễn Văn A")
                    .phone("0912345678")
                    .line("123 Đường ABC")
                    .ward("Phường Linh Trung")
                    .district("Thủ Đức")
                    .province("TP. Hồ Chí Minh")
                    .isDefault(true)
                    .build());
        }

        User customer2 = userRepo.findByEmail("customer2@utea.local").orElse(null);
        if (customer2 != null && addressRepo.findByUserId(customer2.getId()).isEmpty()) {
            addressRepo.save(Address.builder()
                    .user(customer2)
                    .receiverName("Trần Thị B")
                    .phone("0923456789")
                    .line("456 Đường XYZ")
                    .ward("Phường Linh Chiểu")
                    .district("Thủ Đức")
                    .province("TP. Hồ Chí Minh")
                    .isDefault(true)
                    .build());
        }
    }

    // ==================== 11. ORDERS ====================
    private void initOrders(Shop shop) {
        System.out.println("→ Khởi tạo Orders...");
        
        User customer = userRepo.findByEmail("customer@utea.local").orElse(null);
        User customer2 = userRepo.findByEmail("customer2@utea.local").orElse(null);
        User customer3 = userRepo.findByEmail("customer3@utea.local").orElse(null);
        
        Address address1 = customer != null ? addressRepo.findByUserId(customer.getId()).stream().findFirst().orElse(null) : null;
        Address address2 = customer2 != null ? addressRepo.findByUserId(customer2.getId()).stream().findFirst().orElse(null) : null;
        
        java.util.List<Product> products = productRepo.findAll();
        
        if (customer != null && address1 != null && !products.isEmpty()) {
            // 2 đơn NEW - Mới tạo, chưa xác nhận
            createSampleOrder(shop, customer, address1, products.get(0), 
                    "ORD-2025-000011", OrderStatus.NEW, LocalDateTime.now().minusMinutes(30),
                    2, new BigDecimal("35000"), new BigDecimal("75000"), new BigDecimal("90000"));
            
            if (products.size() > 1) {
                createSampleOrder(shop, customer2, address2, products.get(1), 
                        "ORD-2025-000012", OrderStatus.NEW, LocalDateTime.now().minusMinutes(15),
                        1, new BigDecimal("40000"), new BigDecimal("45000"), new BigDecimal("60000"));
            }
            
            // 3 đơn PREPARING - Chờ shipper tự nhận
            createSampleOrder(shop, customer, address1, products.get(0), 
                    "ORD-2025-000001", OrderStatus.PREPARING, LocalDateTime.now().minusHours(2),
                    2, new BigDecimal("30000"), new BigDecimal("65000"), new BigDecimal("80000"));
            
            if (products.size() > 1) {
                createSampleOrder(shop, customer2, address2, products.get(1), 
                        "ORD-2025-000002", OrderStatus.PREPARING, LocalDateTime.now().minusHours(3),
                        1, new BigDecimal("35000"), new BigDecimal("40000"), new BigDecimal("55000"));
            }
            
            if (products.size() > 2) {
                createSampleOrder(shop, customer3, address1, products.get(2), 
                        "ORD-2025-000003", OrderStatus.PREPARING, LocalDateTime.now().minusHours(4),
                        3, new BigDecimal("32000"), new BigDecimal("96000"), new BigDecimal("111000"));
            }
            
            // 1 đơn DELIVERING - Đang giao
            if (products.size() > 3) {
                createSampleOrder(shop, customer, address1, products.get(3), 
                        "ORD-2025-000004", OrderStatus.DELIVERING, LocalDateTime.now().minusHours(5),
                        2, new BigDecimal("35000"), new BigDecimal("75000"), new BigDecimal("90000"));
            }
            
            // 6 đơn DELIVERED - Đã giao
            if (products.size() > 4) {
                createSampleOrder(shop, customer2, address2, products.get(4), 
                        "ORD-2025-000005", OrderStatus.DELIVERED, LocalDateTime.now().minusHours(10),
                        1, new BigDecimal("40000"), new BigDecimal("45000"), new BigDecimal("60000"));
            }
            
            if (products.size() > 5) {
                createSampleOrder(shop, customer, address1, products.get(5), 
                        "ORD-2025-000006", OrderStatus.DELIVERED, LocalDateTime.now().minusDays(1).minusHours(3),
                        2, new BigDecimal("38000"), new BigDecimal("81000"), new BigDecimal("96000"));
            }
            
            if (products.size() > 6) {
                createSampleOrder(shop, customer3, address1, products.get(6), 
                        "ORD-2025-000007", OrderStatus.DELIVERED, LocalDateTime.now().minusDays(2).minusHours(5),
                        1, new BigDecimal("42000"), new BigDecimal("47000"), new BigDecimal("62000"));
            }
            
            if (products.size() > 7) {
                createSampleOrder(shop, customer2, address2, products.get(7), 
                        "ORD-2025-000008", OrderStatus.DELIVERED, LocalDateTime.now().minusDays(3).minusHours(2),
                        3, new BigDecimal("33000"), new BigDecimal("99000"), new BigDecimal("114000"));
            }
            
            if (products.size() > 8) {
                createSampleOrder(shop, customer, address1, products.get(8), 
                        "ORD-2025-000009", OrderStatus.DELIVERED, LocalDateTime.now().minusDays(4).minusHours(6),
                        2, new BigDecimal("36000"), new BigDecimal("77000"), new BigDecimal("92000"));
            }
            
            if (products.size() > 9) {
                createSampleOrder(shop, customer3, address1, products.get(9), 
                        "ORD-2025-000010", OrderStatus.DELIVERED, LocalDateTime.now().minusDays(5).minusHours(4),
                        1, new BigDecimal("45000"), new BigDecimal("50000"), new BigDecimal("65000"));
            }
        }
    }
    
    private void createSampleOrder(Shop shop, User customer, Address address, Product product,
                                   String orderCode, OrderStatus status, LocalDateTime createdAt,
                                   int quantity, BigDecimal unitPrice, BigDecimal subtotal, BigDecimal total) {
        if (orderRepo.findByOrderCode(orderCode).isEmpty()) {
            Order order = Order.builder()
                    .orderCode(orderCode)
                    .user(customer)
                    .shop(shop)
                    .status(status)
                    .shippingAddress(address)
                    .subtotal(subtotal)
                    .shippingFee(new BigDecimal("15000"))
                    .discount(new BigDecimal("0"))
                    .total(total)
                    .createdAt(createdAt)
                    .build();

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .lineTotal(subtotal)
                    .toppingsJson("[{\"name\":\"Trân châu đen\",\"price\":5000}]")
                    .note(status == OrderStatus.CANCELED ? "Khách hủy đơn" : "")
                    .build();
            
            order.getItems().add(item);
            orderRepo.save(order);
        }
    }

    // ==================== 12. REVIEWS ====================
    // ==================== 12. REVIEWS ====================
    private void initReviews() {
        System.out.println("→ Khởi tạo Reviews...");

        User customer = userRepo.findByEmail("customer@utea.local").orElse(null);
        Product product = productRepo.findAll().stream().findFirst().orElse(null);

        if (customer != null && product != null) {
            boolean hasReview = reviewRepo.findAll().stream()
                    .anyMatch(r -> r.getUser() != null && r.getUser().getId().equals(customer.getId()));

            if (!hasReview) {
                reviewRepo.save(Review.builder()
                        .user(customer)
                        .product(product)
                        .rating(5)
                        .content("Trà sữa rất ngon, sẽ mua lại nhiều lần nữa!")
                        .status(ReviewStatus.APPROVED)
                        .helpfulCount(10)
                        .build());
            }
        }

        User customer2 = userRepo.findByEmail("customer2@utea.local").orElse(null);
        if (customer2 != null && product != null) {
            boolean hasReview2 = reviewRepo.findAll().stream()
                    .anyMatch(r -> r.getUser() != null && r.getUser().getId().equals(customer2.getId()));

            if (!hasReview2) {
                reviewRepo.save(Review.builder()
                        .user(customer2)
                        .product(product)
                        .rating(4)
                        .content("Sản phẩm tốt, đóng gói cẩn thận, giao hàng nhanh")
                        .status(ReviewStatus.APPROVED)
                        .helpfulCount(5)
                        .build());
            }
        }
    }


    // ==================== 13. SHIPPING PROVIDERS ====================
    private void initShippingProviders() {
        System.out.println("→ Khởi tạo Shipping Providers...");
        
        createShippingProvider("Giao hàng nhanh", new BigDecimal("15000"));
        createShippingProvider("Giao hàng tiết kiệm", new BigDecimal("12000"));
        createShippingProvider("J&T Express", new BigDecimal("18000"));
    }

    private void createShippingProvider(String name, BigDecimal baseFee) {
        shippingProviderRepo.findByName(name).orElseGet(() ->
            shippingProviderRepo.save(ShippingProvider.builder()
                    .name(name)
                    .baseFee(baseFee)
                    .regionRulesJson("{\"distanceSteps\":[]}")
                    .status("ACTIVE")
                    .build())
        );
    }

    // ==================== 14. SHIPPER PROFILES ====================
    private void initShipperProfiles() {
        System.out.println("→ Khởi tạo Shipper Profiles...");
        
        createShipperProfile("shipper@utea.local", "bike", "79A1-12345", "Tài xế có kinh nghiệm 5 năm");
        createShipperProfile("shipper2@utea.local", "bike", "79C1-67890", "Chuyên giao hàng khu vực Thủ Đức");
        createShipperProfile("shipper3@utea.local", "car", "51B1-23456", "Có xe ô tô, giao hàng xa");
        createShipperProfile("shipper4@utea.local", "bike", "79B2-34567", "Giao hàng nhanh, thân thiện");
        createShipperProfile("shipper5@utea.local", "bike", "79D1-45678", "Tài xế mới, nhiệt tình");
    }

    private void createShipperProfile(String email, String vehicleType, 
                                     String licenseNumber, String note) {
        User shipper = userRepo.findByEmail(email).orElse(null);
        if (shipper != null && !shipperProfileRepo.existsByUserId(shipper.getId())) {
            ShipperProfile profile = new ShipperProfile();
            profile.setUser(shipper);
            profile.setVehicleType(vehicleType);
            profile.setLicenseNumber(licenseNumber);
            profile.setNote(note);
            shipperProfileRepo.save(profile);
        }
    }

    // ==================== 15. SHIP ASSIGNMENTS ====================
    private void initShipAssignments(Shop shop) {
        System.out.println("→ Khởi tạo Ship Assignments (gán shipper vào đơn hàng)...");
        
        User shipper1 = userRepo.findByEmail("shipper@utea.local").orElse(null);
        User shipper2 = userRepo.findByEmail("shipper2@utea.local").orElse(null);
        User shipper3 = userRepo.findByEmail("shipper3@utea.local").orElse(null);
        User shipper4 = userRepo.findByEmail("shipper4@utea.local").orElse(null);
        User shipper5 = userRepo.findByEmail("shipper5@utea.local").orElse(null);
        
        if (shipper1 == null || shipper2 == null || shipper3 == null) {
            System.out.println("  ⚠ WARNING: Không tìm thấy shipper users!");
            return;
        }
        
        // ===== GÁN SHIPPER CHO 1 ĐƠN DELIVERING =====
        // Order 4: DELIVERING (hôm nay, 5h trước)
        assignShipperToOrder("ORD-2025-000004", shipper1, "DELIVERING", 
                "Đang giao hàng khu vực Quận 1", LocalDateTime.now().minusHours(5));
        
        // ===== GÁN SHIPPER CHO 6 ĐƠN DELIVERED =====
        // Order 5: DELIVERED (hôm nay, 10h trước)
        assignShipperToOrder("ORD-2025-000005", shipper2, "DELIVERED", 
                "Đã giao hàng thành công", 
                LocalDateTime.now().minusHours(10), 
                LocalDateTime.now().minusHours(9).minusMinutes(30));
        
        // Order 6: DELIVERED (1 ngày trước)
        assignShipperToOrder("ORD-2025-000006", shipper3, "DELIVERED", 
                "Đã giao hàng thành công", 
                LocalDateTime.now().minusDays(1).minusHours(3), 
                LocalDateTime.now().minusDays(1).minusHours(2));
        
        // Order 7: DELIVERED (2 ngày trước)
        assignShipperToOrder("ORD-2025-000007", shipper4 != null ? shipper4 : shipper1, "DELIVERED", 
                "Đã giao hàng thành công", 
                LocalDateTime.now().minusDays(2).minusHours(5), 
                LocalDateTime.now().minusDays(2).minusHours(4));
        
        // Order 8: DELIVERED (3 ngày trước)
        assignShipperToOrder("ORD-2025-000008", shipper5 != null ? shipper5 : shipper2, "DELIVERED", 
                "Đã giao hàng thành công", 
                LocalDateTime.now().minusDays(3).minusHours(2), 
                LocalDateTime.now().minusDays(3).minusHours(1));
        
        // Order 9: DELIVERED (4 ngày trước)
        assignShipperToOrder("ORD-2025-000009", shipper1, "DELIVERED", 
                "Đã giao hàng thành công", 
                LocalDateTime.now().minusDays(4).minusHours(6), 
                LocalDateTime.now().minusDays(4).minusHours(5));
        
        // Order 10: DELIVERED (5 ngày trước)
        assignShipperToOrder("ORD-2025-000010", shipper2, "DELIVERED", 
                "Đã giao hàng thành công", 
                LocalDateTime.now().minusDays(5).minusHours(4), 
                LocalDateTime.now().minusDays(5).minusHours(3));
        
        System.out.println("  ✓ Đã gán shipper cho 1 đơn DELIVERING và 6 đơn DELIVERED");
    }
    
    private void assignShipperToOrder(String orderCode, User shipper, String status, 
                                     String note, LocalDateTime assignedAt) {
        assignShipperToOrder(orderCode, shipper, status, note, assignedAt, null);
    }
    
    private void assignShipperToOrder(String orderCode, User shipper, String status, 
                                     String note, LocalDateTime assignedAt, LocalDateTime deliveredAt) {
        Order order = orderRepo.findByOrderCode(orderCode).orElse(null);
        
        if (order != null && shipAssignmentRepo.findByOrderId(order.getId()).isEmpty()) {
            ShipAssignment assignment = ShipAssignment.builder()
                    .order(order)
                    .shipper(shipper)
                    .status(status)
                    .note(note)
                    .assignedAt(assignedAt)
                    .pickedUpAt(assignedAt != null ? assignedAt.plusMinutes(30) : null)
                    .deliveredAt(deliveredAt)
                    .build();
            
            shipAssignmentRepo.save(assignment);
            System.out.println("  → Đã gán shipper " + shipper.getFullName() + " cho đơn " + orderCode + " (" + status + ")");
        }
    }

    // ==================== 16. CHAT ====================
    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    // ==================== 16. CONVERSATIONS & MESSAGES ====================
    // ==================== 16. CONVERSATIONS & MESSAGES ====================
    private void initConversationsAndMessages() {
        System.out.println("→ Khởi tạo Conversations & Messages...");

        User manager = userRepo.findByEmail("manager@utea.local").orElse(null);
        User customer = userRepo.findByEmail("customer@utea.local").orElse(null);
        User customer2 = userRepo.findByEmail("customer2@utea.local").orElse(null);

        if (manager == null || customer == null) {
            System.out.println("⚠ Không tìm thấy user cần thiết để tạo cuộc trò chuyện mẫu.");
            return;
        }

        // 1️⃣ Cuộc trò chuyện giữa Manager và Customer 1
        createSampleConversation(manager, customer, ConversationScope.SYSTEM,
                new String[]{
                        "Chào bạn, mình cần hỗ trợ đặt hàng online!",
                        "Chào bạn, mình là quản lý UTea, mình có thể giúp gì?",
                        "Mình muốn hỏi về khuyến mãi cho khách hàng mới.",
                        "Dạ, hiện tại bạn có thể dùng mã WELCOME10 để giảm 10% cho đơn đầu tiên nhé.",
                        "Cảm ơn bạn nhiều, mình đặt thử nhé!"
                });

        // 2️⃣ Cuộc trò chuyện giữa Manager và Customer 2
        createSampleConversation(manager, customer2, ConversationScope.SYSTEM,
                new String[]{
                        "Trà sữa matcha có còn hàng không bạn?",
                        "Dạ có ạ, shop vẫn còn đủ size S, M, L nhé!",
                        "Mình muốn order size L và thêm topping pudding.",
                        "Ok ạ, mình xác nhận đơn giúp bạn ngay nhé!"
                });
    }

    private void createSampleConversation(User manager, User customer, ConversationScope scope, String[] messages) {
        Conversation conv = conversationRepo.findByCustomerAndAdminAndShopAndScope(customer, manager, null, scope)
                .orElseGet(() -> {
                    Conversation c = Conversation.builder()
                            .admin(manager)
                            .customer(customer)
                            .scope(scope)
                            .createdAt(LocalDateTime.now().minusDays(1))
                            .lastMessageAt(LocalDateTime.now())
                            .build();
                    return conversationRepo.save(c);
                });

        // Nếu đã có tin nhắn, bỏ qua để tránh trùng
        if (messageRepo.findTop50ByConversation_IdOrderBySentAtDesc(conv.getId()).isEmpty()) {
            System.out.println("  → Tạo hội thoại giữa " + customer.getFullName() + " và " + manager.getFullName());

            boolean fromCustomer = true;
            for (String content : messages) {
                Message msg = Message.builder()
                        .conversation(conv)
                        .sender(fromCustomer ? customer : manager)
                        .content(content)
                        .sentAt(LocalDateTime.now().minusMinutes((long) (Math.random() * 60)))
                        .read(false)
                        .build();
                messageRepo.save(msg);
                fromCustomer = !fromCustomer; // luân phiên
            }

            conv.setLastMessageAt(LocalDateTime.now());
            conversationRepo.save(conv);
            System.out.println("  ✓ Đã tạo " + messages.length + " tin nhắn cho cuộc trò chuyện mẫu");
        } else {
            System.out.println("  → Cuộc trò chuyện giữa " + customer.getFullName() + " và Manager đã có sẵn, bỏ qua.");
        }
    }
}
