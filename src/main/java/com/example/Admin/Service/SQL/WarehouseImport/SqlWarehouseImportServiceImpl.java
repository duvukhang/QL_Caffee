package com.example.Admin.Service.SQL.WarehouseImport;

import com.example.Admin.DTOS.Request.HoaDonRequest;
import com.example.Admin.Models.Inventoryrecord;
import com.example.Admin.Repositories.InventoryRecordRepository;
// IMPORT THƯ VIỆN CỦA MICROSOFT ĐỂ DÙNG BẢNG (TVP)
import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

@Service
public class SqlWarehouseImportServiceImpl implements SqlWarehouseImportService {

    private final JdbcTemplate jdbcTemplate;
    private final InventoryRecordRepository inventoryRecordRepository;

    public SqlWarehouseImportServiceImpl(JdbcTemplate jdbcTemplate, InventoryRecordRepository inventoryRecordRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.inventoryRecordRepository = inventoryRecordRepository;
    }

    // ĐÃ FIX: Đổi tên thành CreateInventoryRecords và thêm throws Exception để khớp 100% với Interface
    @Override
    @Transactional
    public Inventoryrecord CreateInventoryRecords(List<HoaDonRequest.ProductItem> dsNL, int inventoryId, int recordsType) throws Exception {
        if (dsNL == null || dsNL.isEmpty()) {
            throw new IllegalArgumentException("Need List Good to create Order");
        }

        String recordsId = generateId("PH");

        try {
            // Sử dụng JdbcTemplate để can thiệp sâu vào JDBC Connection
            jdbcTemplate.execute((Connection con) -> {
                
                // 1. Tạo SQLServerDataTable để thay thế cho hàm TaoBangThamSoSanPham của C#
                SQLServerDataTable dataTable = new SQLServerDataTable();
                
                // LƯU Ý QUAN TRỌNG: Tên cột phải KHỚP CHÍNH XÁC với định nghĩa của TYPE [dbo].[DetailType] dưới SQL Server
                dataTable.addColumnMetadata("ProductId", Types.CHAR); 
                dataTable.addColumnMetadata("Quantity", Types.INTEGER);

                // Đổ dữ liệu nguyên liệu từ Request vào bảng
                for (HoaDonRequest.ProductItem item : dsNL) {
                    dataTable.addRow(item.getMasp(), item.getSoLuong());
                }

                // 2. Chuẩn bị gọi Stored Procedure có Return Value
                String sql = "{ ? = call management.usp_CreateInventoryRecords(?, ?, ?, ?) }";
                CallableStatement cs = con.prepareCall(sql);

                // Tham số đầu ra Return Value (Tương đương ParameterDirection.ReturnValue)
                cs.registerOutParameter(1, Types.INTEGER); 
                
                // Tham số đầu vào (Input Parameters)
                cs.setString(2, recordsId);               // @InventoryIdRecordId
                cs.setInt(3, inventoryId);                // @inventoryId
                cs.setInt(4, recordsType);                // @TypeID

                // 3. Unwrap CallableStatement để truyền tham số mảng (Structured Type) của Microsoft
                if (cs.isWrapperFor(SQLServerCallableStatement.class)) {
                    SQLServerCallableStatement sqlServerCs = cs.unwrap(SQLServerCallableStatement.class);
                    sqlServerCs.setStructured(5, "dbo.DetailType", dataTable); // @ListGoods
                }

                return cs;
            }, (CallableStatement cs) -> {
                // 4. Thực thi thủ tục
                cs.execute();
                return null;
            });
        } catch (Exception e) {
            throw new Exception("Lỗi khi thực thi Stored Procedure nhập kho: " + e.getMessage());
        }

        // 5. Trả về kết quả nếu tạo thành công
        return inventoryRecordRepository.findById(recordsId)
                .orElseThrow(() -> new IllegalArgumentException("Mã phiếu nhập bị trùng hoặc phiếu nhập không tồn tại dưới DB"));
    }

    private String generateId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}