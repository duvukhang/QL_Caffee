package com.example.Admin.Shop.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Admin.Shop.Model.ShopOrderStatus;
import com.example.Admin.Shop.Model.ShopReview;
import com.example.Admin.Shop.Repository.ShopOrderRepository;
import com.example.Admin.Shop.Repository.ShopReviewRepository;
import com.example.Admin.Shop.Service.ShopCurrentUserService;
import com.example.Admin.Shop.Service.ShopOrderService;

@Controller
public class ShopOrderController {
    private final ShopCurrentUserService currentUserService;
    private final ShopOrderRepository orderRepository;
    private final ShopReviewRepository reviewRepository;
    private final ShopOrderService orderService;

    public ShopOrderController(ShopCurrentUserService currentUserService, ShopOrderRepository orderRepository,
            ShopReviewRepository reviewRepository, ShopOrderService orderService) {
        this.currentUserService = currentUserService;
        this.orderRepository = orderRepository;
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

        boolean canReviewOrder = !user.isAdminLike()
                && order.getUser().getId().equals(user.getId())
                && order.getStatus() == ShopOrderStatus.COMPLETED;
        List<Long> reviewedProductIds = canReviewOrder
                ? order.getItems().stream()
                        .map(item -> item.getProduct().getId())
                        .filter(productId -> reviewRepository.existsByUser_IdAndProduct_Id(user.getId(), productId))
                        .toList()
                : List.of();

        model.addAttribute("order", order);
        model.addAttribute("canReviewOrder", canReviewOrder);
        model.addAttribute("reviewedProductIds", reviewedProductIds);
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

    @PostMapping("/orders/{orderId}/products/{productId}/reviews")
    public String review(@PathVariable Long orderId, @PathVariable Long productId, @RequestParam int rating,
            @RequestParam String comment, RedirectAttributes redirectAttributes) {
        var user = currentUserService.requireUser();
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (order.getStatus() != ShopOrderStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể đánh giá sản phẩm trong đơn đã hoàn thành");
            return "redirect:/orders/" + orderId;
        }

        var product = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .map(item -> item.getProduct())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm không thuộc đơn này"));

        if (!orderService.canReview(user.getId(), productId)) {
            redirectAttributes.addFlashAttribute("error", "Bạn chỉ có thể đánh giá sản phẩm đã mua và hoàn thành");
            return "redirect:/orders/" + orderId;
        }
        if (reviewRepository.existsByUser_IdAndProduct_Id(user.getId(), productId)) {
            redirectAttributes.addFlashAttribute("error", "Bạn đã đánh giá sản phẩm này rồi");
            return "redirect:/orders/" + orderId;
        }

        ShopReview review = new ShopReview();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(Math.max(1, Math.min(5, rating)));
        review.setComment(comment == null ? "" : comment.trim());
        review.setApproved(false);
        reviewRepository.save(review);

        redirectAttributes.addFlashAttribute("success", "Đã gửi đánh giá, vui lòng chờ admin duyệt");
        return "redirect:/orders/" + orderId;
    }
}
