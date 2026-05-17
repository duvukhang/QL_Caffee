package com.example.demo.Service.SQL.Order;

import com.example.demo.DTOS.Request.HoaDonRequest;
import com.example.demo.Models.Order;
import com.example.demo.Repositories.OrderRepository;

import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;

import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

@Service
public class SqlOrderServiceImpl implements SqlOrderService {

    private final JdbcTemplate jdbcTemplate;
    private final OrderRepository orderRepository;

    public SqlOrderServiceImpl(JdbcTemplate jdbcTemplate, OrderRepository orderRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    // ĐÃ FIX: Sắp xếp lại thứ tự tham số cho khớp 100% với OrderController
    public Order taoDon(String makhach, int maNV, List<HoaDonRequest.ProductItem> dssp) {
        String madon = generateId("DH");

        // ĐÃ FIX: Dùng Anonymous Class thay cho Lambda để trị dứt điểm lỗi gạch đỏ
        jdbcTemplate.execute(new CallableStatementCreator() {
            @Override
            public CallableStatement createCallableStatement(Connection con) throws SQLException {
                SQLServerDataTable dataTable = new SQLServerDataTable();
                
                try {
                    dataTable.addColumnMetadata("ProductId", Types.CHAR); 
                    dataTable.addColumnMetadata("Quantity", Types.INTEGER);

                    for (HoaDonRequest.ProductItem item : dssp) {
                        dataTable.addRow(item.getMasp(), item.getSoLuong());
                    }
                } catch (Exception e) {
                    throw new SQLException("Lỗi cấu hình bảng TVP", e);
                }

                String sql = "{ ? = call dbo.usp_CreateOrder(?, ?, ?, ?) }";
                CallableStatement cs = con.prepareCall(sql);

                cs.registerOutParameter(1, Types.INTEGER); 
                cs.setString(2, makhach);                  
                cs.setInt(3, maNV);                        
                cs.setString(4, madon);                    

                if (cs.isWrapperFor(SQLServerCallableStatement.class)) {
                    SQLServerCallableStatement sqlServerCs = cs.unwrap(SQLServerCallableStatement.class);
                    sqlServerCs.setStructured(5, "dbo.DetailType", dataTable); 
                }

                return cs;
            }
        }, new CallableStatementCallback<Void>() {
            @Override
            public Void doInCallableStatement(CallableStatement cs) throws SQLException {
                cs.execute();
                return null; 
            }
        });

        return orderRepository.findById(madon)
                .orElseThrow(() -> new RuntimeException("Can't create DonHang"));
    }

    @Override
    @Transactional
    public Order updateDonStatus(String madon, String status) {
        Order don = orderRepository.findById(madon)
                .orElseThrow(() -> new RuntimeException("Don Hang not exists"));
        don.setStatus(status);
        return orderRepository.save(don);
    }

    private String generateId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}