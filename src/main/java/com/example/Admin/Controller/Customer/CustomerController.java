package com.example.Admin.Controller.Customer;

import com.example.Admin.DTOS.Request.CustomerChangePasswordRequest;
import com.example.Admin.DTOS.Request.CustomerForgotPasswordRequest;
import com.example.Admin.DTOS.Request.CustomerProfileRequest;
import com.example.Admin.DTOS.Request.CustomerRegisterRequest;
import com.example.Admin.DTOS.Request.HoaDonRequest;
import com.example.Admin.Models.CustomerDetail;
import com.example.Admin.Models.Order;
import com.example.Admin.Models.OrderDetail;
import com.example.Admin.Repositories.OrderRepository;
import com.example.Admin.Service.Customer.CustomerAccountService;
import com.example.Admin.Service.Customer.CustomerOrderService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Customer")
public class CustomerController {

    private final CustomerAccountService customerAccountService;
    private final CustomerOrderService customerOrderService;
    private final OrderRepository orderRepository;

    public CustomerController(
            CustomerAccountService customerAccountService,
            CustomerOrderService customerOrderService,
            OrderRepository orderRepository
    ) {
        this.customerAccountService = customerAccountService;
        this.customerOrderService = customerOrderService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/Register")
    public ResponseEntity<?> register(@Valid @RequestBody CustomerRegisterRequest request) {
        try {
            String customerId = customerAccountService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Register customer successfully.",
                    "customerId", customerId
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/ForgotPassword")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody CustomerForgotPasswordRequest request) {
        try {
            customerAccountService.resetPassword(request);
            return ResponseEntity.ok(Map.of("message", "Reset password successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/Profile")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> profile(Authentication authentication) {
        CustomerDetail detail = customerAccountService.getProfile(currentCustomerId(authentication));
        return ResponseEntity.ok(mapProfile(detail));
    }

    @PutMapping("/Profile")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @Valid @RequestBody CustomerProfileRequest request
    ) {
        try {
            CustomerDetail detail = customerAccountService.updateProfile(currentCustomerId(authentication), request);
            return ResponseEntity.ok(mapProfile(detail));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/ChangePassword")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody CustomerChangePasswordRequest request
    ) {
        try {
            customerAccountService.changePassword(currentCustomerId(authentication), request);
            return ResponseEntity.ok(Map.of("message", "Change password successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/Orders")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> orders(Authentication authentication) {
        List<Map<String, Object>> orders = orderRepository
                .findByCustomer_CustomerIdOrderByRecivingDateDesc(currentCustomerId(authentication))
                .stream()
                .map(this::mapOrderSummary)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/Orders/{orderId}")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> orderDetail(Authentication authentication, @PathVariable String orderId) {
        String customerId = currentCustomerId(authentication);
        return orderRepository.findDetail(orderId)
                .filter(order -> sameId(customerId, order.getCustomerId()))
                .<ResponseEntity<?>>map(order -> ResponseEntity.ok(mapOrderDetail(order)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Order not found.")));
    }

    @PostMapping("/Orders")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> createOrder(Authentication authentication, @RequestBody HoaDonRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Request body is empty."));
            }
            Order order = customerOrderService.createOrder(currentCustomerId(authentication), request.getDssp());
            return ResponseEntity.status(HttpStatus.CREATED).body(mapOrderSummary(order));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/Orders/{orderId}/Cancel")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> cancelOrder(Authentication authentication, @PathVariable String orderId) {
        try {
            customerOrderService.cancelOrder(orderId, currentCustomerId(authentication));
            return ResponseEntity.ok(Map.of("message", "Cancel order successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    private String currentCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Token khong hop le.");
        }
        return authentication.getName();
    }

    private Map<String, Object> mapProfile(CustomerDetail detail) {
        Map<String, Object> response = new HashMap<>();
        response.put("customerId", detail.getCustomerId());
        response.put("fullName", detail.getFullName());
        response.put("email", detail.getEmail());
        response.put("phone", detail.getPhoneNum());
        response.put("address", detail.getAddr());
        response.put("idNumber", detail.getIdNumber());
        response.put("gender", detail.getGender());
        response.put("avatar", detail.getAvatar());
        return response;
    }

    private Map<String, Object> mapOrderSummary(Order order) {
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getOrderId());
        response.put("status", order.getStatus());
        response.put("recivingDate", order.getRecivingDate());
        response.put("updateStatusDate", order.getUpdateStatusDate());
        response.put("completeDate", order.getCompleteDate());
        response.put("customerId", order.getCustomerId());
        return response;
    }

    private Map<String, Object> mapOrderDetail(Order order) {
        Map<String, Object> response = mapOrderSummary(order);
        List<Map<String, Object>> items = order.getOrderDetails().stream()
                .map(this::mapOrderItem)
                .toList();
        response.put("items", items);
        return response;
    }

    private Map<String, Object> mapOrderItem(OrderDetail detail) {
        Map<String, Object> response = new HashMap<>();
        response.put("productId", detail.getProduct() != null ? detail.getProduct().getProductId() : null);
        response.put("productName", detail.getProduct() != null ? detail.getProduct().getProductName() : null);
        response.put("price", detail.getProduct() != null ? detail.getProduct().getPrice() : null);
        response.put("quantity", detail.getQuantity());
        return response;
    }

    private boolean sameId(String left, String right) {
        return left != null && right != null && left.trim().equals(right.trim());
    }
}
