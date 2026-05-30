package com.example.Admin.Shop.Service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Admin.Shop.Model.ShopCart;
import com.example.Admin.Shop.Model.ShopCartItem;
import com.example.Admin.Shop.Model.ShopProduct;
import com.example.Admin.Shop.Model.ShopUser;
import com.example.Admin.Shop.Repository.ShopCartItemRepository;
import com.example.Admin.Shop.Repository.ShopCartRepository;
import com.example.Admin.Shop.Repository.ShopProductRepository;

@Service
public class ShopCartService {
    private final ShopCartRepository cartRepository;
    private final ShopCartItemRepository cartItemRepository;
    private final ShopProductRepository productRepository;

    public ShopCartService(ShopCartRepository cartRepository, ShopCartItemRepository cartItemRepository,
            ShopProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public ShopCart getOrCreateCart(ShopUser user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            ShopCart cart = new ShopCart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public void add(ShopUser user, Long productId, int quantity) {
        ShopCart cart = getOrCreateCart(user);
        ShopProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        if (!product.isActive() || product.getQuantity() <= 0) {
            throw new IllegalArgumentException("Sản phẩm hiện không còn hàng");
        }
        int addQty = Math.max(quantity, 1);
        ShopCartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElseGet(() -> {
            ShopCartItem newItem = new ShopCartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(0);
            return newItem;
        });
        int targetQty = item.getQuantity() + addQty;
        if (targetQty > product.getQuantity()) {
            throw new IllegalArgumentException("Số lượng vượt quá tồn kho");
        }
        item.setQuantity(targetQty);
        cartItemRepository.save(item);
    }

    @Transactional
    public void update(Long itemId, int quantity, ShopUser user) {
        ShopCartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ"));
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền cập nhật giỏ này");
        }
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }
        if (quantity > item.getProduct().getQuantity()) {
            throw new IllegalArgumentException("Số lượng vượt quá tồn kho");
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void remove(Long itemId, ShopUser user) {
        ShopCartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ"));
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xóa mục này");
        }
        cartItemRepository.delete(item);
    }

    public BigDecimal subtotal(ShopCart cart) {
        return cart.getItems().stream()
                .map(item -> item.getProduct().getEffectivePrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void clear(ShopCart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
