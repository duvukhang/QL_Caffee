package com.example.Admin.Controller.Public;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Admin.Models.Category;
import com.example.Admin.Models.Good;
import com.example.Admin.Models.Product;
import com.example.Admin.Models.Store;
import com.example.Admin.Models.Sysrole;
import com.example.Admin.Repositories.CategoryRepository;
import com.example.Admin.Repositories.GoodRepository;
import com.example.Admin.Repositories.ProductRepository;
import com.example.Admin.Repositories.StoreRepository;
import com.example.Admin.Repositories.SysRoleRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final GoodRepository goodRepository;
    private final StoreRepository storeRepository;
    private final SysRoleRepository sysRoleRepository;

    public PublicController(ProductRepository productRepository, 
                            CategoryRepository categoryRepository,
                            GoodRepository goodRepository, 
                            StoreRepository storeRepository, 
                            SysRoleRepository sysRoleRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.goodRepository = goodRepository;
        this.storeRepository = storeRepository;
        this.sysRoleRepository = sysRoleRepository;
    }

    // 🛠️ HÀM HELPER: Trích xuất dữ liệu Product thành DTO thuần túy để tránh lỗi Lazy Loading
    private Map<String, Object> mapProductToDto(Product p) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("productId", p.getProductId());
        dto.put("productName", p.getProductName());
        dto.put("price", p.getPrice());
        dto.put("img", p.getImg());
        dto.put("status", p.getStatus());
        dto.put("description", p.getDescription());
        
        if (p.getSubcategory() != null) {
            dto.put("subcategoryId", p.getSubcategory().getSubCategory());
            dto.put("subcategoryName", p.getSubcategory().getSubCategoryName());
        }
        return dto;
    }

    // 1. GET /public/Product/{pageNum}/{pageSize}
    @GetMapping("/Product/{pageNum}/{pageSize}")
    public ResponseEntity<?> getProduct(@PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize) {
        if (pageNum < 1) {
            throw new IllegalArgumentException("PageNum param is Out Of Range");
        }
        if (pageSize > 100 || pageSize < 0) {
            throw new IllegalArgumentException("Max page size is 100");
        }

        List<Product> allProducts = productRepository.findAll();
        
        List<Product> sorted = allProducts.stream()
                .sorted((p1, p2) -> {
                    String name1 = p1.getProductName() != null ? p1.getProductName() : "";
                    String name2 = p2.getProductName() != null ? p2.getProductName() : "";
                    return name1.compareTo(name2);
                }).toList();

        int skip = (pageNum - 1) * pageSize;
        List<Product> paged = sorted.stream().skip(skip).limit(pageSize).toList();

        if (paged.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<Map<String, Object>> responseItems = new ArrayList<>();
        paged.forEach(p -> {
            Map<String, Object> itemNew = new HashMap<>();
            // 🛠️ ĐÃ FIX: Sử dụng hàm helper thay vì nhét thẳng thực thể p vào
            itemNew.put("Value", mapProductToDto(p)); 
            itemNew.put("PathChiTiet", "/ChiTiet/" + p.getProductId());
            responseItems.add(itemNew);
        });

        Map<String, Object> res = new HashMap<>();
        res.put("items", responseItems);
        res.put("totalCount", sorted.size());
        res.put("totalPages", (int) Math.ceil((double) sorted.size() / pageSize));
        res.put("pageIndex", pageNum);
        res.put("pageSize", pageSize);

        return ResponseEntity.ok(res);
    }

    // 2. GET /public/ProductByCate/{pageNum}/{pageSize}/{CateId}
    @GetMapping("/ProductByCate/{pageNum}/{pageSize}/{CateId}")
    public ResponseEntity<?> getProductByCate(
            @PathVariable("pageNum") int pageNum, 
            @PathVariable("pageSize") int pageSize,
            @PathVariable("CateId") String cateId) {
        
        if (pageNum < 1) {
            throw new IllegalArgumentException("PageNum param is Out Of Range");
        }
        if (pageSize > 100 || pageSize < 0) {
            throw new IllegalArgumentException("Max page size is 100");
        }

        List<Product> allProducts = productRepository.findAll();
        
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getSubcategory() != null && cateId.equals(p.getSubcategory().getSubCategory()))
                .sorted((p1, p2) -> {
                    String name1 = p1.getProductName() != null ? p1.getProductName() : "";
                    String name2 = p2.getProductName() != null ? p2.getProductName() : "";
                    return name1.compareTo(name2);
                }).toList();

        if (filtered.isEmpty()) {
            throw new RuntimeException("Can't find any Product");
        }

        int skip = (pageNum - 1) * pageSize;
        List<Product> paged = filtered.stream().skip(skip).limit(pageSize).toList();

        List<Map<String, Object>> responseItems = new ArrayList<>();
        paged.forEach(p -> {
            Map<String, Object> itemNew = new HashMap<>();
            itemNew.put("Value", mapProductToDto(p));
            itemNew.put("PathChiTiet", "/public/ProductDetail/" + p.getProductId());
            responseItems.add(itemNew);
        });

        Map<String, Object> res = new HashMap<>();
        res.put("items", responseItems);
        res.put("totalCount", filtered.size());
        res.put("totalPages", (int) Math.ceil((double) filtered.size() / pageSize));
        res.put("pageIndex", pageNum);
        res.put("pageSize", pageSize);

        return ResponseEntity.ok(res);
    }

    // 3. GET /public/ProductDetail/{masp}
    @GetMapping("/ProductDetail/{masp}")
    public ResponseEntity<?> getChiTietProduct(@PathVariable("masp") String masp) {
        Product p = productRepository.findById(masp)
                .orElseThrow(() -> new RuntimeException("Product not exists"));
        
        // 🛠️ ĐÃ FIX: Không trả về Entity p nữa, trả về map an toàn
        return ResponseEntity.ok(mapProductToDto(p));
    }

    // 4. GET /public/DanhMuc
    @GetMapping("/DanhMuc")
    public ResponseEntity<?> getAllCate() {
        List<Category> danhmucCha = categoryRepository.findAll();
        if (danhmucCha.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<Map<String, Object>> danhMucRespones = new ArrayList<>();
        danhmucCha.forEach(cha -> {
            Map<String, Object> chaMap = new HashMap<>();
            chaMap.put("MaLoaiDm", cha.getCategoryId());
            chaMap.put("ten", cha.getCategoryName());

            List<Map<String, Object>> subList = new ArrayList<>();
            if (cha.getSubCategories() != null) {
                cha.getSubCategories().forEach(sub -> {
                    Map<String, Object> conMap = new HashMap<>();
                    conMap.put("tenDM", sub.getSubCategoryName());
                    conMap.put("MaDm", sub.getSubCategory());
                    subList.add(conMap);
                });
            }
            chaMap.put("danhMucCon", subList);
            danhMucRespones.add(chaMap);
        });

        return ResponseEntity.ok(danhMucRespones);
    }

    // 5. GET /public/NL
    @GetMapping("/NL")
    public ResponseEntity<?> allNl() {
        List<Good> goods = goodRepository.findAll();
        if (goods.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Map<String, Object>> response = new ArrayList<>();
        goods.forEach(good -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("goodId", good.getGoodId());
            dto.put("goodName", good.getGoodName());
            dto.put("unitName", good.getUnitName());
            response.add(dto);
        });

        return ResponseEntity.ok(response);
    }

    // 6. GET /public/Store
    @GetMapping("/Store")
    public ResponseEntity<?> getStore() {
        List<Store> stores = storeRepository.findAll();
        if (stores.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<Map<String, Object>> ls = new ArrayList<>();
        stores.forEach(store -> {
            Map<String, Object> storeMap = new HashMap<>();
            storeMap.put("StoreId", store.getStoreId());
            storeMap.put("StoreName", store.getStoreName());
            ls.add(storeMap);
        });

        return ResponseEntity.ok(ls);
    }

    // 7. GET /public/Roles
    @GetMapping("/Roles")
    public ResponseEntity<?> allRole() {
        List<Sysrole> sysroles = sysRoleRepository.findAll();
        List<Map<String, Object>> respone = new ArrayList<>();

        sysroles.forEach(role -> {
            if (role.getRoleName() != null && !"Admin".equalsIgnoreCase(role.getRoleName())) {
                Map<String, Object> roleMap = new HashMap<>();
                roleMap.put("roleId", role.getRoleId());
                roleMap.put("roleName", role.getRoleName());
                respone.add(roleMap);
            }
        });

        if (respone.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(respone);
    }
}