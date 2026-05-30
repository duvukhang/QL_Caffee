package com.example.Admin.Controller.CustomerSite;

import com.example.Admin.Models.Cart;
import com.example.Admin.Models.CartItem;
import com.example.Admin.Repositories.ProductRepository;
import com.example.Admin.Service.CustomerSite.CartSessionService;
import com.example.Admin.Util.SessionHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/legacy-cart")
public class CustomerCartController {

    private final CartSessionService cartSessionService;
    private final ProductRepository productRepository;

    public CustomerCartController(CartSessionService cartSessionService, ProductRepository productRepository) {
        this.cartSessionService = cartSessionService;
        this.productRepository = productRepository;
    }

    @GetMapping({"", "/gio-hang"})
    public String cart(HttpSession session, Model model) {
        Cart cart = cartSessionService.getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("relatedProducts", productRepository.findTop4ByOrderByProductIdDesc());
        return "cart/gio-hang";
    }

    @GetMapping("/add")
    public String addToCart(
            @RequestParam String id,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        if (!SessionHelper.isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui l\u00f2ng \u0111\u0103ng nh\u1eadp \u0111\u1ec3 th\u00eam s\u1ea3n ph\u1ea9m v\u00e0o gi\u1ecf h\u00e0ng.");
            return "redirect:/user/login";
        }

        Cart cart = cartSessionService.getCart(session);
        productRepository.findById(id).ifPresentOrElse(product -> {
            cart.addItem(new CartItem(product));
            cartSessionService.saveCart(session, cart);
            redirectAttributes.addFlashAttribute("successMessage", "\u0110\u00e3 th\u00eam s\u1ea3n ph\u1ea9m v\u00e0o gi\u1ecf h\u00e0ng.");
        }, () -> redirectAttributes.addFlashAttribute("errorMessage", "Kh\u00f4ng t\u00ecm th\u1ea5y s\u1ea3n ph\u1ea9m."));

        return redirectBack(request);
    }

    @PostMapping("/update")
    public String updateQuantity(
            @RequestParam String id,
            @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Cart cart = cartSessionService.getCart(session);
        cart.updateQuantity(id, Math.max(quantity, 0));
        cartSessionService.saveCart(session, cart);
        redirectAttributes.addFlashAttribute("successMessage", "\u0110\u00e3 c\u1eadp nh\u1eadt gi\u1ecf h\u00e0ng.");
        return "redirect:/cart/gio-hang";
    }

    @GetMapping("/remove")
    public String removeFromCart(@RequestParam String id, HttpSession session) {
        Cart cart = cartSessionService.getCart(session);
        cart.removeItem(id);
        cartSessionService.saveCart(session, cart);
        return "redirect:/cart/gio-hang";
    }

    private String redirectBack(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "redirect:/";
        }
        return "redirect:" + referer;
    }
}
