package com.example.Admin.Config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.Admin.Shop.Model.DiscountType;
import com.example.Admin.Shop.Model.InventoryHistoryType;
import com.example.Admin.Shop.Model.PaymentMethod;
import com.example.Admin.Shop.Model.ShopBrand;
import com.example.Admin.Shop.Model.ShopCategory;
import com.example.Admin.Shop.Model.ShopCoupon;
import com.example.Admin.Shop.Model.ShopInventoryHistory;
import com.example.Admin.Shop.Model.ShopOrder;
import com.example.Admin.Shop.Model.ShopOrderItem;
import com.example.Admin.Shop.Model.ShopOrderStatus;
import com.example.Admin.Shop.Model.ShopProduct;
import com.example.Admin.Shop.Model.ShopProductImage;
import com.example.Admin.Shop.Model.ShopRole;
import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Model.ShopUserCoupon;
import com.example.Admin.Shop.Repository.ShopBrandRepository;
import com.example.Admin.Shop.Repository.ShopCategoryRepository;
import com.example.Admin.Shop.Repository.ShopCouponRepository;
import com.example.Admin.Shop.Repository.ShopInventoryHistoryRepository;
import com.example.Admin.Shop.Repository.ShopOrderRepository;
import com.example.Admin.Shop.Repository.ShopProductRepository;
import com.example.Admin.Shop.Repository.ShopUserCouponRepository;
import com.example.Admin.Shop.Repository.ShopUserRepository;
import com.example.Admin.Shop.Service.ShopSlugService;

@Component
public class DataInitializer implements CommandLineRunner {
    private final ShopUserRepository userRepository;
    private final ShopCategoryRepository categoryRepository;
    private final ShopBrandRepository brandRepository;
    private final ShopProductRepository productRepository;
    private final ShopCouponRepository couponRepository;
    private final ShopUserCouponRepository userCouponRepository;
    private final ShopOrderRepository orderRepository;
    private final ShopInventoryHistoryRepository inventoryHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ShopUserRepository userRepository, ShopCategoryRepository categoryRepository,
            ShopBrandRepository brandRepository, ShopProductRepository productRepository,
            ShopCouponRepository couponRepository, ShopUserCouponRepository userCouponRepository,
            ShopOrderRepository orderRepository, ShopInventoryHistoryRepository inventoryHistoryRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.orderRepository = orderRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        if (categoryRepository.count() == 0) {
            seedCatalog();
        }
        seedCoupons();
        seedSampleOrder();
    }

    private void seedUsers() {
        createUser("admin111", "admin111@store.com", "Quản trị hệ thống", "0900000000",
                "Hồ Chí Minh", ShopRole.SUPER_ADMIN);
        createUser("customer1", "customer1@store.com", "Nguyễn Khách Một", "0911111111",
                "12 Nguyễn Trãi, Quận 1", ShopRole.CUSTOMER);
        createUser("customer2", "customer2@store.com", "Trần Khách Hai", "0922222222",
                "45 Lê Lợi, Quận 3", ShopRole.CUSTOMER);
    }

    private ShopUser createUser(String username, String email, String fullName, String phone, String address, ShopRole role) {
        return userRepository.findByUsernameIgnoreCase(username).orElseGet(() -> {
            ShopUser user = new ShopUser();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode("123123"));
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setAddress(address);
            user.setRole(role);
            user.setEnabled(true);
            return userRepository.save(user);
        });
    }

    private void seedCatalog() {
        ShopBrand house = brand("QL Caffee");
        ShopBrand fresh = brand("Fresh Daily");

        ShopCategory coffee = category("Cà phê", "Cà phê rang xay và espresso");
        ShopCategory milkTea = category("Trà sữa", "Trà sữa và topping");
        ShopCategory juice = category("Nước ép", "Nước ép, sinh tố trái cây");
        ShopCategory cake = category("Bánh ngọt", "Bánh dùng kèm cà phê");
        ShopCategory snack = category("Đồ ăn nhẹ", "Bữa nhẹ tiện lợi");

        List<ProductSeed> products = List.of(
                new ProductSeed("Cà phê đen", coffee, house, "cf_den.jpg", 29000, null, 30, true, true),
                new ProductSeed("Cà phê sữa", coffee, house, "cf_sua.jpg", 35000, 31000, 24, true, true),
                new ProductSeed("Espresso", coffee, house, "cf_espr.jpg", 39000, null, 18, false, true),
                new ProductSeed("Latte", coffee, house, "cf_latte.jpg", 49000, 45000, 15, true, true),
                new ProductSeed("Cappuccino", coffee, house, "cf_capu.jpg", 49000, null, 3, true, false),
                new ProductSeed("Trà sữa trân châu", milkTea, house, "ts_tc.jpg", 42000, 39000, 28, true, true),
                new ProductSeed("Trà sữa matcha", milkTea, house, "ts_matcha.jpg", 45000, null, 21, true, true),
                new ProductSeed("Trà sữa khoai môn", milkTea, house, "ts_khoaimon.jpg", 45000, 41000, 12, false, true),
                new ProductSeed("Trà sữa hồng trà", milkTea, house, "ts_hong.jpg", 39000, null, 4, false, false),
                new ProductSeed("Trà sữa cheese", milkTea, house, "ts_cheese.jpg", 49000, 45000, 9, true, true),
                new ProductSeed("Nước ép cam", juice, fresh, "ep_cam.jpg", 39000, null, 16, true, true),
                new ProductSeed("Nước ép táo", juice, fresh, "ep_tao.jpg", 39000, 35000, 10, false, true),
                new ProductSeed("Nước ép ổi", juice, fresh, "ep_oi.jpg", 35000, null, 2, false, false),
                new ProductSeed("Sinh tố bơ", juice, fresh, "st_bo.jpg", 49000, 45000, 8, true, true),
                new ProductSeed("Sinh tố xoài", juice, fresh, "st_xoai.jpg", 45000, null, 14, false, true),
                new ProductSeed("Cheesecake", cake, fresh, "banh_cheese.jpg", 59000, 52000, 7, true, true),
                new ProductSeed("Tiramisu", cake, fresh, "banh_tira.jpg", 65000, null, 5, true, true),
                new ProductSeed("Muffin chocolate", cake, fresh, "banh_muffin.jpg", 39000, 35000, 20, false, true),
                new ProductSeed("Burger bò", snack, fresh, "bm_burger.jpg", 79000, 69000, 6, true, true),
                new ProductSeed("Salad Caesar", snack, fresh, "salad_caesar.jpg", 69000, null, 3, false, true));

        products.forEach(this::product);
    }

    private ShopCategory category(String name, String description) {
        ShopCategory category = new ShopCategory();
        category.setName(name);
        category.setDescription(description);
        category.setActive(true);
        return categoryRepository.save(category);
    }

    private ShopBrand brand(String name) {
        return brandRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            ShopBrand brand = new ShopBrand();
            brand.setName(name);
            return brandRepository.save(brand);
        });
    }

    private void product(ProductSeed seed) {
        String baseSlug = ShopSlugService.slugify(seed.name());
        String slug = baseSlug;
        int suffix = 2;
        while (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + suffix++;
        }

        ShopProduct product = new ShopProduct();
        product.setName(seed.name());
        product.setSlug(slug);
        product.setDescription("Sản phẩm " + seed.name() + " được chuẩn bị mới mỗi ngày, phù hợp để demo tìm kiếm, lọc, giỏ hàng và tồn kho.");
        product.setPrice(BigDecimal.valueOf(seed.price()));
        product.setSalePrice(seed.salePrice() == null ? null : BigDecimal.valueOf(seed.salePrice()));
        product.setQuantity(seed.quantity());
        product.setActive(true);
        product.setFeatured(seed.featured());
        product.setNewProduct(seed.newProduct());
        product.setSaleProduct(seed.salePrice() != null);
        product.setCategory(seed.category());
        product.setBrand(seed.brand());

        ShopProductImage image = new ShopProductImage();
        image.setImagePath("/img/" + seed.image());
        image.setMainImage(true);
        image.setProduct(product);
        product.getImages().add(image);

        productRepository.save(product);
    }

    private void seedCoupons() {
        ShopCoupon sale10 = coupon("SALE10", DiscountType.PERCENT, 10, 100000, null,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusMonths(2), 100, true, true);
        coupon("GIAM50K", DiscountType.FIXED_AMOUNT, 50000, 500000, null,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusMonths(2), 50, true, true);
        ShopCoupon vip20 = coupon("VIP20", DiscountType.PERCENT, 20, 150000, 100000,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusMonths(2), 20, true, false);
        coupon("EXPIRED10", DiscountType.PERCENT, 10, 100000, null,
                LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusDays(1), 10, true, true);

        userRepository.findByUsernameIgnoreCase("customer1").ifPresent(customer -> {
            if (!userCouponRepository.existsByUserAndCoupon(customer, vip20)) {
                ShopUserCoupon userCoupon = new ShopUserCoupon();
                userCoupon.setUser(customer);
                userCoupon.setCoupon(vip20);
                userCouponRepository.save(userCoupon);
            }
        });

        // Make sure SALE10 is referenced so static analyzers do not complain about the seed variable.
        sale10.setActive(true);
    }

    private ShopCoupon coupon(String code, DiscountType type, int value, int minOrder, Integer maxDiscount,
            LocalDateTime startDate, LocalDateTime endDate, Integer usageLimit, boolean active, boolean publicCoupon) {
        return couponRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
            ShopCoupon coupon = new ShopCoupon();
            coupon.setCode(code);
            coupon.setDiscountType(type);
            coupon.setDiscountValue(BigDecimal.valueOf(value));
            coupon.setMinOrderAmount(BigDecimal.valueOf(minOrder));
            coupon.setMaxDiscountAmount(maxDiscount == null ? null : BigDecimal.valueOf(maxDiscount));
            coupon.setStartDate(startDate);
            coupon.setEndDate(endDate);
            coupon.setUsageLimit(usageLimit);
            coupon.setActive(active);
            coupon.setPublicCoupon(publicCoupon);
            return couponRepository.save(coupon);
        });
    }

    private void seedSampleOrder() {
        if (orderRepository.count() > 0 || productRepository.count() == 0) {
            return;
        }
        ShopUser customer = userRepository.findByUsernameIgnoreCase("customer1").orElseThrow();
        ShopProduct product = productRepository.findAll().get(0);
        ShopOrder order = new ShopOrder();
        order.setOrderCode("DHDEMO001");
        order.setUser(customer);
        order.setReceiverName(customer.getFullName());
        order.setReceiverPhone(customer.getPhone());
        order.setShippingAddress(customer.getAddress());
        order.setPaymentMethod(PaymentMethod.COD);
        order.setStatus(ShopOrderStatus.COMPLETED);
        order.setSubtotal(product.getEffectivePrice());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(product.getEffectivePrice());

        ShopOrderItem item = new ShopOrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setPrice(product.getEffectivePrice());
        item.setQuantity(1);
        item.setTotal(product.getEffectivePrice());
        order.getItems().add(item);

        orderRepository.save(order);

        ShopInventoryHistory history = new ShopInventoryHistory();
        history.setProduct(product);
        history.setType(InventoryHistoryType.EXPORT);
        history.setQuantity(1);
        history.setNote("Đơn mẫu dashboard DHDEMO001");
        inventoryHistoryRepository.save(history);
    }

    private record ProductSeed(String name, ShopCategory category, ShopBrand brand, String image, int price,
            Integer salePrice, int quantity, boolean featured, boolean newProduct) {
    }
}
