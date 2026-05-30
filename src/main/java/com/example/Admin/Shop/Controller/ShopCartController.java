package com.example.Admin.Shop.Controller;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Admin.Shop.Model.PaymentMethod;
import com.example.Admin.Shop.Model.ShopCart;
import com.example.Admin.Shop.Service.ShopCartService;
import com.example.Admin.Shop.Service.ShopCouponService;
import com.example.Admin.Shop.Service.ShopCurrentUserService;
import com.example.Admin.Shop.Service.ShopOrderService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ShopCartController {
    private static final Pattern PHONE = Pattern.compile("^\\d{10,11}$");
    private static final String COUPON_SESSION_KEY = "shopCouponCode";

    private final ShopCurrentUserService currentUserService;
    private final ShopCartService cartService;
    private final ShopCouponService couponService;
    private final ShopOrderService orderService;

    public ShopCartController(ShopCurrentUserService currentUserService, ShopCartService cartService,
            ShopCouponService couponService, ShopOrderService orderService) {
        this.currentUserService = currentUserService;
        this.cartService = cartService;
        this.couponService = couponService;
        this.orderService = orderService;
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        var user = currentUserService.requireUser();
        ShopCart cart = cartService.getOrCreateCart(user);
        BigDecimal subtotal = cartService.subtotal(cart);
        String couponCode = (String) session.getAttribute(COUPON_SESSION_KEY);
        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("couponCode", couponCode);
        model.addAttribute("couponQuote", couponService.quote(couponCode, user, subtotal));
        return "shop/cart";
    }

    @PostMapping("/cart/add")
    public String add(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.add(currentUserService.requireUser(), productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{id}/update")
    public String update(@PathVariable Long id, @RequestParam int quantity, RedirectAttributes redirectAttributes) {
        try {
            cartService.update(id, quantity, currentUserService.requireUser());
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật giỏ hàng");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{id}/remove")
    public String remove(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            cartService.remove(id, currentUserService.requireUser());
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/apply-coupon")
    public String applyCoupon(@RequestParam String couponCode, HttpSession session, RedirectAttributes redirectAttributes) {
        var user = currentUserService.requireUser();
        BigDecimal subtotal = cartService.subtotal(cartService.getOrCreateCart(user));
        var quote = couponService.quote(couponCode, user, subtotal);
        if (quote.valid() && quote.coupon() != null) {
            session.setAttribute(COUPON_SESSION_KEY, quote.coupon().getCode());
            redirectAttributes.addFlashAttribute("success", quote.message());
        } else {
            session.removeAttribute(COUPON_SESSION_KEY);
            redirectAttributes.addFlashAttribute("error", quote.message().isBlank() ? "Mã không hợp lệ" : quote.message());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear-coupon")
    public String clearCoupon(HttpSession session) {
        session.removeAttribute(COUPON_SESSION_KEY);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        var user = currentUserService.requireUser();
        ShopCart cart = cartService.getOrCreateCart(user);
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng đang trống");
        }
        BigDecimal subtotal = cartService.subtotal(cart);
        String couponCode = (String) session.getAttribute(COUPON_SESSION_KEY);
        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("couponCode", couponCode);
        model.addAttribute("couponQuote", couponService.quote(couponCode, user, subtotal));
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "shop/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            @RequestParam String shippingAddress,
            @RequestParam(defaultValue = "COD") PaymentMethod paymentMethod,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (receiverName == null || receiverName.isBlank() || shippingAddress == null || shippingAddress.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập đầy đủ thông tin giao hàng");
            return "redirect:/checkout";
        }
        if (receiverPhone == null || !PHONE.matcher(receiverPhone.trim()).matches()) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại phải gồm 10-11 chữ số");
            return "redirect:/checkout";
        }
        try {
            String couponCode = (String) session.getAttribute(COUPON_SESSION_KEY);
            var order = orderService.placeOrder(currentUserService.requireUser(), receiverName.trim(), receiverPhone.trim(),
                    shippingAddress.trim(), paymentMethod, couponCode);
            session.removeAttribute(COUPON_SESSION_KEY);
            if (order.getPaymentMethod() != PaymentMethod.COD) {
                redirectAttributes.addFlashAttribute("success", "Vui lòng hoàn tất thanh toán online");
                return "redirect:/payments/" + order.getId();
            }
            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công");
            return "redirect:/orders/" + order.getId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/checkout";
        }
    }
}
