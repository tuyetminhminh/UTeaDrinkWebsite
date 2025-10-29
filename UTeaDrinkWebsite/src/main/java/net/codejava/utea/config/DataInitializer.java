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
import net.codejava.utea.manager.entity.ShopSection;
import net.codejava.utea.manager.repository.ShopSectionRepository;
import net.codejava.utea.catalog.repository.ProductSyncService;

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
    private final ShopSectionRepository shopSectionRepo;
    private final ProductSyncService productSyncService;
    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;

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

        // 17. Shop Sections (Featured, Top Selling, New Arrivals, Promotions)
        initSections(shop1);

        // 18. Sync Product Stats (rating_avg và sold_count)
        syncProductStats();

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

        ensureUserWithRole("admin@utea.local", "admin", "Admin Seed", "123456", "ACTIVE", "ADMIN");
        ensureUserWithRole("manager@utea.local", "manager", "Manager Seed", "123456", "ACTIVE", "MANAGER");
        ensureUserWithRole("seller@utea.local", "seller", "Seller Seed", "123456", "ACTIVE", "SELLER");
        ensureUserWithRole("customer@utea.local", "customer", "Nguyễn Văn An", "123456", "ACTIVE", "CUSTOMER");
        ensureUserWithRole("customer2@utea.local", "customer2", "Trần Thị Bình", "123456", "ACTIVE", "CUSTOMER");
        ensureUserWithRole("thanhnau25@gmail.com", "customer3", "Cáp Thanh Nhàn", "123456", "ACTIVE", "CUSTOMER");

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
                    "https://i.pinimg.com/736x/f5/d8/bf/f5d8bfaee048fab36aaba31e0928dafd.jpg",
                    "/customer/menu", 0, true);

            createBanner(shop, "Combo Sinh Nhật Ưu Đãi 20%",
                    "https://i.pinimg.com/736x/d1/0d/f3/d10df341186aa730f636095ba1b8837c.jpg",
                    "/customer/menu?category=combo", 1, true);

            createBanner(shop, "Cà Phê Sáng - Thức Dậy Năng Lượng",
                    "https://i.pinimg.com/736x/ec/b4/14/ecb41449333d531f3d6f9bba33c73f04.jpg",
                    "/customer/menu?category=coffee", 2, true);
            createBanner(shop, "Bánh Ngon Đặc Biệt Mùa Hè",
                    "https://i.pinimg.com/1200x/87/29/83/8729832f3c84c3286cd9c2a9b0c24242.jpg",
                    "/customer/menu?category=banhmi", 3, true);

            System.out.println("  ✓ Đã tạo 4 banners mẫu cho shop");
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

        ensureCategory("Trà sữa", "Các loại trà sữa thơm ngon, đa dạng hương vị");
        ensureCategory("Cà phê", "Các loại cà phê rang xay cao cấp, đậm đà");
        ensureCategory("Bánh", "Bánh ngọt, bánh mì, các loại bánh ăn kèm");
        ensureCategory("Sinh tố", "Sinh tố trái cây tươi ngon, bổ dưỡng");
        ensureCategory("Trà trái cây", "Trà trái cây thanh mát, giải nhiệt");
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
        ProductCategory banh = categoryRepo.findByName("Bánh").orElse(null);
        ProductCategory sinhTo = categoryRepo.findByName("Sinh tố").orElse(null);
        ProductCategory traTraiCay = categoryRepo.findByName("Trà trái cây").orElse(null);

     // ============ TRÀ SỮA (11 sản phẩm) ============
        createProduct(shop, traSua,
                "Trà sữa truyền thống",
                "Hương vị trà sữa truyền thống đi kèm trân châu đường đen",
                new BigDecimal("25000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761610306/Tr%C3%A0-s%E1%BB%AFa-Tr%C3%A2n-ch%C3%A2u-%C4%91en-1_lxvady.png",
                "Trà-sữa-Trân-châu-đen-1_lxvady");

        createProduct(shop, traSua,
                "Trà matcha sữa",
                "Matcha đậm vị, hòa quyện sữa tươi ngọt nhẹ.",
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761610557/Matcha-Tea-Latte-with-Matcha-Jelly_zivtxb.png",
                "Matcha-Tea-Latte-with-Matcha-Jelly_zivtxb");

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
                "Trà sữa smoothie",
                "Dâu chua ngọt dịu, sắc hồng bắt mắt, thơm trái cây tự nhiên, càng ngon với thạch",
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761582414/utea/products/37/prod_37_img_1761582407900_1.png",
                "utea/products/37/prod_37_img_1761582407900_1");

        createProduct(shop, traSua,
                "Trà sữa bạc hà",
                "Trà sữa mang hương vị ngọt ngào kết hợp vị thơm của bạc hà tạo nên hương vị tuyệt đỉnh",
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013915/daxay1_m2cmdw.png",
                "daxay1_m2cmdw");

        // ============ CÀ PHÊ (5 sản phẩm) ============
        createProduct(shop, caPhe,
                "Cà phê sữa đá",
                "Cà phê phin truyền thống pha cùng sữa đặc thơm béo.",
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761610738/HLC_New_logo_5.1_Products__PHIN_SUADA_hsnohh.jpg",
                "HLC_New_logo_5.1_Products__PHIN_SUADA_hsnohh");

        createProduct(shop, caPhe,
                "Cà phê đen đá",
                "Cà phê phin đen nguyên chất, đậm đà, mạnh mẽ",
                new BigDecimal("25000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761610951/HLC_New_logo_5.1_Products__PHIN_DEN_DA_td4qlq.png",
                "HLC_New_logo_5.1_Products__PHIN_DEN_DA_td4qlq");

        createProduct(shop, caPhe,
                "Bạc xỉu",
                "Cà phê sữa ngọt ngào, thơm béo, phù hợp cho người thích vị ngọt",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761611030/bac-xiu_iklh4s.png",
                "bac-xiu_iklh4s");

        createProduct(shop, caPhe,
                "Cà phê trứng",
                "Cà phê trứng truyền thống Hà Nội, béo ngậy, thơm ngon",
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761611209/ca_phe_trung_da_to6aqn.png",
                "ca_phe_trung_da_to6aqn");

        createProduct(shop, caPhe,
                "Cappuccino",
                "Cà phê espresso kết hợp sữa tươi và foam mịn màng",
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761640520/PAUL-Coffee-Iced-Cappuccino_qwqx3r.png",
                "PAUL-Coffee-Iced-Cappuccino_qwqx3r");



        createProduct(shop, caPhe,
                "Cà phê muối",
                "Espresso và kem sữa mặn ngọt cân bằng; lớp muối nhẹ làm bật hương cà, béo mịn nhưng không ngấy",
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761644681/Quan-ca-phe-quan-7-mo-24h-Bamos-Coffee-co-Acoustic-To-chuc-su-kien-boi-bai-tarot5-1_azjbvw.png",
                "Quan-ca-phe-quan-7-mo-24h-Bamos-Coffee-co-Acoustic-To-chuc-su-kien-boi-bai-tarot5-1_azjbvw");

        createProduct(shop, caPhe,
                "Cà phê hạnh nhân",
                "Hương espresso đậm quyện vị bùi của sữa hạnh nhân, hậu vị thanh, ít béo, dễ chịu cả ngày.",
                new BigDecimal("25000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761644662/HLC_New_logo_5.1_Products__PHINDI_HANH_NHAN_ot8tee.jpg",
                "HLC_New_logo_5.1_Products__PHINDI_HANH_NHAN_ot8tee");

        createProduct(shop, caPhe,
                "Espresso",
                "Espresso pha loãng với nước nóng, vị sáng rõ, nhẹ đắng, hậu vị sạch—lựa chọn “tỉnh táo mà không nặng sữa",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761644682/Quan-ca-phe-quan-7-mo-24h-Bamos-Coffee-co-Acoustic-To-chuc-su-kien-boi-bai-tarot21_arffy4.png",
                "Quan-ca-phe-quan-7-mo-24h-Bamos-Coffee-co-Acoustic-To-chuc-su-kien-boi-bai-tarot21_arffy4");

        createProduct(shop, caPhe,
                "Cà phê cốt dừa",
                "Espresso cùng cốt dừa xay mát; thơm nhiệt đới, béo mà không ngấy",
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761645096/cafe_dua_8dd8c0fa674f48a0bf53f8196547552e_master_xsusuw.png",
                "cafe_dua_8dd8c0fa674f48a0bf53f8196547552e_master_xsusuw");

        createProduct(shop, caPhe,
                "Caramel Macchiato",
                "Sữa êm, espresso rót lên, caramel thơm bơ; ngọt mặn hài hòa.",
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761644680/Quan-ca-phe-quan-7-mo-24h-Bamos-Coffee-co-Acoustic-To-chuc-su-kien-boi-bai-tarot1-1_rscb2a.png",
                "Quan-ca-phe-quan-7-mo-24h-Bamos-Coffee-co-Acoustic-To-chuc-su-kien-boi-bai-tarot1-1_rscb2a");

        // ============ BÁNH (11 sản phẩm) ============
        createProduct(shop, banh,
                "Bánh cầu vồng",
                "Thiết kế dễ thương, mỗi màu cầu vồng là một hương vị làm cho bánh có vị độc đáo và ngon miệng",
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042330/banh_cau_vong_jlqx1n.jpg",
                "banh_cau_vong_jlqx1n");

        createProduct(shop, banh,
                "Bánh sandwich trứng chảy",
                "Vỏ bánh mềm, trứng chảy làm bùng nổ vị giác.",
                new BigDecimal("49000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042371/sandwich_mhqntr.png",
                "sandwich_mhqntr");

        createProduct(shop, banh,
                "Bánh sừng bò",
                "Loại bánh nổi tiếng từ nước Pháp, vỏ bánh giòn, nhân cherry bùng nổ vị giác.",
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042364/roissant_giam_bong_c3eaxh.png",
                "roissant_giam_bong_c3eaxh");

        createProduct(shop, banh,
                "Bánh mochi nướng",
                "Loại bánh đến từ Nhật Bản, vỏ bánh mềm, nhân khoai môn tan chảy.",
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042357/mochi_nuong_lcgjow.png",
                "mochi_nuong_lcgjow");

        createProduct(shop, banh,
                "Bánh donut",
                "Vỏ bánh ngon, được phủ một lớp kem dâu ngon lành",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042350/donut_socola_pucmiy.png",
                "donut_socola_pucmiy");

        createProduct(shop, banh,
                "Bánh sừng bò socola",
                "Vỏ bánh giòn, hương socola bùng nổ vị giác.",
                new BigDecimal("39000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042334/banh_croissant_vsmahb.png",
                "banh_croissant_vsmahb");

        createProduct(shop, banh,
                "Bánh kem oreo",
                "Bánh kem ngon, bơ kem béo ngậy",
                new BigDecimal("45000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042333/banh_kem_oreo_r5k9y2.png",
                "banh_kem_oreo_r5k9y2");

        createProduct(shop, banh,
                "Bánh pudding Caramel",
                "Hương caramel ngọt ngào, bánh mềm.",
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042334/caramen_wtbkde.png",
                "caramen_wtbkde");

        createProduct(shop, banh,
                "Bánh su kem",
                "Vỏ bánh giòn, nhân kem ngọt ngào bùng nổ vị giác",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042334/banh_su_kem_nea1hj.png",
                "banh_su_kem_nea1hj");

        createProduct(shop, banh,
                "Bánh tiramisu",
                "Bánh mềm, nhân kem kết hợp trứng làm gia tăng hương vị",
                new BigDecimal("39000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760042332/banh_tiramisu_dgv2zi.png",
                "banh_tiramisu_dgv2zi");

        createProduct(shop, banh,
                "Bánh bông lan trứng muối",
                "Bánh bông lan mềm mịn kết hợp nhân trứng muối béo ngậy",
                new BigDecimal("42000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761637877/MDA1MTg1MDU_eqbzg7.jpg",
                "MDA1MTg1MDU_eqbzg7");

        // ============ SINH TỐ (5 sản phẩm) ============
        createProduct(shop, sinhTo,
                "Sinh tố bơ",
                "Sinh tố bơ đậm đà, béo ngậy, bổ dưỡng",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761612529/24_llfykj.png",
                "24_llfykj");

        createProduct(shop, sinhTo,
                "Sinh tố dâu",
                "Sinh tố dâu tươi ngon, ngọt tự nhiên, sảng khoái",
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761612513/dau2_b2f4da868c4347fd942aa896c754c95c_ovwy1q.png",
                "dau2_b2f4da868c4347fd942aa896c754c95c_ovwy1q");

        createProduct(shop, sinhTo,
                "Sinh tố xoài",
                "Sinh tố xoài thơm ngon, mát lạnh, giàu vitamin",
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761611636/xoai-sinhto_a3ee750fb32f410085123b2b2bc25d10_bjkrwx.png",
                "xoai-sinhto_a3ee750fb32f410085123b2b2bc25d10_bjkrwx");

        createProduct(shop, sinhTo,
                "Sinh tố dưa hấu",
                "Sinh tố dưa hấu mát lạnh, giải nhiệt tuyệt vời",
                new BigDecimal("30000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761611747/pngtree-juicy-watermelon-smoothie-in-a-tall-glass-with-mint-leaf-png-image_21074832_lqdpy3.png",
                "pngtree-juicy-watermelon-smoothie-in-a-tall-glass-with-mint-leaf-png-image_21074832_lqdpy3");

        createProduct(shop, sinhTo,
                "Sinh tố sapoche",
                "Sinh tố sapoche béo ngậy, thơm ngon, bổ dưỡng",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761611838/sinhto-sapoche_014200e3709c4450a2989173d951d9d4_fkckfl.png",
                "sinhto-sapoche_014200e3709c4450a2989173d951d9d4_fkckfl");


        createProduct(shop, sinhTo,
                "Sinh tố mãng cầu",
                "Sinh tố mãng cầu mát lạnh, thơm dịu, vị chua ngọt hài hòa, sánh mịn và béo nhẹ—uống một ngụm là tỉnh người",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761637850/mangcau_026c13c5e9aa4562ad41d77daa19fdbb_ev5g4q.png",
                "mangcau_026c13c5e9aa4562ad41d77daa19fdbb_ev5g4q");

        createProduct(shop, sinhTo,
                "Sinh tố việt quất",
                "Sinh tố việt quất tím mướt, chua nhẹ, ngọt thanh, thơm lừng và mịn sánh—uống vào mát người, tỉnh vị giác.",
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761637838/sinh-to-viet-quat_mmycd6.png",
                "sinh-to-viet-quat_mmycd6");

        createProduct(shop, sinhTo,
                "Sinh tố chuối",
                "Sinh tố chuối sánh mịn, thơm ngậy, ngọt dịu tự nhiên—uống một hơi là no nhẹ và tràn năng lượng.",
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761637862/sinhto-chuoi_2cae15fae32342adabf3ca2af104cb5b_mkprby.png",
                "sinhto-chuoi_2cae15fae32342adabf3ca2af104cb5b_mkprby");

        createProduct(shop, sinhTo,
                "Sinh tố thanh long",
                "Sinh tố thanh long mát rượi, ngọt thanh, hơi thơm hoa quả nhiệt đới; đỏ thì rực rỡ bắt mắt, trắng thì nhẹ nhàng, cực dễ uống.",
                new BigDecimal("30000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761643516/sinhto-thanhlong-1024_e89fa4797ceb48d3a05c381dd8ab9121_zfi0y3.png",
                "sinhto-thanhlong-1024_e89fa4797ceb48d3a05c381dd8ab9121_zfi0y3");

        createProduct(shop, sinhTo,
                "Sinh tố đu đủ",
                "Sinh tố đu đủ mượt sánh, thơm ngọt tự nhiên, béo nhẹ và mát lạnh—uống vào là “dịu bụng”, dễ chịu.",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1761643801/sinhto-dudu_e8f5a7b3b86f4668b52a2c0393aaa86e_master_zsssxc.png",
                "sinhto-dudu_e8f5a7b3b86f4668b52a2c0393aaa86e_master_zsssxc");
        // ============ TRÀ TRÁI CÂY (10 sản phẩm) ============
        createProduct(shop, traTraiCay,
                "Trà chanh nhiệt đới",
                "Hương chanh mát lạnh, đánh tan cơn nóng mùa hè",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013925/tra8_kqmw06.png",
                "tra8_kqmw06");

        createProduct(shop, traTraiCay,
                "Trà cherry nhiệt đới",
                "Hương cherry mát lạnh, bùng nổ vị giác ngày hè",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013922/Tra9_pmuarx.png",
                "Tra9_pmuarx");

        createProduct(shop, traTraiCay,
                "Trà tắc",
                "Thức uống phổ biến ngày hè, vị ngon khó cưỡng",
                new BigDecimal("30000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013917/tra4_vvnmry.png",
                "tra4_vvnmry");

        createProduct(shop, traTraiCay,
                "Hồng trà UTea",
                "Hồng trà thơm ngon, thanh mát, đậm vị",
                new BigDecimal("32000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013916/tra1_j3r8fr.png",
                "tra1_j3r8fr");

        createProduct(shop, traTraiCay,
                "Trà đào cam sả",
                "Thức uống phổ biến, hương vị hài hòa giữa đào, cam và sả",
                new BigDecimal("38000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760045845/Tr%C3%A0-%C4%90%C3%A0o-Cam-s%E1%BA%A3_rmwgcn.jpg",
                "Trà-Đào-Cam-sả_rmwgcn");

        createProduct(shop, traTraiCay,
                "Trà xanh",
                "Hương vị ngọt thanh, mát mẻ của trà xanh",
                new BigDecimal("25000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013917/tra4_vvnmry.png",
                "tra_xanh");

        createProduct(shop, traTraiCay,
                "Trà ô long",
                "Trà ô long thơm ngon, thanh mát, giàu chất chống oxy hóa",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013916/tra1_j3r8fr.png",
                "tra_o_long");

        createProduct(shop, traTraiCay,
                "Trà vải",
                "Trà vải ngọt ngào, thơm mát, hương vị độc đáo",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013917/tra2_fxdxpc.png",
                "tra_vai");

        createProduct(shop, traTraiCay,
                "Trà dâu tằm",
                "Vị ngọt của dâu tằm, đánh tan cái nóng mùa hè",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013919/tra6_n5ldnj.png",
                "tra_dau_tam");

        createProduct(shop, traTraiCay,
                "Trà nhiệt đới",
                "Mang lại cảm giác sảng khoái, đánh tan cơn nóng Sài Gòn",
                new BigDecimal("35000"),
                "https://res.cloudinary.com/dhmh2ekqy/image/upload/v1760013917/tra5_ekeiow.png",
                "tra_nhiet_doi");
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
                    .ratingAvg(null) // Sẽ được tính toán từ reviews
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
        User customer3 = userRepo.findByEmail("thanhnau25@gmail.com").orElse(null);

        Address address1 = customer != null ? addressRepo.findByUserId(customer.getId()).stream().findFirst().orElse(null) : null;
        Address address2 = customer2 != null ? addressRepo.findByUserId(customer2.getId()).stream().findFirst().orElse(null) : null;

        java.util.List<Product> products = productRepo.findAll();

        if (customer == null || address1 == null || products.isEmpty()) {
            System.out.println("  ⚠ WARNING: Cannot create orders - missing users, addresses or products");
            return;
        }

        // Kiểm tra xem đã có orders chưa
        long existingOrders = orderRepo.count();
        if (existingOrders > 0) {
            System.out.println("  → Orders đã tồn tại (" + existingOrders + " orders)");
            return;
        }

        int orderCounter = 1;

        // 2 đơn NEW - Mới tạo, chưa xác nhận
        createSampleOrder(shop, customer, address1, products.get(0),
                String.format("ORD-2025-%06d", orderCounter++), OrderStatus.NEW,
                LocalDateTime.now().minusMinutes(30),
                2, new BigDecimal("25000"), new BigDecimal("50000"), new BigDecimal("65000"));

        if (products.size() > 1) {
            createSampleOrder(shop, customer2, address2, products.get(1),
                    String.format("ORD-2025-%06d", orderCounter++), OrderStatus.NEW,
                    LocalDateTime.now().minusMinutes(15),
                    1, new BigDecimal("45000"), new BigDecimal("45000"), new BigDecimal("60000"));
        }

        // 3 đơn PREPARING - Chờ shipper tự nhận
        if (products.size() > 2) {
            createSampleOrder(shop, customer, address1, products.get(2),
                    String.format("ORD-2025-%06d", orderCounter++), OrderStatus.PREPARING,
                    LocalDateTime.now().minusHours(2),
                    2, new BigDecimal("35000"), new BigDecimal("70000"), new BigDecimal("85000"));
        }

        if (products.size() > 3) {
            createSampleOrder(shop, customer2, address2, products.get(3),
                    String.format("ORD-2025-%06d", orderCounter++), OrderStatus.PREPARING,
                    LocalDateTime.now().minusHours(3),
                    1, new BigDecimal("39000"), new BigDecimal("39000"), new BigDecimal("54000"));
        }

        if (products.size() > 4) {
            createSampleOrder(shop, customer3, address1, products.get(4),
                    String.format("ORD-2025-%06d", orderCounter++), OrderStatus.PREPARING,
                    LocalDateTime.now().minusHours(4),
                    3, new BigDecimal("32000"), new BigDecimal("96000"), new BigDecimal("111000"));
        }

        // 1 đơn DELIVERING - Đang giao
        if (products.size() > 5) {
            createSampleOrder(shop, customer, address1, products.get(5),
                    String.format("ORD-2025-%06d", orderCounter++), OrderStatus.DELIVERING,
                    LocalDateTime.now().minusHours(5),
                    2, new BigDecimal("45000"), new BigDecimal("90000"), new BigDecimal("105000"));
        }

        // 15 đơn DELIVERED - Đã giao (để tính sold_count)
        for (int i = 0; i < 15 && (i + 6) < products.size(); i++) {
            User orderUser = (i % 3 == 0) ? customer : (i % 3 == 1) ? customer2 : customer3;
            Address orderAddress = (i % 2 == 0) ? address1 : address2;
            Product product = products.get(i + 6);

            int quantity = 1 + (i % 3); // Quantity từ 1-3
            BigDecimal unitPrice = product.getBasePrice();
            BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
            BigDecimal total = subtotal.add(new BigDecimal("15000")); // + shipping fee

            createSampleOrder(shop, orderUser, orderAddress, product,
                    String.format("ORD-2025-%06d", orderCounter++), OrderStatus.DELIVERED,
                    LocalDateTime.now().minusDays(i + 1).minusHours(i % 12),
                    quantity, unitPrice, subtotal, total);
        }

        System.out.println("  ✓ Đã tạo " + (orderCounter - 1) + " orders");
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
    private void initReviews() {
        System.out.println("→ Khởi tạo Reviews...");

        User customer = userRepo.findByEmail("customer@utea.local").orElse(null);
        User customer2 = userRepo.findByEmail("customer2@utea.local").orElse(null);
        User customer3 = userRepo.findByEmail("thanhnau25@gmail.com").orElse(null);

        java.util.List<Product> allProducts = productRepo.findAll();

        if (customer == null || allProducts.isEmpty()) {
            System.out.println("  ⚠ WARNING: Cannot create reviews - missing users or products");
            return;
        }

        // Kiểm tra xem đã có reviews chưa
        long existingReviews = reviewRepo.count();
        if (existingReviews > 0) {
            System.out.println("  → Reviews đã tồn tại (" + existingReviews + " reviews)");
            return;
        }

        // Tạo reviews cho nhiều sản phẩm khác nhau
        int reviewCount = 0;

        // Review cho 15 sản phẩm đầu tiên với rating đa dạng
        for (int i = 0; i < Math.min(15, allProducts.size()); i++) {
            Product product = allProducts.get(i);

            // Review từ customer 1
            if (customer != null) {
                int rating = 3 + (i % 3); // Rating từ 3-5
                reviewRepo.save(Review.builder()
                        .user(customer)
                        .product(product)
                        .rating(rating)
                        .content(getReviewContent(rating))
                        .status(ReviewStatus.APPROVED)
                        .helpfulCount(i % 10)
                        .build());
                reviewCount++;
            }

            // Review từ customer 2 (chỉ cho một số sản phẩm)
            if (customer2 != null && i % 2 == 0) {
                int rating = 4 + (i % 2); // Rating 4 hoặc 5
                reviewRepo.save(Review.builder()
                        .user(customer2)
                        .product(product)
                        .rating(rating)
                        .content(getReviewContent(rating))
                        .status(ReviewStatus.APPROVED)
                        .helpfulCount(i % 8)
                        .build());
                reviewCount++;
            }

            // Review từ customer 3 (chỉ cho một số sản phẩm)
            if (customer3 != null && i % 3 == 0) {
                int rating = 3 + (i % 3); // Rating từ 3-5
                reviewRepo.save(Review.builder()
                        .user(customer3)
                        .product(product)
                        .rating(rating)
                        .content(getReviewContent(rating))
                        .status(ReviewStatus.APPROVED)
                        .helpfulCount(i % 12)
                        .build());
                reviewCount++;
            }
        }

        System.out.println("  ✓ Đã tạo " + reviewCount + " reviews cho sản phẩm");
    }

    private String getReviewContent(int rating) {
        switch (rating) {
            case 5:
                return "Sản phẩm tuyệt vời! Rất hài lòng, sẽ mua lại nhiều lần nữa!";
            case 4:
                return "Sản phẩm tốt, chất lượng ổn, giao hàng nhanh";
            case 3:
                return "Sản phẩm bình thường, có thể cải thiện thêm";
            case 2:
                return "Không như mong đợi lắm, cần cải thiện chất lượng";
            default:
                return "Không hài lòng với sản phẩm";
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

        // Kiểm tra xem đã có assignments chưa
        long existingAssignments = shipAssignmentRepo.count();
        if (existingAssignments > 0) {
            System.out.println("  → Ship assignments đã tồn tại (" + existingAssignments + " assignments)");
            return;
        }

        // Lấy tất cả orders DELIVERING và DELIVERED
        java.util.List<Order> deliveringOrders = orderRepo.findAll().stream()
                .filter(o -> OrderStatus.DELIVERING.equals(o.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        java.util.List<Order> deliveredOrders = orderRepo.findAll().stream()
                .filter(o -> OrderStatus.DELIVERED.equals(o.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        int assignmentCount = 0;
        User[] shippers = {shipper1, shipper2, shipper3,
                shipper4 != null ? shipper4 : shipper1,
                shipper5 != null ? shipper5 : shipper2};

        // Gán shipper cho đơn DELIVERING
        for (int i = 0; i < deliveringOrders.size(); i++) {
            Order order = deliveringOrders.get(i);
            User shipper = shippers[i % shippers.length];
            assignShipperToOrder(order.getOrderCode(), shipper, "DELIVERING",
                    "Đang giao hàng", order.getCreatedAt());
            assignmentCount++;
        }

        // Gán shipper cho đơn DELIVERED
        for (int i = 0; i < deliveredOrders.size(); i++) {
            Order order = deliveredOrders.get(i);
            User shipper = shippers[i % shippers.length];
            LocalDateTime deliveredAt = order.getCreatedAt().plusHours(2);
            assignShipperToOrder(order.getOrderCode(), shipper, "DELIVERED",
                    "Đã giao hàng thành công",
                    order.getCreatedAt(),
                    deliveredAt);
            assignmentCount++;
        }

        System.out.println("  ✓ Đã gán shipper cho " + assignmentCount + " đơn hàng");
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

    // ==================== 17. SHOP SECTIONS ====================
    private void initSections(Shop shop) {
        System.out.println("→ Khởi tạo Shop Sections...");

        if (shopSectionRepo.findByShopIdOrderBySortOrderAsc(shop.getId()).isEmpty()) {
            // 1. Sản phẩm nổi bật
            createSection(shop, "Sản phẩm nổi bật", "FEATURED",
                    "{\"criteria\":\"featured\",\"limit\":12}", 0);

            // 2. Bán chạy nhất
            createSection(shop, "Bán chạy nhất", "TOP_SELLING",
                    "{\"criteria\":\"sold_count\",\"order\":\"DESC\",\"limit\":12}", 1);

            // 3. Sản phẩm mới
            createSection(shop, "Sản phẩm mới", "NEW_ARRIVALS",
                    "{\"criteria\":\"created_at\",\"order\":\"DESC\",\"limit\":12}", 2);

            // 4. Đánh giá cao
            createSection(shop, "Đánh giá cao", "TOP_RATED",
                    "{\"criteria\":\"rating_avg\",\"order\":\"DESC\",\"limit\":12}", 3);


            System.out.println("  ✓ Đã tạo 4 sections cho shop");
        } else {
            System.out.println("  → Sections đã tồn tại");
        }
    }

    private void createSection(Shop shop, String title, String sectionType,
                               String contentJson, int sortOrder) {
        ShopSection section = ShopSection.builder()
                .shop(shop)
                .title(title)
                .sectionType(sectionType)
                .contentJson(contentJson)
                .sortOrder(sortOrder)
                .isActive(true)
                .build();
        shopSectionRepo.save(section);
    }

    // ==================== 18. SYNC PRODUCT STATS ====================
    private void syncProductStats() {
        System.out.println("→ Đồng bộ Product Stats (rating_avg và sold_count)...");

        try {
            productSyncService.syncAllProductsBatch();
            System.out.println("  ✓ Đã đồng bộ rating_avg và sold_count cho tất cả sản phẩm");
        } catch (Exception e) {
            System.out.println("  ⚠ Lỗi khi đồng bộ product stats: " + e.getMessage());
        }
    }
}
