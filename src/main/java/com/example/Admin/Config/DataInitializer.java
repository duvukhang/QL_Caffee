package com.example.Admin.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.Admin.Models.Store;
import com.example.Admin.Models.Sysrole;
import com.example.Admin.Models.Sysuser;
import com.example.Admin.Repositories.StoreRepository;
import com.example.Admin.Repositories.SysRoleRepository;
import com.example.Admin.Repositories.SysUserRepository;
import com.example.Admin.Service.HashPassWord.PasswordService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final StoreRepository storeRepository;
    private final PasswordService passwordService;

    public DataInitializer(SysUserRepository sysUserRepository, SysRoleRepository sysRoleRepository, StoreRepository storeRepository, PasswordService passwordService) {
        this.sysUserRepository = sysUserRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.storeRepository = storeRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void run(String... args) {
        // 1. Tạo Role Admin nếu chưa có
        Sysrole adminRole = sysRoleRepository.findById("Admin").orElseGet(() -> {
            Sysrole role = new Sysrole();
            role.setRoleId("Admin");
            role.setRoleName("Admin");
            return sysRoleRepository.save(role);
        });

        // 2. Tạo Store mặc định nếu chưa có
        Store defaultStore = storeRepository.findById("ST01").orElseGet(() -> {
            Store store = new Store();
            store.setStoreId("ST01");
            store.setStoreName("Cửa Hàng Trung Tâm");
            store.setStoreAddr("TP.HCM");
            return storeRepository.save(store);
        });

        // 3. Tạo tài khoản Admin mặc định (Dùng Email làm Username)
        String adminEmail = "admin@gmail.com";
        if (sysUserRepository.findByUserName(adminEmail).isEmpty()) {
            Sysuser adminUser = new Sysuser();
            adminUser.setUserName(adminEmail); // Sử dụng Email để đăng nhập
            adminUser.setPassword(passwordService.hashPassword("123")); // Mật khẩu là 123
            adminUser.setRole(adminRole);
            adminUser.setStore(defaultStore);
            sysUserRepository.save(adminUser);
            System.out.println("✅ Đã khởi tạo tài khoản Admin mặc định: admin@gmail.com / 123");
        }
    }
}