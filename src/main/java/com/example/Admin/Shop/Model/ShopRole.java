package com.example.Admin.Shop.Model;

public enum ShopRole {
    CUSTOMER,
    STAFF,
    MANAGER,
    ADMIN,
    SUPER_ADMIN;

    public boolean isEmployee() {
        return this == STAFF || this == MANAGER || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean isStaffOnly() {
        return this == STAFF;
    }

    public boolean canUseSalesFlow() {
        return this == CUSTOMER || isEmployee();
    }

    public boolean canViewReports() {
        return this == MANAGER || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canManageCatalog() {
        return this == MANAGER || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canManageInventory() {
        return this == MANAGER || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canManagePromotions() {
        return this == MANAGER || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canModerateReviews() {
        return this == MANAGER || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canViewAllOrders() {
        return this == MANAGER || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canCancelOrders() {
        return this == MANAGER || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canManageStaffAccounts() {
        return this == MANAGER || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canAssignRoles() {
        return this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canManageCustomerAccounts() {
        return this == ADMIN || this == SUPER_ADMIN;
    }
}
