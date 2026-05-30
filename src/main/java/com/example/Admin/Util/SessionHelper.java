package com.example.Admin.Util;

import com.example.Admin.Models.Customer;
import jakarta.servlet.http.HttpSession;

public final class SessionHelper {

    public static final String USER_ID = "UserId";
    public static final String USER_ID_LOWER = "userId";
    public static final String CUSTOMER_ID = "customerId";
    public static final String USER_NAME = "userName";
    public static final String CUSTOMER = "customer";

    private SessionHelper() {
    }

    public static void login(HttpSession session, Customer customer) {
        if (session == null || customer == null) {
            return;
        }

        String id = customer.getCustomerId();
        session.setAttribute(USER_ID, id);
        session.setAttribute(USER_ID_LOWER, id);
        session.setAttribute(CUSTOMER_ID, id);
        session.setAttribute(USER_NAME, customer.getUserName());
        session.setAttribute(CUSTOMER, customer);
    }

    public static String getCustomerId(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(USER_ID);
        if (value == null) {
            value = session.getAttribute(USER_ID_LOWER);
        }
        if (value == null) {
            value = session.getAttribute(CUSTOMER_ID);
        }
        if (value == null) {
            Object customer = session.getAttribute(CUSTOMER);
            if (customer instanceof Customer currentCustomer) {
                value = currentCustomer.getCustomerId();
            }
        }

        return value == null ? null : value.toString();
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getCustomerId(session) != null;
    }
}
