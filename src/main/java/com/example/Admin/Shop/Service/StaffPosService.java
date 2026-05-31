package com.example.Admin.Shop.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Admin.Shop.Dto.PosOrderItemRequest;
import com.example.Admin.Shop.Dto.PosOrderQuoteResponse;
import com.example.Admin.Shop.Dto.PosOrderRequest;
import com.example.Admin.Shop.Model.InventoryHistoryType;
import com.example.Admin.Shop.Model.PaymentMethod;
import com.example.Admin.Shop.Model.PaymentStatus;
import com.example.Admin.Shop.Model.ShopInventoryHistory;
import com.example.Admin.Shop.Model.ShopOrder;
import com.example.Admin.Shop.Model.ShopOrderItem;
import com.example.Admin.Shop.Model.ShopOrderStatus;
import com.example.Admin.Shop.Model.ShopOrderType;
import com.example.Admin.Shop.Model.ShopProduct;
import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Repository.ShopInventoryHistoryRepository;
import com.example.Admin.Shop.Repository.ShopOrderRepository;
import com.example.Admin.Shop.Repository.ShopProductRepository;
import com.example.Admin.Shop.Service.ShopCouponService.CouponQuote;

@Service
public class StaffPosService {
    private static final List<PaymentMethod> POS_PAYMENT_METHODS = List.of(
            PaymentMethod.CASH, PaymentMethod.BANK_QR, PaymentMethod.CARD, PaymentMethod.WALLET);
    private static final List<ShopOrderType> POS_ORDER_TYPES = List.of(ShopOrderType.POS, ShopOrderType.TAKE_AWAY);

    private final ShopProductRepository productRepository;
    private final ShopOrderRepository orderRepository;
    private final ShopInventoryHistoryRepository inventoryHistoryRepository;
    private final ShopCouponService couponService;

    public StaffPosService(ShopProductRepository productRepository, ShopOrderRepository orderRepository,
            ShopInventoryHistoryRepository inventoryHistoryRepository, ShopCouponService couponService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
        this.couponService = couponService;
    }

    public List<PaymentMethod> availablePaymentMethods() {
        return POS_PAYMENT_METHODS;
    }

    public List<ShopOrderType> availableOrderTypes() {
        return POS_ORDER_TYPES;
    }

    public PosOrderQuoteResponse calculatePosOrderQuote(PosOrderRequest request, ShopUser staffUser) {
        ensureCanUsePos(staffUser);
        List<PosLine> lines = buildLines(request == null ? null : request.items());
        BigDecimal subtotal = subtotal(lines);
        CouponQuote quote = couponService.quote(request == null ? null : request.couponCode(), staffUser, subtotal);
        return new PosOrderQuoteResponse(quote.valid(), quote.message(), subtotal, quote.discountAmount(), quote.totalAmount());
    }

    @Transactional
    public ShopOrder createPosOrder(ShopUser staffUser, PosOrderRequest request) {
        ensureCanUsePos(staffUser);
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu đơn tại quầy không hợp lệ");
        }
        PaymentMethod paymentMethod = request.paymentMethod();
        if (!POS_PAYMENT_METHODS.contains(paymentMethod)) {
            throw new IllegalArgumentException("Phương thức thanh toán tại quầy không hợp lệ");
        }

        List<PosLine> lines = buildLines(request.items());
        BigDecimal subtotal = subtotal(lines);
        CouponQuote quote = couponService.quote(request.couponCode(), staffUser, subtotal);
        if (!quote.valid()) {
            throw new IllegalArgumentException(quote.message());
        }
        if (quote.totalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tổng tiền không hợp lệ");
        }

        LocalDateTime now = LocalDateTime.now();
        ShopOrderType orderType = request.orderType() == ShopOrderType.TAKE_AWAY
                ? ShopOrderType.TAKE_AWAY
                : ShopOrderType.POS;

        ShopOrder order = new ShopOrder();
        order.setOrderCode(nextOrderCode());
        order.setOrderType(orderType);
        order.setUser(staffUser);
        order.setCreatedByStaff(staffUser);
        order.setReceiverName("Khách tại quầy");
        order.setReceiverPhone("0000000000");
        order.setShippingAddress(orderType == ShopOrderType.TAKE_AWAY ? "Bán mang đi" : "Tại quầy");
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaidAt(now);
        order.setCompletedAt(now);
        order.setStatus(ShopOrderStatus.COMPLETED);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(quote.discountAmount());
        order.setTotalAmount(quote.totalAmount());
        order.setCoupon(quote.coupon());

        for (PosLine line : lines) {
            ShopProduct product = line.product();
            product.setQuantity(product.getQuantity() - line.quantity());
            productRepository.save(product);

            ShopOrderItem item = new ShopOrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setPrice(product.getEffectivePrice());
            item.setQuantity(line.quantity());
            item.setTotal(line.total());
            order.getItems().add(item);

            ShopInventoryHistory history = new ShopInventoryHistory();
            history.setProduct(product);
            history.setType(InventoryHistoryType.EXPORT);
            history.setQuantity(line.quantity());
            history.setNote("Bán tại quầy cho đơn " + order.getOrderCode());
            inventoryHistoryRepository.save(history);
        }

        ShopOrder saved = orderRepository.save(order);
        couponService.markUsed(quote.coupon(), staffUser);
        return saved;
    }

    private void ensureCanUsePos(ShopUser user) {
        if (user == null || user.getRole() == null || !user.getRole().canUsePos()) {
            throw new IllegalArgumentException("Bạn không có quyền tạo đơn tại quầy");
        }
    }

    private List<PosLine> buildLines(List<PosOrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn chưa có sản phẩm");
        }

        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (PosOrderItemRequest item : items) {
            if (item == null || item.productId() == null) {
                throw new IllegalArgumentException("Sản phẩm không hợp lệ");
            }
            int quantity = item.quantity() == null ? 0 : item.quantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }
            quantities.merge(item.productId(), quantity, Integer::sum);
        }

        List<PosLine> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            ShopProduct product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));
            if (!product.isActive()) {
                throw new IllegalArgumentException(product.getName() + " đang ngừng bán");
            }
            if (entry.getValue() > product.getQuantity()) {
                throw new IllegalArgumentException(product.getName() + " không đủ tồn kho");
            }
            lines.add(new PosLine(product, entry.getValue(),
                    product.getEffectivePrice().multiply(BigDecimal.valueOf(entry.getValue()))));
        }
        return lines;
    }

    private BigDecimal subtotal(List<PosLine> lines) {
        return lines.stream()
                .map(PosLine::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String nextOrderCode() {
        long nextNumber = orderRepository.count() + 1;
        String orderCode;
        do {
            orderCode = "QLCF" + String.format("%06d", nextNumber++);
        } while (orderRepository.existsByOrderCode(orderCode));
        return orderCode;
    }

    private record PosLine(ShopProduct product, int quantity, BigDecimal total) {
    }
}
