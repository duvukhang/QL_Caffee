package com.example.Admin.Shop.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Admin.Shop.Model.ShopReview;
import com.example.Admin.Shop.Repository.ShopOrderRepository;
import com.example.Admin.Shop.Repository.ShopProductRepository;
import com.example.Admin.Shop.Repository.ShopReviewRepository;
import com.example.Admin.Shop.Service.ShopCurrentUserService;
import com.example.Admin.Shop.Service.ShopOrderService;

@Controller
public class ShopOrderController {
    private final ShopCurrentUserService currentUserService;
    private final ShopOrderRepository orderRepository;
    private final ShopProductRepository productRepository;
    private final ShopReviewRepository reviewRepository;
    private final ShopOrderService orderService;

    public ShopOrderController(ShopCurrentUserService currentUserService, ShopOrderRepository orderRepository,
            ShopProductRepository productRepository, ShopReviewRepository reviewRepository, ShopOrderService orderService) {
        this.currentUserService = currentUserService;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        var user = currentUserService.requireUser();
        model.addAttribute("orders", orderRepository.findByUserOrderByCreatedAtDesc(user));
        return "shop/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        var user = currentUserService.requireUser();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!order.getUser().getId().equals(user.getId()) && !user.isAdminLike()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        model.addAttribute("order", order);
        return "shop/order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var user = currentUserService.requireUser();
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            orderService.cancelByCustomer(order, user);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn và hoàn kho");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/products/{id}/reviews")
    public String review(@PathVariable Long id, @RequestParam int rating, @RequestParam String comment,
            RedirectAttributes redirectAttributes) {
        var user = currentUserService.requireUser();
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!orderService.canReview(user.getId(), id)) {
            redirectAttributes.addFlashAttribute("error", "Bạn chỉ có thể đánh giá sản phẩm đã mua và hoàn thành");
            return "redirect:/products/" + id;
        }
        ShopReview review = new ShopReview();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(Math.max(1, Math.min(5, rating)));
        review.setComment(comment == null ? "" : comment.trim());
        review.setApproved(false);
        reviewRepository.save(review);
        redirectAttributes.addFlashAttribute("success", "Đã gửi đánh giá, vui lòng chờ admin duyệt");
        return "redirect:/products/" + id;
    }
}
