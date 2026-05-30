package com.example.Admin.Controller.CustomerSite;

import com.example.Admin.Models.Product;
import com.example.Admin.Repositories.ProductRepository;
import com.example.Admin.Repositories.StoreRepository;
import com.example.Admin.Repositories.SubCategoryRepository;
import com.example.Admin.Util.VietnameseHelper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Controller
public class CustomerHomeController {

    private static final String SUPPLY_SUBCATEGORY_ID = "DM00000012";

    private final ProductRepository productRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final StoreRepository storeRepository;

    public CustomerHomeController(
            ProductRepository productRepository,
            SubCategoryRepository subCategoryRepository,
            StoreRepository storeRepository
    ) {
        this.productRepository = productRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.storeRepository = storeRepository;
    }

    @GetMapping({"/", "/home", "/home/index"})
    public String index(
            @RequestParam(required = false) String maDm,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String[] selectedTypes,
            Model model
    ) {
        model.addAttribute("sliderProducts", productRepository.findTop3ByOrderByPriceDesc());

        List<Product> products = productRepository.findAllWithCategory().stream()
                .filter(product -> product.getSubcategoryId() == null || !product.getSubcategoryId().equals(SUPPLY_SUBCATEGORY_ID))
                .toList();

        String currentFilter = "T\u1ea5t c\u1ea3 s\u1ea3n ph\u1ea9m";
        if (maDm != null && !maDm.isBlank()) {
            String value = maDm.trim();
            products = products.stream()
                    .filter(product -> product.getSubcategoryId() != null && product.getSubcategoryId().contains(value))
                    .toList();
            currentFilter = subCategoryRepository.findFirstBySubCategoryContaining(value)
                    .map(subCategory -> subCategory.getSubCategoryName())
                    .orElse(value);
        }

        if (keyword != null && !keyword.isBlank()) {
            String key = VietnameseHelper.removeSign(keyword.trim()).toLowerCase();
            products = products.stream()
                    .filter(product -> VietnameseHelper.removeSign(product.getProductName()).toLowerCase().contains(key))
                    .toList();
            currentFilter = "K\u1ebft qu\u1ea3 t\u00ecm ki\u1ebfm: '" + keyword.trim() + "'";
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(product -> product.getPrice() != null && product.getPrice().compareTo(minPrice) >= 0)
                    .toList();
        }
        if (maxPrice != null) {
            products = products.stream()
                    .filter(product -> product.getPrice() != null && product.getPrice().compareTo(maxPrice) <= 0)
                    .toList();
        }

        if (selectedTypes != null && selectedTypes.length > 0) {
            List<String> types = Arrays.asList(selectedTypes);
            products = products.stream()
                    .filter(product -> product.getSubcategory() != null && types.stream().anyMatch(type ->
                            product.getSubcategory().getSubCategoryName().contains(type)
                                    || (product.getSubcategory().getCategory() != null
                                    && product.getSubcategory().getCategory().getCategoryName().contains(type))
                    ))
                    .toList();
        }

        if ("price_asc".equals(sortOrder)) {
            products = products.stream().sorted(Comparator.comparing(Product::getPrice)).toList();
        } else if ("price_desc".equals(sortOrder)) {
            products = products.stream().sorted(Comparator.comparing(Product::getPrice).reversed()).toList();
        } else if ("popular".equals(sortOrder)) {
            products = products.stream()
                    .sorted(Comparator.comparing((Product product) ->
                            product.getOrderDetails() == null ? 0 : product.getOrderDetails().size()).reversed())
                    .toList();
        } else {
            products = products.stream().sorted(Comparator.comparing(Product::getProductId).reversed()).toList();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", subCategoryRepository.findAll(Sort.by("subCategoryName")));
        model.addAttribute("selectedTypes", selectedTypes == null ? List.of() : Arrays.asList(selectedTypes));
        model.addAttribute("currentFilter", currentFilter);
        model.addAttribute("minPrice", minPrice == null ? BigDecimal.ZERO : minPrice);
        model.addAttribute("maxPrice", maxPrice == null ? BigDecimal.valueOf(200000) : maxPrice);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("activeMaDm", maDm);
        model.addAttribute("keyword", keyword);
        return "home/index";
    }

    @GetMapping("/home/detail")
    public String detail(@RequestParam String id, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "error/404";
        }

        List<Product> relatedProducts = product.getSubcategoryId() == null
                ? productRepository.findTop4ByOrderByProductIdDesc()
                : productRepository.findTop4BySubcategory_SubCategoryAndProductIdNot(product.getSubcategoryId(), id);
        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        return "home/detail";
    }

    @GetMapping("/home/search")
    public String search(@RequestParam(required = false) String keyword, Model model) {
        return index(null, keyword, null, null, null, null, model);
    }

    @GetMapping("/home/contact")
    public String contact(Model model) {
        model.addAttribute("stores", storeRepository.findAll());
        return "home/contact";
    }

    @GetMapping("/home/about")
    public String about() {
        return "home/about";
    }

    @GetMapping("/home/privacy")
    public String privacy() {
        return "home/privacy";
    }

    @GetMapping("/home/menu")
    public String menu(Model model) {
        model.addAttribute("categories", subCategoryRepository.findAll(Sort.by("subCategoryName")));
        return "home/menu";
    }
}
