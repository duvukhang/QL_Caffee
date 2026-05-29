package com.example.Admin.Service.CustomerSite;

import com.example.Admin.Models.Cart;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CartSessionService {

    private static final String CART_KEY = "Cart";

    public Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(CART_KEY);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(CART_KEY, cart);
        }
        return cart;
    }

    public void saveCart(HttpSession session, Cart cart) {
        session.setAttribute(CART_KEY, cart);
    }

    public void clear(HttpSession session) {
        session.removeAttribute(CART_KEY);
    }
}
