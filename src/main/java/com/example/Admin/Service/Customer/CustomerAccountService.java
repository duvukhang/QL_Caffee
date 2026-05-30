package com.example.Admin.Service.Customer;

import com.example.Admin.DTOS.Request.CustomerChangePasswordRequest;
import com.example.Admin.DTOS.Request.CustomerForgotPasswordRequest;
import com.example.Admin.DTOS.Request.CustomerProfileRequest;
import com.example.Admin.DTOS.Request.CustomerRegisterRequest;
import com.example.Admin.Models.Customer;
import com.example.Admin.Models.CustomerDetail;
import com.example.Admin.Repositories.CustomerDetailRepository;
import com.example.Admin.Repositories.CustomerRepository;
import com.example.Admin.Repositories.SysUserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Service
public class CustomerAccountService {

    private static final String ACTIVE_STATUS = "Ho\u1ea1t \u0111\u1ed9ng";
    private static final String DEFAULT_AVATAR = "/uploads/default.png";
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    private final CustomerRepository customerRepository;
    private final CustomerDetailRepository detailRepository;
    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder encoder;

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerAccountService(
            CustomerRepository customerRepository,
            CustomerDetailRepository detailRepository,
            SysUserRepository sysUserRepository,
            PasswordEncoder encoder
    ) {
        this.customerRepository = customerRepository;
        this.detailRepository = detailRepository;
        this.sysUserRepository = sysUserRepository;
        this.encoder = encoder;
    }

    public Optional<Customer> login(String username, String rawPassword) {
        String normalizedUsername = normalizeLower(username);
        if (normalizedUsername == null || rawPassword == null) {
            return Optional.empty();
        }

        return customerRepository.findByUserNameIgnoreCase(normalizedUsername)
                .filter(customer -> isActive(customer.getStatus()))
                .filter(customer -> passwordMatches(rawPassword, customer.getPassword()));
    }

    public boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return encoder.matches(rawPassword, storedPassword);
        }

        return storedPassword.equals(rawPassword);
    }

    @Transactional
    public String register(CustomerRegisterRequest request) {
        String email = normalizeLower(request.getEmail());
        String password = normalize(request.getPassword());
        String fullName = normalize(request.getFullName());
        String address = normalize(request.getAddress());
        String phone = onlyDigits(request.getPhone());
        String idNumber = onlyDigits(request.getIdNumber());
        String gender = normalizeGender(request.getGender());

        validateRegisterData(email, password, fullName, phone, idNumber, gender);

        String customerId = generateUniqueCustomerId();
        entityManager.createNativeQuery("""
                        INSERT INTO customers.Customer(CustomerId, UserName, [Password], Status)
                        VALUES (:customerId, :userName, :password, :status)
                        """)
                .setParameter("customerId", customerId)
                .setParameter("userName", email)
                .setParameter("password", encoder.encode(password))
                .setParameter("status", ACTIVE_STATUS)
                .executeUpdate();

        entityManager.createNativeQuery("""
                        INSERT INTO customers.CustomerDetail(CustomerId, FullName, IdNumber, Gender, PhoneNum, Email, Avatar, Addr)
                        VALUES (:customerId, :fullName, :idNumber, :gender, :phoneNum, :email, :avatar, :addr)
                        """)
                .setParameter("customerId", customerId)
                .setParameter("fullName", fullName)
                .setParameter("idNumber", idNumber)
                .setParameter("gender", gender)
                .setParameter("phoneNum", phone)
                .setParameter("email", email)
                .setParameter("avatar", DEFAULT_AVATAR)
                .setParameter("addr", address)
                .executeUpdate();

        entityManager.flush();
        return customerId;
    }

    @Transactional(readOnly = true)
    public CustomerDetail getProfile(String customerId) {
        String id = normalize(customerId);
        if (id == null) {
            throw new IllegalArgumentException("Phien dang nhap khong hop le.");
        }

        return detailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thong tin khach hang."));
    }

    @Transactional
    public CustomerDetail updateProfile(String customerId, CustomerProfileRequest request) {
        String id = normalize(customerId);
        if (id == null) {
            throw new IllegalArgumentException("Phien dang nhap khong hop le.");
        }

        CustomerDetail detail = detailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thong tin khach hang."));

        String email = normalizeLower(request.getEmail());
        String fullName = normalize(request.getFullName());
        String phone = onlyDigits(request.getPhone());
        String address = normalize(request.getAddress());
        String idNumber = onlyDigits(request.getIdNumber());
        String gender = normalizeGender(request.getGender());

        validateProfileData(id, email, fullName, phone, idNumber, gender);

        detail.setFullName(fullName);
        detail.setPhoneNum(phone);
        detail.setAddr(address);
        detail.setEmail(email);
        detail.setIdNumber(idNumber);
        detail.setGender(gender);
        detailRepository.save(detail);

        Customer customer = detail.getCustomer();
        if (customer != null && !email.equalsIgnoreCase(customer.getUserName())) {
            customer.setUserName(email);
            customerRepository.save(customer);
        }

        return detail;
    }

    @Transactional
    public void changePassword(String customerId, CustomerChangePasswordRequest request) {
        String id = normalize(customerId);
        if (id == null) {
            throw new IllegalArgumentException("Phien dang nhap khong hop le.");
        }
        if (!normalize(request.getNewPassword()).equals(normalize(request.getConfirmPassword()))) {
            throw new IllegalArgumentException("Mat khau xac nhan khong khop.");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay khach hang."));

        if (!passwordMatches(request.getOldPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("Mat khau hien tai khong chinh xac.");
        }

        customer.setPassword(encoder.encode(request.getNewPassword()));
        customerRepository.save(customer);
    }

    @Transactional
    public void resetPassword(CustomerForgotPasswordRequest request) {
        String email = normalizeLower(request.getEmail());
        String phone = onlyDigits(request.getPhone());
        String newPassword = normalize(request.getNewPassword());
        String confirmPassword = normalize(request.getConfirmPassword());

        if (email == null || phone == null || newPassword == null || confirmPassword == null) {
            throw new IllegalArgumentException("Vui long nhap day du email, so dien thoai va mat khau moi.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mat khau xac nhan khong khop.");
        }
        if (newPassword.length() < 6 || newPassword.length() > 50) {
            throw new IllegalArgumentException("Mat khau moi phai tu 6 den 50 ky tu.");
        }

        CustomerDetail detail = detailRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay tai khoan khach hang."));

        if (detail.getPhoneNum() == null || !detail.getPhoneNum().trim().equals(phone)) {
            throw new IllegalArgumentException("So dien thoai khong khop voi tai khoan.");
        }
        Customer customer = detail.getCustomer();
        if (customer == null) {
            customer = customerRepository.findById(detail.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay tai khoan khach hang."));
        }

        customer.setPassword(encoder.encode(newPassword));
        customerRepository.save(customer);
    }

    private void validateRegisterData(String email, String password, String fullName, String phone, String idNumber, String gender) {
        validateCommonCustomerData(email, fullName, phone, idNumber, gender);
        if (password == null || password.length() < 6 || password.length() > 50) {
            throw new IllegalArgumentException("Mat khau phai tu 6 den 50 ky tu.");
        }
        if (customerRepository.existsByUserNameIgnoreCase(email) || detailRepository.existsByEmail(email) || sysUserRepository.existsByUserNameIgnoreCase(email)) {
            throw new IllegalArgumentException("Email nay da duoc su dung.");
        }
        if (detailRepository.existsByPhoneNum(phone)) {
            throw new IllegalArgumentException("So dien thoai nay da duoc su dung.");
        }
        if (idNumber != null && detailRepository.existsByIdNumber(idNumber)) {
            throw new IllegalArgumentException("CMND/CCCD nay da duoc su dung.");
        }
    }

    private void validateProfileData(String customerId, String email, String fullName, String phone, String idNumber, String gender) {
        validateCommonCustomerData(email, fullName, phone, idNumber, gender);
        if (sysUserRepository.existsByUserNameIgnoreCase(email)) {
            throw new IllegalArgumentException("Email nay dang duoc dung boi tai khoan nhan vien/admin.");
        }
        if (detailRepository.existsByEmailAndCustomerIdNot(email, customerId)) {
            throw new IllegalArgumentException("Email nay da duoc khach hang khac su dung.");
        }
        Customer current = customerRepository.findById(customerId).orElse(null);
        if (current == null || (!email.equalsIgnoreCase(current.getUserName()) && customerRepository.existsByUserNameIgnoreCase(email))) {
            throw new IllegalArgumentException("Email nay da duoc khach hang khac su dung.");
        }
        if (detailRepository.existsByPhoneNumAndCustomerIdNot(phone, customerId)) {
            throw new IllegalArgumentException("So dien thoai nay da duoc khach hang khac su dung.");
        }
        if (idNumber != null && detailRepository.existsByIdNumberAndCustomerIdNot(idNumber, customerId)) {
            throw new IllegalArgumentException("CMND/CCCD nay da duoc khach hang khac su dung.");
        }
    }

    private void validateCommonCustomerData(String email, String fullName, String phone, String idNumber, String gender) {
        if (email == null || fullName == null || phone == null) {
            throw new IllegalArgumentException("Vui long nhap day du email, ho ten va so dien thoai.");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Email khong dung dinh dang.");
        }
        if (email.length() > 50) {
            throw new IllegalArgumentException("Email toi da 50 ky tu.");
        }
        if (fullName.length() > 50) {
            throw new IllegalArgumentException("Ho ten toi da 50 ky tu.");
        }
        if (!phone.matches("0[0-9]{9}")) {
            throw new IllegalArgumentException("So dien thoai gom 10 so va bat dau bang 0.");
        }
        if (idNumber != null && !idNumber.matches("[0-9]{9,11}")) {
            throw new IllegalArgumentException("CMND/CCCD gom 9 den 11 so.");
        }
        if (gender != null && !isAllowedGender(gender)) {
            throw new IllegalArgumentException("Gioi tinh khong hop le.");
        }
    }

    private String generateUniqueCustomerId() {
        for (int i = 0; i < 200; i++) {
            String candidate = "US" + String.format("%08d", ThreadLocalRandom.current().nextInt(0, 100_000_000));
            if (!customerRepository.existsById(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Khong tao duoc ma khach hang, vui long thu lai.");
    }

    private boolean isActive(String status) {
        String normalized = normalizeText(status);
        return "hoat dong".equals(normalized) || "active".equals(normalized);
    }

    private boolean isAllowedGender(String gender) {
        String normalized = normalizeText(gender);
        return "nam".equals(normalized) || "nu".equals(normalized) || "khac".equals(normalized);
    }

    private String normalizeGender(String gender) {
        String normalized = normalize(gender);
        return normalized == null ? null : normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeLower(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private String onlyDigits(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.replaceAll("\\D", "");
    }

    private String normalizeText(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String decomposed = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed)
                .replaceAll("")
                .replace('\u0111', 'd')
                .replace('\u0110', 'd')
                .toLowerCase();
    }
}
