package com.example.Admin.Service.SQL.Ingredient;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Admin.Models.Good;
import com.example.Admin.Repositories.GoodRepository;

import java.util.UUID;

@Service
public class SqlIngredientServicelImpl implements SqlIngredientService {

    private final EntityManager entityManager;
    private final GoodRepository goodRepository;

    public SqlIngredientServicelImpl(EntityManager entityManager, GoodRepository goodRepository) {
        this.entityManager = entityManager;
        this.goodRepository = goodRepository;
    }

    @Override
    @Transactional
    public Good taoNguyenLieu(String tenNL, String dvt) {
        String maNL = generateId("NL");

        // Gọi Stored Procedure
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("management.usp_CreateNewGoods");
        query.registerStoredProcedureParameter("GoodId", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("UnitName", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("GoodsName", String.class, ParameterMode.IN);
        
        // Nhận Return Value
        query.registerStoredProcedureParameter("ReturnValue", Integer.class, ParameterMode.OUT);

        query.setParameter("GoodId", maNL);
        query.setParameter("UnitName", dvt);
        query.setParameter("GoodsName", tenNL);

        query.execute();

        Integer returnValue = (Integer) query.getOutputParameterValue("ReturnValue");

        if (returnValue != null) {
            return goodRepository.findById(maNL)
                    .orElseThrow(() -> new RuntimeException("Lỗi truy xuất nguyên liệu sau khi tạo"));
        }
        throw new IllegalArgumentException("NguyenLieu Exists in DB can't create new");
    }

    private String generateId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}