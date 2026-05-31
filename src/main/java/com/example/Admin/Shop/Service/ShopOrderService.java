package com.example.Admin.Shop.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Admin.Shop.Model.InventoryHistoryType;
import com.example.Admin.Shop.Model.PaymentMethod;
import com.example.Admin.Shop.Model.PaymentStatus;
import com.example.Admin.Shop.Model.ShopCart;
import com.example.Admin.Shop.Model.ShopInventoryHistory;
import com.example.Admin.Shop.Model.ShopOrder;
import com.example.Admin.Shop.Model.ShopOrderItem;
import com.example.Admin.Shop.Model.ShopOrderStatus;
import com.example.Admin.Shop.Model.ShopProduct;
import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Repository.ShopInventoryHistoryRepository;
import com.example.Admin.Shop.Repository.ShopOrderItemRepository;
import com.example.Admin.Shop.Repository.ShopOrderRepository;
import com.example.Admin.Shop.Repository.ShopProductRepository;
import com.example.Admin.Shop.Service.ShopCouponService.CouponQuote;

@Service
public class ShopOrderService {
    private final ShopCartService cartService;
    private final ShopCouponService couponService;
    private final ShopOrderRepository orderRepository;
    private final ShopProductRepository productRepository;
    private final ShopInventoryHistoryRepository inventoryHistoryRepository;
    private final ShopOrderItemRepository orderItemRepository;

    public ShopOrderService(ShopCartService cartService, ShopCouponService couponService,
            ShopOrderRepository orderRepository, ShopProductRepository productRepository,
            ShopInventoryHistoryRepository inventoryHistoryRepository, ShopOrderItemRepository orderItemRepository) {
        this.cartService = cartService;
        this.couponService = couponService;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public ShopOrder placeOrder(ShopUser user, String receiverName, String receiverPhone, String shippingAddress,
            PaymentMethod paymentMethod, String couponCode) {
        ShopCart cart = cartService.getOrCreateCart(user);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống");
        }

        BigDecimal subtotal = cartService.subtotal(cart);
        CouponQuote quote = couponService.quote(couponCode, user, subtotal);
        if (!quote.valid()) {
            throw new IllegalArgumentException(quote.message());
        }

        PaymentMethod selectedPaymentMethod = paymentMethod == null ? PaymentMethod.COD : paymentMethod;
        ShopOrder order = new ShopOrder();
        order.setOrderCode(nextOrderCode());
        order.setUser(user);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(selectedPaymentMethod);
        if (selectedPaymentMethod == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setStatus(ShopOrderStatus.PENDING);
        } else if (selectedPaymentMethod == PaymentMethod.BANK_QR_MANUAL) {
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setPaymentReference(order.getOrderCode());
            order.setStatus(ShopOrderStatus.PENDING_PAYMENT);
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setPaymentReference("PAY" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
            order.setStatus(ShopOrderStatus.PENDING);
        }
        order.setSubtotal(subtotal);
        order.setDiscountAmount(quote.discountAmount());
        order.setTotalAmount(quote.totalAmount());
        order.setCoupon(quote.coupon());

        cart.getItems().forEach(cartItem -> {
            ShopProduct product = cartItem.getProduct();
            if (cartItem.getQuantity() > product.getQuantity()) {
                throw new IllegalArgumentException(product.getName() + " không đủ tồn kho");
            }
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            ShopOrderItem item = new ShopOrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setPrice(product.getEffectivePrice());
            item.setQuantity(cartItem.getQuantity());
            item.setTotal(product.getEffectivePrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            order.getItems().add(item);

            inventoryHistoryRepository.save(history(product, InventoryHistoryType.EXPORT, cartItem.getQuantity(),
                    "Trừ kho cho đơn " + order.getOrderCode()));
        });

        ShopOrder saved = orderRepository.save(order);
        couponService.markUsed(quote.coupon(), user);
        cartService.clear(cart);
        return saved;
    }

    @Transactional
    public void cancelByCustomer(ShopOrder order, ShopUser user) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn này");
        }
        if (!order.isCancelableByCustomer()) {
            throw new IllegalArgumentException("Chỉ được hủy đơn khi đang chờ xác nhận hoặc chờ thanh toán");
        }
        cancel(order);
    }

    @Transactional
    public void cancel(ShopOrder order) {
        if (order.getStatus() == ShopOrderStatus.CANCELLED) {
            return;
        }
        if (order.getStatus() != ShopOrderStatus.PENDING && order.getStatus() != ShopOrderStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Chỉ được hủy đơn đang chờ xác nhận hoặc chờ thanh toán");
        }
        restoreStock(order);
        order.setStatus(ShopOrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public void moveToStatus(ShopOrder order, ShopOrderStatus nextStatus) {
        if (order.getPaymentMethod() != PaymentMethod.COD && order.getPaymentStatus() != PaymentStatus.PAID
                && nextStatus != ShopOrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Đơn online phải thanh toán thành công trước khi xử lý");
        }
        List<ShopOrderStatus> flow = order.getStatus() == ShopOrderStatus.PENDING_PAYMENT
                ? List.of(ShopOrderStatus.PENDING_PAYMENT, ShopOrderStatus.CONFIRMED,
                        ShopOrderStatus.SHIPPING, ShopOrderStatus.COMPLETED)
                : List.of(ShopOrderStatus.PENDING, ShopOrderStatus.CONFIRMED,
                        ShopOrderStatus.SHIPPING, ShopOrderStatus.COMPLETED);
        if (nextStatus == ShopOrderStatus.CANCELLED) {
            cancel(order);
            return;
        }
        int current = flow.indexOf(order.getStatus());
        int next = flow.indexOf(nextStatus);
        if (current < 0 || next != current + 1) {
            throw new IllegalArgumentException("Không được nhảy trạng thái sai luồng");
        }
        order.setStatus(nextStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void confirmManualBankPayment(ShopOrder order) {
        if (order.getPaymentMethod() != PaymentMethod.BANK_QR_MANUAL) {
            throw new IllegalArgumentException("Chỉ xác nhận thủ công cho đơn chuyển khoản QR");
        }
        if (order.getStatus() == ShopOrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Đơn đã hủy");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ xác nhận đơn đang chờ thanh toán");
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setStatus(ShopOrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Transactional
    public void confirmOnlinePayment(ShopOrder order, ShopUser user) {
        ensureOwner(order, user);
        throw new IllegalArgumentException("Khách hàng không được tự xác nhận thanh toán");
    }

    @Transactional
    public void failOnlinePayment(ShopOrder order, ShopUser user) {
        ensureOwner(order, user);
        throw new IllegalArgumentException("Khách hàng không được tự đổi trạng thái thanh toán");
    }

    public boolean canReview(Long userId, Long productId) {
        return orderItemRepository.existsByOrder_User_IdAndProduct_IdAndOrder_Status(userId, productId, ShopOrderStatus.COMPLETED);
    }

    private void restoreStock(ShopOrder order) {
        order.getItems().forEach(item -> {
            ShopProduct product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
            inventoryHistoryRepository.save(history(product, InventoryHistoryType.ORDER_CANCEL, item.getQuantity(),
                    "Hoàn kho do hủy đơn " + order.getOrderCode()));
        });
    }

    private void ensureOwner(ShopOrder order, ShopUser user) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác đơn này");
        }
    }

    private String nextOrderCode() {
        long nextNumber = orderRepository.count() + 1;
        String orderCode;
        do {
            orderCode = "QLCF" + String.format("%06d", nextNumber++);
        } while (orderRepository.existsByOrderCode(orderCode));
        return orderCode;
    }

    private ShopInventoryHistory history(ShopProduct product, InventoryHistoryType type, int quantity, String note) {
        ShopInventoryHistory history = new ShopInventoryHistory();
        history.setProduct(product);
        history.setType(type);
        history.setQuantity(quantity);
        history.setNote(note);
        return history;
    }
}
