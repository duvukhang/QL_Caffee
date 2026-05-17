package com.example.demo.Service.SQL.Staff;

import com.example.demo.DTOS.Request.UpdateStaffRequest;
import com.example.demo.DTOS.Respone.PageResponse3;
import com.example.demo.DTOS.SqlDTO.EligibleStaff;
import com.example.demo.DTOS.SqlDTO.StoreAccount;
import com.example.demo.DTOS.SqlDTO.StoreAccountResult;
import com.example.demo.Models.Staff;
import com.example.demo.Repositories.StaffRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SqlStaffServiceImpl implements SqlStaffService {

    private final StaffRepository staffRepository;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public SqlStaffServiceImpl(StaffRepository staffRepository, EntityManager entityManager, JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.staffRepository = staffRepository;
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public String assignUserToStaff(String staffId, String userName) {
        // ĐÃ CHUẨN HÓA: Sử dụng ?1 và ?2 cho Native Query để tương thích hoàn hảo với SQL Server
        String sql = "EXEC management.uspAssignUserToStaff @UserName = ?1, @TargetStaffId = ?2";
        entityManager.createNativeQuery(sql)
                .setParameter(1, userName)
                .setParameter(2, staffId)
                .executeUpdate();
        return "Successfully Assign User To Staff";
    }

    @Override
    @Transactional
    public Staff createStaff(Staff newStaff, String imgPath) {
        newStaff.setStaffId(generateId("ST"));
        newStaff.setAvatar(imgPath);
        newStaff.setStatus("Hoạt động");
        return staffRepository.save(newStaff);
    }

    @Override
    @Transactional
    public int softDeleteUser(String id) {
        // Hết lỗi gạch đỏ nhờ đồng bộ String ID trong StaffRepository
        Staff user = staffRepository.findById(id).orElse(null);
        if (user != null) {
            user.setStatus("Nghỉ việc");
            staffRepository.save(user);
            return 200;
        }
        return 404;
    }

    @Override
    @Transactional
    public int updateStaffInfo(UpdateStaffRequest req) {
        Staff staff = staffRepository.findById(req.getStaffId()).orElse(null);
        if (staff == null) return 404;

        staff.setStaffName(req.getStaffName());
        staff.setIdNumber(req.getStaffIdNumber());
        staff.setStaffAddr(req.getAddress());
        staff.setDoB(req.getDob());
        staff.setEmail(req.getEmail());
        staff.setPhoneNum(req.getPhoneNum());
        staff.setGender(req.getGender());
        staffRepository.save(staff);
        return 200;
    }

    @Override
    public List<StoreAccount> getStoreAccountsAsync(String storeId, String roleId) {
        String sql = "SELECT * FROM management.fn_GetStoreAccounts(?, ?)";
        List<StoreAccountResult> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(StoreAccountResult.class), storeId, roleId);
        return convertResultToViewModel(results);
    }

    @Override
    public PageResponse3<StoreAccount> getPageAccountAsync(int pageNum, int pageSize) {
        String sql = "SELECT * FROM management.fn_GetStoreAccounts_Paged(?, ?)";
        List<StoreAccountResult> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(StoreAccountResult.class), pageNum, pageSize);

        Integer totalCount = jdbcTemplate.queryForObject("SELECT management.fn_GetStoreAccounts_Count()", Integer.class);
        int totalPage = (pageSize == 0) ? 0 : (int) Math.ceil((double) (totalCount == null ? 0 : totalCount) / pageSize);

        PageResponse3<StoreAccount> response = new PageResponse3<>();
        response.setItems(convertResultToViewModel(results));
        response.setPageIndex(pageNum);
        response.setPageSize(pageSize);
        response.setTotalPages(totalPage);
        response.setTotalCount(totalCount == null ? 0 : totalCount);
        
        return response;
    }

    private List<StoreAccount> convertResultToViewModel(List<StoreAccountResult> lsAcountResult) {
        if (lsAcountResult == null) return new ArrayList<>();

        return lsAcountResult.stream().map(dto -> {
            StoreAccount account = new StoreAccount();
            account.setUsername(dto.getUsername());
            account.setRoleId(dto.getRoleId());
            account.setStaffId(dto.getStaffId());
            account.setStaffName(dto.getStaffName());
            account.setStoreId(dto.getStoreId());
            account.setStoreName(dto.getStoreName());

            if (dto.getEligibleStaff() != null && !dto.getEligibleStaff().isEmpty()) {
                try {
                    EligibleStaff[] parsedArray = objectMapper.readValue(dto.getEligibleStaff(), EligibleStaff[].class);
                    account.setEligibleStaff(new ArrayList<>(Arrays.asList(parsedArray)));
                } catch (Exception e) { 
                    account.setEligibleStaff(new ArrayList<>());
                }
            }
            return account;
        }).collect(Collectors.toList());
    }

    private String generateId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}