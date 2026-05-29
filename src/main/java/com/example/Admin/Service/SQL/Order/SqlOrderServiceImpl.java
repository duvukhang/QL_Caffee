package com.example.Admin.Service.SQL.Order;

import com.example.Admin.DTOS.Request.HoaDonRequest;
import com.example.Admin.Models.Order;
import com.example.Admin.Repositories.OrderRepository;
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
    public Order taoDon(String makhach, int maNV, List<HoaDonRequest.ProductItem> dssp) {
        String madon = generateId("DH");

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

                // 🛠️ LƯU Ý KHO HÀNG: Việc kiểm tra "Ngăn chặn thêm vào giỏ hàng số lượng lớn hơn kho"
                // và "Tự động trừ tồn kho" đang được xử lý ngầm bên trong SP usp_CreateOrder. 
                // Nếu SP trả về lỗi (RaiseError), khối Try-Catch của hệ thống sẽ tự động chặn đơn hàng.
                String sql = "{ ? = call dbo.usp_CreateOrder(?, ?, ?, ?) }";
                CallableStatement cs = con.prepareCall(sql);

                // ... (Khai báo param giữ nguyên)
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
                .orElseThrow(() -> new RuntimeException("Không thể tạo đơn hàng"));
    }

    @Override
    @Transactional
    public Order updateDonStatus(String madon, String status) {
        Order don = orderRepository.findById(madon)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // 🛠️ YÊU CẦU: Chỉ cho phép hủy đơn khi đơn hàng đang ở trạng thái "Chờ xác nhận"
        if ("Huy".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
            String currentStatus = don.getStatus();
            if (!"ChoXacNhan".equalsIgnoreCase(currentStatus) && !"Pending".equalsIgnoreCase(currentStatus)) {
                throw new RuntimeException("Từ chối thao tác: Chỉ được phép hủy khi đơn hàng ở trạng thái Chờ xác nhận.");
            }
        }

        don.setStatus(status);
        don.setUpdateStatusDate(java.time.LocalDateTime.now()); // Nên cập nhật thêm thời gian thay đổi trạng thái
        
        if ("HoanThanh".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            don.setCompleteDate(java.time.LocalDateTime.now());
        }

        return orderRepository.save(don);
    }

    private String generateId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}