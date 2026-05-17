package com.example.demo.Controller.Public;

import com.example.demo.Models.Product;
import com.example.demo.Repositories.ProductRepository;
import com.example.demo.Repositories.CategoryRepository;
import com.example.demo.Repositories.GoodRepository;
import com.example.demo.Repositories.StoreRepository;
import com.example.demo.Repositories.SysRoleRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 1. GET /public/Product/{pageNum}/{pageSize}
    @GetMapping("/Product/{pageNum}/{pageSize}")
    public ResponseEntity<?> getProduct(@PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize) {
        if (pageNum < 1) {
            throw new IllegalArgumentException("PageNum param is Out Of Range"); // Khớp 1-1 C#
        }
        if (pageSize > 100 || pageSize < 0) {
            throw new IllegalArgumentException("Max page size is 100"); // Khớp 1-1 C#
        }

        List<Product> allProducts = productRepository.findAll();
        
        // .OrderBy(x => x.ProductName)
        List<Product> sorted = allProducts.stream()
                .sorted((p1, p2) -> {
                    String name1 = p1.getProductName() != null ? p1.getProductName() : "";
                    String name2 = p2.getProductName() != null ? p2.getProductName() : "";
                    return name1.compareTo(name2);
                }).toList();

        // .Skip((pageNum - 1) * pageSize).Take(pageSize)
        int skip = (pageNum - 1) * pageSize;
        List<Product> paged = sorted.stream().skip(skip).limit(pageSize).toList();

        if (paged.isEmpty()) {
            return ResponseEntity.noContent().build(); // Return NoContent()
        }

        // Đóng gói cấu trúc dạng Item<Product> gồm Value và PathChiTiet
        List<Map<String, Object>> responseItems = new ArrayList<>();
        paged.forEach(p -> {
            Map<String, Object> itemNew = new HashMap<>();
            itemNew.put("Value", p);
            itemNew.put("PathChiTiet", "/ChiTiet/" + p.getProductId()); // Giữ đúng định dạng đường dẫn gốc
            responseItems.add(itemNew);
        });

        // Khởi tạo đối tượng PageRespone trả về
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
        
        // Lọc sản phẩm bọc lót thông minh qua cả 2 cách định nghĩa model (Trường ID chuỗi đơn hoặc Object SubCategory)
        List<Product> filtered = allProducts.stream()
                .filter(p -> {
                    try {
                        // Thử khả năng 1: Đọc trực tiếp biến chuỗi subcategoryId / subCategoryid
                        try {
                            String id = (String) p.getClass().getMethod("getSubcategoryId").invoke(p);
                            if (id != null) return id.equals(cateId);
                        } catch (Exception e) {
                            try {
                                String id = (String) p.getClass().getMethod("getSubcategoryid").invoke(p);
                                if (id != null) return id.equals(cateId);
                            } catch (Exception ex) {}
                        }

                        // Thử khả năng 2: Đi đường vòng nếu dự án map dạng đối tượng Object SubCategory liên kết
                        Object sub = null;
                        try { sub = p.getClass().getMethod("getSubcategory").invoke(p); } 
                        catch (Exception e) { sub = p.getClass().getMethod("getSubCategory").invoke(p); }
                        
                        if (sub != null) {
                            try {
                                return cateId.equals(sub.getClass().getMethod("getSubcategoryid").invoke(sub));
                            } catch (Exception e1) {
                                return cateId.equals(sub.getClass().getMethod("getSubCategoryId").invoke(sub));
                            }
                        }
                    } catch (Exception e) {}
                    return false;
                })
                .sorted((p1, p2) -> {
                    String name1 = p1.getProductName() != null ? p1.getProductName() : "";
                    String name2 = p2.getProductName() != null ? p2.getProductName() : "";
                    return name1.compareTo(name2);
                }).toList();

        if (filtered.isEmpty()) {
            throw new RuntimeException("Can't find any Product"); // Khớp lỗi KeyNotFoundException
        }

        int skip = (pageNum - 1) * pageSize;
        List<Product> paged = filtered.stream().skip(skip).limit(pageSize).toList();

        List<Map<String, Object>> responseItems = new ArrayList<>();
        paged.forEach(p -> {
            Map<String, Object> itemNew = new HashMap<>();
            itemNew.put("Value", p);
            itemNew.put("PathChiTiet", "/public/Product/" + p.getProductId());
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
        Product product = productRepository.findById(masp)
                .orElseThrow(() -> new RuntimeException("Product not exists")); // Khớp 1-1 thông điệp lỗi C#
        return ResponseEntity.ok(product);
    }

    // 4. GET /public/DanhMuc
    @GetMapping("/DanhMuc")
    public ResponseEntity<?> getAllCate() {
        List<?> danhmucCha = categoryRepository.findAll();
        if (danhmucCha.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<Map<String, Object>> danhMucRespones = new ArrayList<>();
        
        danhmucCha.forEach(cha -> {
            Map<String, Object> chaMap = new HashMap<>();
            try {
                String categoryId = (String) cha.getClass().getMethod("getCategoryId").invoke(cha);
                String categoryName = (String) cha.getClass().getMethod("getCategoryName").invoke(cha);
                chaMap.put("MaLoaiDm", categoryId);
                chaMap.put("ten", categoryName);

                // Đọc danh mục con tương đương .Include(cate => cate.SubCategories) bên C#
                java.util.Collection<?> subCategories = null;
                try {
                    subCategories = (java.util.Collection<?>) cha.getClass().getMethod("getSubCategories").invoke(cha);
                } catch (Exception es) {
                    subCategories = (java.util.Collection<?>) cha.getClass().getMethod("getSubcategories").invoke(cha);
                }

                List<Map<String, Object>> subList = new ArrayList<>();
                if (subCategories != null) {
                    subCategories.forEach(con -> {
                        Map<String, Object> conMap = new HashMap<>();
                        try {
                            String subName = (String) con.getClass().getMethod("getSubCategoryName").invoke(con);
                            String subId = (String) con.getClass().getMethod("getSubCategoryId").invoke(con);
                            conMap.put("tenDM", subName);
                            conMap.put("MaDm", subId);
                        } catch (Exception e) {
                            try {
                                String subName = (String) con.getClass().getMethod("getSubcategoryname").invoke(con);
                                String subId = (String) con.getClass().getMethod("getSubcategoryid").invoke(con);
                                conMap.put("tenDM", subName);
                                conMap.put("MaDm", subId);
                            } catch (Exception ex) {}
                        }
                        subList.add(conMap);
                    });
                }
                chaMap.put("danhMucCon", subList);
                danhMucRespones.add(chaMap);
            } catch (Exception e) {}
        });

        return ResponseEntity.ok(danhMucRespones);
    }

    // 5. GET /public/NL
    @GetMapping("/NL")
    public ResponseEntity<?> allNl() {
        return ResponseEntity.ok(goodRepository.findAll());
    }

    // 6. GET /public/Store
    @GetMapping("/Store")
    public ResponseEntity<?> getStore() {
        List<?> stores = storeRepository.findAll();
        if (stores.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<Map<String, Object>> ls = new ArrayList<>();
        stores.forEach(s -> {
            Map<String, Object> storeMap = new HashMap<>();
            try {
                storeMap.put("StoreId", s.getClass().getMethod("getStoreId").invoke(s));
                storeMap.put("StoreName", s.getClass().getMethod("getStoreName").invoke(s));
            } catch (Exception e) {
                try {
                    storeMap.put("StoreId", s.getClass().getMethod("getStoreid").invoke(s));
                    storeMap.put("StoreName", s.getClass().getMethod("getStorename").invoke(s));
                } catch (Exception ex) {}
            }
            ls.add(storeMap);
        });

        return ResponseEntity.ok(ls);
    }

    // 7. GET /public/Roles
    @GetMapping("/Roles")
    public ResponseEntity<?> allRole() {
        List<?> sysroles = sysRoleRepository.findAll();
        List<Map<String, Object>> respone = new ArrayList<>();

        sysroles.forEach(role -> {
            try {
                String roleName = (String) role.getClass().getMethod("getRoleName").invoke(role);
                // Loại trừ tài khoản quyền tối cao: Where(r => r.RoleName != "Admin")
                if (roleName != null && !"Admin".equalsIgnoreCase(roleName)) {
                    Map<String, Object> roleMap = new HashMap<>();
                    String roleId = (String) role.getClass().getMethod("getRoleId").invoke(role);
                    roleMap.put("roleId", roleId);
                    roleMap.put("roleName", roleName);
                    respone.add(roleMap);
                }
            } catch (Exception e) {
                try {
                    String roleName = (String) role.getClass().getMethod("getRolename").invoke(role);
                    if (roleName != null && !"Admin".equalsIgnoreCase(roleName)) {
                        Map<String, Object> roleMap = new HashMap<>();
                        String roleId = (String) role.getClass().getMethod("getRoleid").invoke(role);
                        roleMap.put("roleId", roleId);
                        roleMap.put("roleName", roleName);
                        respone.add(roleMap);
                    }
                } catch (Exception ex) {}
            }
        });

        if (respone.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(respone);
    }
}