package com.example.Admin.Controller.CustomerSite;

import com.example.Admin.DTOS.Customer.CheckoutForm;
import com.example.Admin.Models.Cart;
import com.example.Admin.Models.CustomerDetail;
import com.example.Admin.Repositories.CustomerDetailRepository;
import com.example.Admin.Repositories.OrderRepository;
import com.example.Admin.Service.CustomerSite.CartSessionService;
import com.example.Admin.Service.CustomerSite.CustomerSiteOrderService;
import com.example.Admin.Util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/order")
public class CustomerOrderPageController {

    private final OrderRepository orderRepository;
    private final CustomerDetailRepository detailRepository;
    private final CartSessionService cartSessionService;
    private final CustomerSiteOrderService orderService;

    public CustomerOrderPageController(
            OrderRepository orderRepository,
            CustomerDetailRepository detailRepository,
            CartSessionService cartSessionService,
            CustomerSiteOrderService orderService
    ) {
        this.orderRepository = orderRepository;
        this.detailRepository = detailRepository;
        this.cartSessionService = cartSessionService;
        this.orderService = orderService;
    }

    @GetMapping
    public String index() {
        return "order/index";
    }

    @GetMapping("/history")
    public String history(HttpSession session, Model model) {
        String customerId = SessionHelper.getCustomerId(session);
        if (customerId == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("orders", orderRepository.findByCustomer_CustomerIdOrderByRecivingDateDesc(customerId));
        return "order/history";
    }

    @GetMapping("/details")
    public String details(@RequestParam String id, HttpSession session, Model model) {
        String customerId = SessionHelper.getCustomerId(session);
        if (customerId == null) {
            return "redirect:/user/login";
        }

        return orderRepository.findDetail(id)
                .filter(order -> sameId(customerId, order.getCustomerId()))
                .map(order -> {
                    model.addAttribute("order", order);
                    return "order/details";
                })
                .orElse("error/404");
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        String customerId = SessionHelper.getCustomerId(session);
        if (customerId == null) {
            return "redirect:/user/login";
        }

        Cart cart = cartSessionService.getCart(session);
        if (cart.getItems().isEmpty()) {
            return "redirect:/";
        }

        CustomerDetail customer = detailRepository.findById(customerId).orElse(null);
        CheckoutForm form = new CheckoutForm();
        if (customer != null) {
            form.setReceiverName(customer.getFullName());
            form.setReceiverPhone(customer.getPhoneNum());
            form.setReceiverAddress(customer.getAddr());
        }
        hydrateCheckoutForm(form, cart);
        model.addAttribute("checkoutForm", form);
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String checkoutSubmit(
            @Valid @ModelAttribute("checkoutForm") CheckoutForm checkoutForm,
            BindingResult result,
            HttpSession session,
            Model model
    ) {
        String customerId = SessionHelper.getCustomerId(session);
        if (customerId == null) {
            return "redirect:/user/login";
        }

        Cart cart = cartSessionService.getCart(session);
        if (cart.getItems().isEmpty()) {
            return "redirect:/";
        }

        if (result.hasErrors()) {
            hydrateCheckoutForm(checkoutForm, cart);
            return "order/checkout";
        }

        try {
            orderService.createOrder(cart, customerId);
            cartSessionService.clear(session);
            return "order/order-success";
        } catch (RuntimeException ex) {
            model.addAttribute("error", "L\u1ed7i \u0111\u1eb7t h\u00e0ng: " + ex.getMessage());
            hydrateCheckoutForm(checkoutForm, cart);
            return "order/checkout";
        }
    }

    @PostMapping("/cancel")
    public String cancel(
            @RequestParam String id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String customerId = SessionHelper.getCustomerId(session);
        if (customerId == null) {
            return "redirect:/user/login";
        }

        try {
            orderService.cancelOrder(id, customerId);
            redirectAttributes.addFlashAttribute("successMessage", "\u0110\u00e3 h\u1ee7y \u0111\u01a1n h\u00e0ng th\u00e0nh c\u00f4ng.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/order/history";
    }

    private void hydrateCheckoutForm(CheckoutForm form, Cart cart) {
        form.setCartItems(cart.getItems());
        form.setGrandTotal(cart.totalMoney());
    }

    private boolean sameId(String left, String right) {
        return left != null && right != null && left.trim().equals(right.trim());
    }
}
