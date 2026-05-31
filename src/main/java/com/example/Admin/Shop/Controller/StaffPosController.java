package com.example.Admin.Shop.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Admin.Shop.Dto.PosOrderItemRequest;
import com.example.Admin.Shop.Dto.PosOrderQuoteResponse;
import com.example.Admin.Shop.Dto.PosOrderRequest;
import com.example.Admin.Shop.Dto.PosOrderResponse;
import com.example.Admin.Shop.Dto.PosProductResponse;
import com.example.Admin.Shop.Model.PaymentMethod;
import com.example.Admin.Shop.Model.ShopOrder;
import com.example.Admin.Shop.Model.ShopOrderType;
import com.example.Admin.Shop.Model.ShopRole;
import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Repository.ShopCategoryRepository;
import com.example.Admin.Shop.Repository.ShopOrderRepository;
import com.example.Admin.Shop.Repository.ShopProductRepository;
import com.example.Admin.Shop.Service.ShopCurrentUserService;
import com.example.Admin.Shop.Service.StaffPosService;

@Controller
public class StaffPosController {
    private static final List<ShopOrderType> POS_TYPES = List.of(ShopOrderType.POS, ShopOrderType.TAKE_AWAY);

    private final ShopCurrentUserService currentUserService;
    private final ShopProductRepository productRepository;
    private final ShopCategoryRepository categoryRepository;
    private final ShopOrderRepository orderRepository;
    private final StaffPosService staffPosService;

    public StaffPosController(ShopCurrentUserService currentUserService, ShopProductRepository productRepository,
            ShopCategoryRepository categoryRepository, ShopOrderRepository orderRepository, StaffPosService staffPosService) {
        this.currentUserService = currentUserService;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.staffPosService = staffPosService;
    }

    @GetMapping("/staff/pos")
    public String pos(Model model) {
        ShopUser staff = requirePosUser();
        model.addAttribute("products", productRepository.findActiveWithImagesOrderByNameAsc());
        model.addAttribute("categories", categoryRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("paymentMethods", staffPosService.availablePaymentMethods());
        model.addAttribute("orderTypes", staffPosService.availableOrderTypes());
        model.addAttribute("staffUser", staff);
        return "shop/staff/pos";
    }

    @GetMapping("/staff/pos/products")
    @ResponseBody
    public List<PosProductResponse> products() {
        requirePosUser();
        return productRepository.findActiveWithImagesOrderByNameAsc().stream()
                .map(product -> new PosProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getCategory().getName(),
                        product.getEffectivePrice(),
                        product.getQuantity(),
                        product.getMainImagePath()))
                .toList();
    }

    @PostMapping("/staff/pos/quote")
    @ResponseBody
    public PosOrderQuoteResponse quote(@RequestBody PosOrderRequest request) {
        try {
            return staffPosService.calculatePosOrderQuote(request, requirePosUser());
        } catch (IllegalArgumentException ex) {
            return new PosOrderQuoteResponse(false, ex.getMessage(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    @PostMapping("/staff/pos/create-order")
    public String createOrder(
            @RequestParam(name = "productId", required = false) List<Long> productIds,
            @RequestParam(name = "quantity", required = false) List<Integer> quantities,
            @RequestParam(required = false) String couponCode,
            @RequestParam PaymentMethod paymentMethod,
            @RequestParam(defaultValue = "POS") ShopOrderType orderType,
            RedirectAttributes redirectAttributes) {
        try {
            PosOrderRequest request = new PosOrderRequest(toItems(productIds, quantities), couponCode, paymentMethod, orderType);
            ShopOrder order = staffPosService.createPosOrder(requirePosUser(), request);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã hoàn thành: " + order.getOrderCode());
            return "redirect:/staff/pos/orders/" + order.getId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/staff/pos";
        }
    }

    @GetMapping("/staff/pos/orders")
    public String posOrders(Model model) {
        ShopUser staff = requirePosUser();
        List<ShopOrder> orders = staff.getRole().canViewAllOrders()
                ? orderRepository.findByOrderTypeInOrderByCreatedAtDesc(POS_TYPES)
                : orderRepository.findByCreatedByStaffAndOrderTypeInOrderByCreatedAtDesc(staff, POS_TYPES);
        model.addAttribute("orders", orders);
        return "shop/staff/pos-orders";
    }

    @GetMapping("/staff/pos/orders/{id}")
    public String posOrderDetail(@PathVariable Long id, Model model) {
        ShopOrder order = findVisiblePosOrder(id);
        model.addAttribute("order", order);
        return "shop/staff/pos-order-detail";
    }

    @GetMapping("/staff/pos/orders/{id}/receipt")
    public String receipt(@PathVariable Long id, Model model) {
        ShopOrder order = findVisiblePosOrder(id);
        model.addAttribute("order", order);
        return "shop/staff/pos-receipt";
    }

    @PostMapping("/staff/pos/create-order-json")
    @ResponseBody
    public PosOrderResponse createOrderJson(@RequestBody PosOrderRequest request) {
        ShopOrder order = staffPosService.createPosOrder(requirePosUser(), request);
        return new PosOrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                "Đã thanh toán",
                "Hoàn thành",
                "Đơn hàng đã hoàn thành");
    }

    private ShopOrder findVisiblePosOrder(Long id) {
        ShopUser staff = requirePosUser();
        ShopOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!POS_TYPES.contains(order.getOrderType())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!staff.getRole().canViewAllOrders()
                && (order.getCreatedByStaff() == null || !order.getCreatedByStaff().getId().equals(staff.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return order;
    }

    private ShopUser requirePosUser() {
        ShopUser user = currentUserService.requireUser();
        if (user.getRole() == null || !user.getRole().canUsePos() || user.getRole() == ShopRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private List<PosOrderItemRequest> toItems(List<Long> productIds, List<Integer> quantities) {
        if (productIds == null || quantities == null || productIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Hóa đơn chưa có sản phẩm");
        }
        return IntStream.range(0, productIds.size())
                .mapToObj(index -> new PosOrderItemRequest(productIds.get(index), quantities.get(index)))
                .toList();
    }
}
