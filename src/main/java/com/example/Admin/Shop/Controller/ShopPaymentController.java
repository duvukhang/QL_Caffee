package com.example.Admin.Shop.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.Admin.Shop.Model.PaymentMethod;
import com.example.Admin.Shop.Repository.ShopOrderRepository;
import com.example.Admin.Shop.Service.ShopCurrentUserService;

@Controller
public class ShopPaymentController {
    private final ShopCurrentUserService currentUserService;
    private final ShopOrderRepository orderRepository;
    private final String bankName;
    private final String bankAccountNumber;
    private final String bankAccountHolder;
    private final String bankQrImage;

    public ShopPaymentController(ShopCurrentUserService currentUserService, ShopOrderRepository orderRepository,
            @Value("${shop.payment.bank-name:Ngân hàng Demo Coffee}") String bankName,
            @Value("${shop.payment.bank-account-number:0123456789}") String bankAccountNumber,
            @Value("${shop.payment.bank-account-holder:QL CAFFEE}") String bankAccountHolder,
            @Value("${shop.payment.bank-qr-image:/img/payment/qr-bank.jpg}") String bankQrImage) {
        this.currentUserService = currentUserService;
        this.orderRepository = orderRepository;
        this.bankName = bankName;
        this.bankAccountNumber = bankAccountNumber;
        this.bankAccountHolder = bankAccountHolder;
        this.bankQrImage = bankQrImage;
    }

    @GetMapping("/payments/{orderId}")
    public String payment(@PathVariable Long orderId, Model model) {
        var user = currentUserService.requireUser();
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (order.getPaymentMethod() != PaymentMethod.BANK_QR_MANUAL) {
            return "redirect:/orders/" + order.getId();
        }
        model.addAttribute("order", order);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankAccountNumber", bankAccountNumber);
        model.addAttribute("bankAccountHolder", bankAccountHolder);
        model.addAttribute("bankQrImage", bankQrImage);
        return "shop/payment";
    }

    @PostMapping("/payments/{orderId}/confirm")
    public String confirm(@PathVariable Long orderId) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Khách hàng không được tự xác nhận thanh toán");
    }

    @PostMapping("/payments/{orderId}/fail")
    public String fail(@PathVariable Long orderId) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Khách hàng không được tự đổi trạng thái thanh toán");
    }
}
