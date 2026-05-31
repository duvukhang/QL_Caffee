package com.example.Admin.Shop.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Admin.Shop.Model.DiscountType;
import com.example.Admin.Shop.Model.InventoryHistoryType;
import com.example.Admin.Shop.Model.ShopCategory;
import com.example.Admin.Shop.Model.ShopCoupon;
import com.example.Admin.Shop.Model.ShopInventoryHistory;
import com.example.Admin.Shop.Model.ShopOrder;
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
import com.example.Admin.Shop.Repository.ShopReviewRepository;
import com.example.Admin.Shop.Repository.ShopUserCouponRepository;
import com.example.Admin.Shop.Repository.ShopUserRepository;
import com.example.Admin.Shop.Service.ShopCurrentUserService;
import com.example.Admin.Shop.Service.ShopOrderService;
import com.example.Admin.Shop.Service.ShopSlugService;

@Controller
public class ShopAdminController {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE = Pattern.compile("^\\d{10,11}$");
    private static final List<ShopRole> EMPLOYEE_ROLES = List.of(
            ShopRole.STAFF, ShopRole.MANAGER, ShopRole.ADMIN, ShopRole.SUPER_ADMIN);
    private static final List<ShopRole> ADMIN_ASSIGNABLE_EMPLOYEE_ROLES = List.of(
            ShopRole.STAFF, ShopRole.MANAGER, ShopRole.ADMIN);
    private static final List<ShopRole> MANAGER_ASSIGNABLE_EMPLOYEE_ROLES = List.of(ShopRole.STAFF);

    private final ShopUserRepository userRepository;
    private final ShopCategoryRepository categoryRepository;
    private final ShopBrandRepository brandRepository;
    private final ShopProductRepository productRepository;
    private final ShopCouponRepository couponRepository;
    private final ShopUserCouponRepository userCouponRepository;
    private final ShopOrderRepository orderRepository;
    private final ShopReviewRepository reviewRepository;
    private final ShopInventoryHistoryRepository inventoryHistoryRepository;
    private final ShopOrderService orderService;
    private final ShopCurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public ShopAdminController(ShopUserRepository userRepository, ShopCategoryRepository categoryRepository,
            ShopBrandRepository brandRepository, ShopProductRepository productRepository,
            ShopCouponRepository couponRepository, ShopUserCouponRepository userCouponRepository,
            ShopOrderRepository orderRepository, ShopReviewRepository reviewRepository,
            ShopInventoryHistoryRepository inventoryHistoryRepository, ShopOrderService orderService,
            ShopCurrentUserService currentUserService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
        this.orderService = orderService;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = today.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        LocalDateTime startOfYear = today.withDayOfYear(1).atStartOfDay();
        LocalDateTime startOfNextYear = today.withDayOfYear(1).plusYears(1).atStartOfDay();

        Map<ShopOrderStatus, Long> statusCounts = new EnumMap<>(ShopOrderStatus.class);
        for (ShopOrderStatus status : ShopOrderStatus.values()) {
            statusCounts.put(status, orderRepository.countByStatus(status));
        }
        List<StatusSummary> statusRows = statusCounts.entrySet().stream()
                .map(entry -> new StatusSummary(entry.getKey(), entry.getValue()))
                .toList();

        model.addAttribute("revenueTotal", orderRepository.sumCompletedRevenue());
        model.addAttribute("revenueToday", completedRevenueBetween(startOfDay, startOfNextDay));
        model.addAttribute("revenueMonth", completedRevenueBetween(startOfMonth, startOfNextMonth));
        model.addAttribute("revenueYear", completedRevenueBetween(startOfYear, startOfNextYear));
        model.addAttribute("completedToday", orderRepository.countByStatusAndCreatedAtBetween(
                ShopOrderStatus.COMPLETED, startOfDay, startOfNextDay));
        model.addAttribute("completedMonth", orderRepository.countByStatusAndCreatedAtBetween(
                ShopOrderStatus.COMPLETED, startOfMonth, startOfNextMonth));
        model.addAttribute("completedYear", orderRepository.countByStatusAndCreatedAtBetween(
                ShopOrderStatus.COMPLETED, startOfYear, startOfNextYear));
        model.addAttribute("orderCount", orderRepository.count());
        model.addAttribute("customerCount", userRepository.countByRole(ShopRole.CUSTOMER));
        model.addAttribute("productCount", productRepository.countByActiveTrue());
        model.addAttribute("lowStock", productRepository.findByQuantityLessThanOrderByQuantityAsc(5));
        model.addAttribute("pendingOrders", statusCounts.get(ShopOrderStatus.PENDING));
        model.addAttribute("statusRows", statusRows);
        model.addAttribute("pendingOrderList", orderRepository.findTop10ByStatusOrderByCreatedAtDesc(ShopOrderStatus.PENDING));
        model.addAttribute("recentOrders", orderRepository.findTop10ByOrderByCreatedAtDesc());
        return "shop/admin/dashboard";
    }

    @GetMapping("/admin/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(ShopCategory::getName)).toList());
        return "shop/admin/categories";
    }

    @PostMapping("/admin/categories")
    public String saveCategory(@RequestParam(required = false) Long id, @RequestParam String name,
            @RequestParam(required = false) String description, @RequestParam(required = false) String active,
            RedirectAttributes redirectAttributes) {
        if (name == null || name.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Tên danh mục không được rỗng");
            return "redirect:/admin/categories";
        }
        var duplicate = categoryRepository.findByNameIgnoreCase(name.trim());
        if (duplicate.isPresent() && (id == null || !duplicate.get().getId().equals(id))) {
            redirectAttributes.addFlashAttribute("error", "Tên danh mục đã tồn tại");
            return "redirect:/admin/categories";
        }
        ShopCategory category = id == null ? new ShopCategory()
                : categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        category.setName(name.trim());
        category.setDescription(description == null ? "" : description.trim());
        category.setActive(active != null || id == null);
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("success", "Đã lưu danh mục");
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ShopCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (productRepository.existsByCategory(category)) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa danh mục còn sản phẩm");
        } else {
            categoryRepository.delete(category);
            redirectAttributes.addFlashAttribute("success", "Đã xóa danh mục");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/products")
    public String products(Model model) {
        model.addAttribute("products", productRepository.findAll().stream()
                .sorted(Comparator.comparing(ShopProduct::getCreatedAt).reversed()).toList());
        return "shop/admin/products";
    }

    @GetMapping("/admin/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ShopProduct());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        return "shop/admin/product-form";
    }

    @GetMapping("/admin/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        model.addAttribute("product", productRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        return "shop/admin/product-form";
    }

    @PostMapping("/admin/products")
    public String saveProduct(@RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) BigDecimal salePrice,
            @RequestParam int quantity,
            @RequestParam Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String active,
            @RequestParam(required = false) String featured,
            @RequestParam(required = false) String newProduct,
            @RequestParam(required = false) String imagePath,
            @RequestParam(required = false) MultipartFile[] images,
            RedirectAttributes redirectAttributes) {
        if (name == null || name.isBlank() || price.compareTo(BigDecimal.ZERO) < 0 || quantity < 0) {
            redirectAttributes.addFlashAttribute("error", "Tên, giá và số lượng không hợp lệ");
            return "redirect:" + (id == null ? "/admin/products/new" : "/admin/products/" + id + "/edit");
        }

        ShopProduct product = id == null ? new ShopProduct()
                : productRepository.findDetailById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        product.setName(name.trim());
        product.setSlug(uniqueSlug(name, product.getId()));
        product.setDescription(description == null ? "" : description.trim());
        product.setPrice(price);
        product.setSalePrice(salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0 ? salePrice : null);
        product.setQuantity(quantity);
        product.setCategory(categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục không tồn tại")));
        product.setBrand(brandId == null ? null : brandRepository.findById(brandId).orElse(null));
        product.setActive(active != null || id == null);
        product.setFeatured(featured != null);
        product.setNewProduct(newProduct != null || id == null);
        product.setSaleProduct(product.isOnSale());

        try {
            attachImages(product, imagePath, images);
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể lưu ảnh: " + ex.getMessage());
            return "redirect:" + (id == null ? "/admin/products/new" : "/admin/products/" + id + "/edit");
        }

        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Đã lưu sản phẩm");
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/delete")
    public String disableProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ShopProduct product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        product.setActive(false);
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Đã tắt sản phẩm");
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/orders")
    public String adminOrders(@RequestParam(required = false) ShopOrderStatus status, Model model) {
        ShopUser currentUser = currentUserService.requireUser();
        var baseOrders = currentUser.getRole().canViewAllOrders()
                ? orderRepository.findAll()
                : orderRepository.findAll().stream()
                        .filter(order -> canStaffManageOrder(currentUser, order))
                        .toList();
        var orders = baseOrders.stream()
                .filter(order -> status == null || order.getStatus() == status)
                .sorted(Comparator.comparing(order -> order.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", availableOrderStatuses(currentUser));
        model.addAttribute("status", status);
        model.addAttribute("canViewAllOrders", currentUser.getRole().canViewAllOrders());
        model.addAttribute("canCancelOrders", currentUser.getRole().canCancelOrders());
        model.addAttribute("orderPageTitle", currentUser.getRole() == ShopRole.STAFF ? "Đơn hàng khách hàng" : "Đơn hàng");
        return "shop/admin/orders";
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam ShopOrderStatus status,
            RedirectAttributes redirectAttributes) {
        ShopUser currentUser = currentUserService.requireUser();
        var order = orderRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!currentUser.getRole().canViewAllOrders() && !canStaffManageOrder(currentUser, order)) {
            redirectAttributes.addFlashAttribute("error", "Nhân viên chỉ được cập nhật đơn của khách hàng");
            return "redirect:/admin/orders";
        }
        if (status == ShopOrderStatus.CANCELLED && !currentUser.getRole().canCancelOrders()) {
            redirectAttributes.addFlashAttribute("error", "Nhân viên không được hủy đơn hàng");
            return "redirect:/admin/orders";
        }
        try {
            orderService.moveToStatus(order, status);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái đơn");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/admin/orders/{id}/payment/confirm")
    public String confirmManualBankPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ShopUser currentUser = currentUserService.requireUser();
        if (!currentUser.getRole().canViewAllOrders()) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xác nhận thanh toán");
            return "redirect:/admin/orders";
        }
        var order = orderRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            orderService.confirmManualBankPayment(order);
            redirectAttributes.addFlashAttribute("success", "Đã xác nhận đã nhận tiền chuyển khoản");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findByRole(ShopRole.CUSTOMER).stream()
                .sorted(Comparator.comparing(ShopUser::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList());
        return "shop/admin/users";
    }

    @PostMapping("/admin/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (user.getRole() != ShopRole.CUSTOMER) {
            redirectAttributes.addFlashAttribute("error", "Trang khách hàng chỉ thao tác tài khoản khách hàng");
            return "redirect:/admin/users";
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái tài khoản");
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/staff")
    public String staff(Model model) {
        ShopUser currentUser = currentUserService.requireUser();
        model.addAttribute("staffUsers", visibleStaffUsers(currentUser).stream()
                .sorted(Comparator.comparing(ShopUser::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList());
        model.addAttribute("staffRoles", assignableEmployeeRoles(currentUser));
        model.addAttribute("canAssignStaffRoles", currentUser.getRole().canAssignRoles());
        return "shop/admin/staff";
    }

    @PostMapping("/admin/staff")
    public String createStaff(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) ShopRole role,
            RedirectAttributes redirectAttributes) {
        ShopUser currentUser = currentUserService.requireUser();
        ShopRole newRole = role == null ? ShopRole.STAFF : role;
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedPhone = phone == null ? "" : phone.trim();

        String error = validateStaff(normalizedUsername, normalizedEmail, password, fullName, normalizedPhone, newRole,
                assignableEmployeeRoles(currentUser));
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/staff";
        }
        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            redirectAttributes.addFlashAttribute("error", "Username đã tồn tại");
            return "redirect:/admin/staff";
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại");
            return "redirect:/admin/staff";
        }
        if (userRepository.existsByPhone(normalizedPhone)) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại đã tồn tại");
            return "redirect:/admin/staff";
        }

        ShopUser user = new ShopUser();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName.trim());
        user.setPhone(normalizedPhone);
        user.setAddress(address == null || address.isBlank() ? null : address.trim());
        user.setRole(newRole);
        user.setEnabled(true);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Đã tạo tài khoản nhân viên");
        return "redirect:/admin/staff";
    }

    @PostMapping("/admin/staff/{id}/toggle")
    public String toggleStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ShopUser currentUser = currentUserService.requireUser();
        var user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!canManageStaffTarget(currentUser, user)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền khóa hoặc mở khóa tài khoản này");
            return "redirect:/admin/staff";
        }
        if (user.getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không được khóa tài khoản đang đăng nhập");
            return "redirect:/admin/staff";
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái nhân viên");
        return "redirect:/admin/staff";
    }

    @PostMapping("/admin/staff/{id}/role")
    public String updateStaffRole(@PathVariable Long id, @RequestParam ShopRole role, RedirectAttributes redirectAttributes) {
        ShopUser currentUser = currentUserService.requireUser();
        var user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!currentUser.getRole().canAssignRoles()) {
            redirectAttributes.addFlashAttribute("error", "Chỉ quản trị viên được phân quyền người dùng");
            return "redirect:/admin/staff";
        }
        if (!canManageStaffTarget(currentUser, user)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền đổi quyền tài khoản này");
            return "redirect:/admin/staff";
        }
        if (!assignableEmployeeRoles(currentUser).contains(role)) {
            redirectAttributes.addFlashAttribute("error", "Vai trò nhân viên không hợp lệ");
            return "redirect:/admin/staff";
        }
        if (user.getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không được đổi quyền tài khoản đang đăng nhập");
            return "redirect:/admin/staff";
        }
        user.setRole(role);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật quyền nhân viên");
        return "redirect:/admin/staff";
    }

    @GetMapping("/admin/coupons")
    public String coupons(Model model) {
        model.addAttribute("coupons", couponRepository.findAll());
        model.addAttribute("coupon", new ShopCoupon());
        model.addAttribute("discountTypes", DiscountType.values());
        model.addAttribute("users", userRepository.findByRole(ShopRole.CUSTOMER));
        return "shop/admin/coupons";
    }

    @PostMapping("/admin/coupons")
    public String saveCoupon(@RequestParam(required = false) Long id,
            @RequestParam String code,
            @RequestParam DiscountType discountType,
            @RequestParam BigDecimal discountValue,
            @RequestParam BigDecimal minOrderAmount,
            @RequestParam(required = false) BigDecimal maxDiscountAmount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Integer usageLimit,
            @RequestParam(required = false) String active,
            @RequestParam(required = false) String publicCoupon,
            RedirectAttributes redirectAttributes) {
        if (code == null || code.isBlank() || discountValue.compareTo(BigDecimal.ZERO) <= 0
                || minOrderAmount.compareTo(BigDecimal.ZERO) < 0 || endDate.isBefore(startDate)) {
            redirectAttributes.addFlashAttribute("error", "Thông tin coupon không hợp lệ");
            return "redirect:/admin/coupons";
        }
        var duplicate = couponRepository.findByCodeIgnoreCase(code.trim());
        if (duplicate.isPresent() && (id == null || !duplicate.get().getId().equals(id))) {
            redirectAttributes.addFlashAttribute("error", "Mã coupon đã tồn tại");
            return "redirect:/admin/coupons";
        }
        ShopCoupon coupon = id == null ? new ShopCoupon()
                : couponRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        coupon.setCode(code.trim().toUpperCase());
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(discountValue);
        coupon.setMinOrderAmount(minOrderAmount);
        coupon.setMaxDiscountAmount(maxDiscountAmount);
        coupon.setStartDate(startDate);
        coupon.setEndDate(endDate);
        coupon.setUsageLimit(usageLimit);
        coupon.setActive(active != null || id == null);
        coupon.setPublicCoupon(publicCoupon != null);
        couponRepository.save(coupon);
        redirectAttributes.addFlashAttribute("success", "Đã lưu coupon");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/admin/coupons/{id}/assign")
    public String assignCoupon(@PathVariable Long id, @RequestParam Long userId, RedirectAttributes redirectAttributes) {
        var coupon = couponRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!userCouponRepository.existsByUserAndCoupon(user, coupon)) {
            ShopUserCoupon assigned = new ShopUserCoupon();
            assigned.setUser(user);
            assigned.setCoupon(coupon);
            userCouponRepository.save(assigned);
        }
        redirectAttributes.addFlashAttribute("success", "Đã gán mã cho khách hàng");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/admin/coupons/{id}/toggle")
    public String toggleCoupon(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var coupon = couponRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái coupon");
        return "redirect:/admin/coupons";
    }

    @GetMapping("/admin/reviews")
    public String reviews(Model model) {
        model.addAttribute("reviews", reviewRepository.findAllByOrderByCreatedAtDesc());
        return "shop/admin/reviews";
    }

    @PostMapping("/admin/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id) {
        var review = reviewRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        review.setApproved(true);
        reviewRepository.save(review);
        return "redirect:/admin/reviews";
    }

    @PostMapping("/admin/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/admin/reviews";
    }

    @GetMapping("/admin/inventory")
    public String inventory(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("lowStock", productRepository.findByQuantityLessThanOrderByQuantityAsc(5));
        model.addAttribute("histories", inventoryHistoryRepository.findTop50ByOrderByCreatedAtDesc());
        return "shop/admin/inventory";
    }

    @PostMapping("/admin/inventory/import")
    public String importStock(@RequestParam Long productId, @RequestParam int quantity, @RequestParam(required = false) String note,
            RedirectAttributes redirectAttributes) {
        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Số lượng nhập phải lớn hơn 0");
            return "redirect:/admin/inventory";
        }
        ShopProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);
        ShopInventoryHistory history = new ShopInventoryHistory();
        history.setProduct(product);
        history.setType(InventoryHistoryType.IMPORT);
        history.setQuantity(quantity);
        history.setNote(note == null || note.isBlank() ? "Admin nhập thêm hàng" : note.trim());
        inventoryHistoryRepository.save(history);
        redirectAttributes.addFlashAttribute("success", "Đã nhập thêm tồn kho");
        return "redirect:/admin/inventory";
    }

    private String validateStaff(String username, String email, String password, String fullName, String phone,
            ShopRole role, List<ShopRole> assignableRoles) {
        if (username == null || username.length() < 3) {
            return "Username nhân viên tối thiểu 3 ký tự";
        }
        if (email == null || !EMAIL.matcher(email).matches()) {
            return "Email nhân viên không hợp lệ";
        }
        if (phone == null || !PHONE.matcher(phone).matches()) {
            return "Số điện thoại nhân viên phải gồm 10-11 chữ số";
        }
        if (password == null || password.length() < 6) {
            return "Mật khẩu nhân viên tối thiểu 6 ký tự";
        }
        if (fullName == null || fullName.isBlank()) {
            return "Họ tên nhân viên không được rỗng";
        }
        if (!assignableRoles.contains(role)) {
            return "Vai trò nhân viên không hợp lệ";
        }
        return null;
    }

    private List<ShopRole> assignableEmployeeRoles(ShopUser currentUser) {
        if (currentUser.getRole().canAssignRoles()) {
            return ADMIN_ASSIGNABLE_EMPLOYEE_ROLES;
        }
        if (currentUser.getRole() == ShopRole.MANAGER) {
            return MANAGER_ASSIGNABLE_EMPLOYEE_ROLES;
        }
        return List.of();
    }

    private List<ShopUser> visibleStaffUsers(ShopUser currentUser) {
        if (currentUser.getRole().canAssignRoles()) {
            return userRepository.findByRoleIn(EMPLOYEE_ROLES);
        }
        return userRepository.findByRole(ShopRole.STAFF);
    }

    private boolean canManageStaffTarget(ShopUser currentUser, ShopUser targetUser) {
        if (targetUser.getRole() == null || !targetUser.getRole().isEmployee()) {
            return false;
        }
        if (currentUser.getRole() == ShopRole.MANAGER) {
            return targetUser.getRole() == ShopRole.STAFF;
        }
        if (currentUser.getRole().canAssignRoles()) {
            return targetUser.getRole() != ShopRole.SUPER_ADMIN;
        }
        return false;
    }

    private boolean canStaffManageOrder(ShopUser currentUser, ShopOrder order) {
        if (currentUser.getRole() != ShopRole.STAFF) {
            return order.getUser().getId().equals(currentUser.getId());
        }
        return order.getUser().getRole() == ShopRole.CUSTOMER || order.getUser().getId().equals(currentUser.getId());
    }

    private ShopOrderStatus[] availableOrderStatuses(ShopUser currentUser) {
        if (currentUser.getRole().canCancelOrders()) {
            return ShopOrderStatus.values();
        }
        return java.util.Arrays.stream(ShopOrderStatus.values())
                .filter(status -> status != ShopOrderStatus.CANCELLED)
                .toArray(ShopOrderStatus[]::new);
    }

    private String uniqueSlug(String name, Long currentId) {
        String base = ShopSlugService.slugify(name);
        String slug = base;
        int suffix = 2;
        while (true) {
            var found = productRepository.findBySlug(slug);
            if (found.isEmpty() || found.get().getId().equals(currentId)) {
                return slug;
            }
            slug = base + "-" + suffix++;
        }
    }

    private void attachImages(ShopProduct product, String imagePath, MultipartFile[] images) throws IOException {
        boolean hasExistingImages = !product.getImages().isEmpty();
        if (imagePath != null && !imagePath.isBlank()) {
            ShopProductImage image = new ShopProductImage();
            image.setProduct(product);
            image.setImagePath(imagePath.trim());
            image.setMainImage(!hasExistingImages);
            product.getImages().add(image);
            hasExistingImages = true;
        }
        if (images == null) {
            return;
        }
        Path uploadDir = Path.of("uploads", "products");
        Files.createDirectories(uploadDir);
        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename().replaceAll("[^A-Za-z0-9._-]", "_");
            String filename = UUID.randomUUID() + "_" + original;
            Path target = uploadDir.resolve(filename);
            file.transferTo(target);

            ShopProductImage image = new ShopProductImage();
            image.setProduct(product);
            image.setImagePath("/uploads/products/" + filename);
            image.setMainImage(!hasExistingImages);
            product.getImages().add(image);
            hasExistingImages = true;
        }
    }

    private BigDecimal completedRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return orderRepository.sumRevenueByStatusAndCreatedAtBetween(ShopOrderStatus.COMPLETED, start, end);
    }

    public record StatusSummary(ShopOrderStatus status, long count) {
    }
}
