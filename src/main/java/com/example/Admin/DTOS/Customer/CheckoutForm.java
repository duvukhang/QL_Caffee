package com.example.Admin.DTOS.Customer;

import com.example.Admin.Models.CartItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CheckoutForm {

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp ng\u01b0\u1eddi nh\u1eadn")
    private String receiverName;

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp s\u1ed1 \u0111i\u1ec7n tho\u1ea1i")
    private String receiverPhone;

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp \u0111\u1ecba ch\u1ec9")
    private String receiverAddress;

    @Pattern(regexp = "COD|CARD|EWALLET", message = "Ph\u01b0\u01a1ng th\u1ee9c thanh to\u00e1n kh\u00f4ng h\u1ee3p l\u1ec7")
    private String paymentMethod = "COD";

    private String note;
    private List<CartItem> cartItems = new ArrayList<>();
    private BigDecimal grandTotal = BigDecimal.ZERO;

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = trim(receiverName);
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = trim(receiverPhone);
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = trim(receiverAddress);
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = trim(paymentMethod);
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = trim(note);
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems == null ? new ArrayList<>() : cartItems;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal == null ? BigDecimal.ZERO : grandTotal;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
