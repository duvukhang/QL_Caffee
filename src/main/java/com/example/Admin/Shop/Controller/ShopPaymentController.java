package com.example.Admin.Shop.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Admin.Shop.Model.PaymentMethod;
import com.example.Admin.Shop.Repository.ShopOrderRepository;
import com.example.Admin.Shop.Service.ShopCurrentUserService;
import com.example.Admin.Shop.Service.ShopOrderService;

@Controller
public class ShopPaymentController {
    private final ShopCurrentUserService currentUserService;
    private final ShopOrderRepository orderRepository;
    private final ShopOrderService orderService;

    public ShopPaymentController(ShopCurrentUserService currentUserService, ShopOrderRepository orderRepository,
            ShopOrderService orderService) {
        this.currentUserService = currentUserService;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @GetMapping("/payments/{orderId}")
    public String payment(@PathVariable Long orderId, Model model) {
        var user = currentUserService.requireUser();
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            return "redirect:/orders/" + order.getId();
        }
        model.addAttribute("order", order);
        return "shop/payment";
    }

    @PostMapping("/payments/{orderId}/confirm")
    public String confirm(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            orderService.confirmOnlinePayment(order, currentUserService.requireUser());
            redirectAttributes.addFlashAttribute("success", "Thanh toán online thành công");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/payments/" + orderId;
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/payments/{orderId}/fail")
    public String fail(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            orderService.failOnlinePayment(order, currentUserService.requireUser());
            redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại, đơn đã hủy và hoàn kho");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }
}
