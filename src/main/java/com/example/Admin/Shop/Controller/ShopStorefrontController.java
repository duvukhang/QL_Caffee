package com.example.Admin.Shop.Controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Admin.Shop.Model.ShopProduct;
import com.example.Admin.Shop.Repository.ShopBrandRepository;
import com.example.Admin.Shop.Repository.ShopCategoryRepository;
import com.example.Admin.Shop.Repository.ShopCouponRepository;
import com.example.Admin.Shop.Repository.ShopProductRepository;
import com.example.Admin.Shop.Repository.ShopReviewRepository;
import com.example.Admin.Shop.Repository.ShopUserCouponRepository;
import com.example.Admin.Shop.Service.ShopCatalogService;
import com.example.Admin.Shop.Service.ShopCurrentUserService;

@Controller
public class ShopStorefrontController {
    private final ShopProductRepository productRepository;
    private final ShopCategoryRepository categoryRepository;
    private final ShopBrandRepository brandRepository;
    private final ShopReviewRepository reviewRepository;
    private final ShopCouponRepository couponRepository;
    private final ShopUserCouponRepository userCouponRepository;
    private final ShopCatalogService catalogService;
    private final ShopCurrentUserService currentUserService;

    public ShopStorefrontController(ShopProductRepository productRepository, ShopCategoryRepository categoryRepository,
            ShopBrandRepository brandRepository, ShopReviewRepository reviewRepository, ShopCouponRepository couponRepository,
            ShopUserCouponRepository userCouponRepository, ShopCatalogService catalogService,
            ShopCurrentUserService currentUserService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.reviewRepository = reviewRepository;
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.catalogService = catalogService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/")
    public String home(Model model) {
        var newProducts = productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
        var featuredProducts = productRepository.findTop8ByActiveTrueAndFeaturedTrueOrderByCreatedAtDesc();
        var saleProducts = productRepository.findTop8ByActiveTrueAndSaleProductTrueOrderByCreatedAtDesc();
        var productsForRatings = new ArrayList<ShopProduct>();
        productsForRatings.addAll(newProducts);
        productsForRatings.addAll(featuredProducts);
        productsForRatings.addAll(saleProducts);

        model.addAttribute("categories", categoryRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("newProducts", newProducts);
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("saleProducts", saleProducts);
        addProductRatings(model, productsForRatings);
        return "shop/home";
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        var products = catalogService.search(q, categoryId, brandId, minPrice, maxPrice, sort, page, 12);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        addProductRatings(model, products.getContent());
        return "shop/products";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        var product = productRepository.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        long reviewCount = reviewRepository.countByProductAndApprovedTrue(product);
        Double averageRating = reviewRepository.averageApprovedRatingByProduct(product);
        model.addAttribute("product", product);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("averageRating", averageRating == null ? 0.0 : averageRating);
        model.addAttribute("reviews", reviewRepository.findByProductAndApprovedTrueOrderByCreatedAtDesc(product));
        return "shop/product-detail";
    }

    private void addProductRatings(Model model, Collection<ShopProduct> products) {
        var productIds = products.stream()
                .map(ShopProduct::getId)
                .distinct()
                .toList();
        var ratingByProductId = new HashMap<Long, Double>();
        var reviewCountByProductId = new HashMap<Long, Long>();
        if (!productIds.isEmpty()) {
            reviewRepository.summarizeApprovedRatings(productIds).forEach(summary -> {
                ratingByProductId.put(summary.getProductId(), summary.getAverageRating());
                reviewCountByProductId.put(summary.getProductId(), summary.getReviewCount());
            });
        }
        model.addAttribute("ratingByProductId", ratingByProductId);
        model.addAttribute("reviewCountByProductId", reviewCountByProductId);
    }

    @GetMapping("/my-coupons")
    public String myCoupons(Model model) {
        var user = currentUserService.requireUser();
        model.addAttribute("publicCoupons", couponRepository.findByActiveTrueOrderByCodeAsc().stream()
                .filter(coupon -> coupon.isPublicCoupon()).toList());
        model.addAttribute("assignedCoupons", userCouponRepository.findByUserOrderByAssignedAtDesc(user));
        return "shop/my-coupons";
    }
}
