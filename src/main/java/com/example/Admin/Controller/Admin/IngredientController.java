package com.example.Admin.Controller.Admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Admin.Service.SQL.Ingredient.SqlIngredientService;

@RestController
@RequestMapping("/Admin/good")
@PreAuthorize("hasRole('ADMIN')")
public class IngredientController {

    private final SqlIngredientService sqlIngredientService;

    public IngredientController(SqlIngredientService sqlIngredientService) {
        this.sqlIngredientService = sqlIngredientService;
    }

    @PostMapping
    public ResponseEntity<?> createNL(@RequestParam("goods_name") String goodsName, @RequestParam("unit_name") String unitName) {
        if (goodsName == null || goodsName.trim().isEmpty() || unitName == null || unitName.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing Query 'TenNL' or 'DVT'");
        }
        var response = sqlIngredientService.taoNguyenLieu(goodsName, unitName);
        if (response == null) throw new RuntimeException("Can't create NL");
        return ResponseEntity.ok(response);
    }
}