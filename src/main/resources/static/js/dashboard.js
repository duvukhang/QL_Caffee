let isAuthenticated = false;
let products = [];

function normalizeProducts(rawData) {
    if (Array.isArray(rawData)) {
        return rawData;
    }

    if (rawData && Array.isArray(rawData.items)) {
        return rawData.items.map(item => item.Value || item);
    }

    if (rawData && Array.isArray(rawData.data)) {
        return rawData.data;
    }

    return [];
}

function resolveProductImage(product) {
    const imagePath = product?.img || product?.imageUrl || "";

    if (!imagePath) {
        return "/uploads/default.png";
    }

    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
        return imagePath;
    }

    if (imagePath.startsWith("/uploads/")) {
        return imagePath;
    }

    if (imagePath.startsWith("uploads/")) {
        return `/${imagePath}`;
    }

    return `/uploads/${imagePath}`;
}

function getProductStatus(product) {
    const status = (product?.status || "").toString().trim();

    if (!status) {
        return { label: "Đang kinh doanh", className: "default" };
    }

    if (status.toLowerCase().includes("ngưng") || status.toLowerCase().includes("không")) {
        return { label: status, className: "warning" };
    }

    return { label: status, className: "success" };
}

function renderLoadingState() {
    const tbody = document.getElementById("product-table-body");
    tbody.innerHTML = `
        <tr class="loading-row">
            <td colspan="5">
                <div class="loading-row-inner">
                    <span class="spinner"></span>
                    <span>Đang tải dữ liệu sản phẩm...</span>
                </div>
            </td>
        </tr>`;
}

function renderEmptyState() {
    const tbody = document.getElementById("product-table-body");
    tbody.innerHTML = `
        <tr>
            <td colspan="5">
                <div class="table-empty">
                    <div class="table-empty-card">
                        <div class="table-empty-icon"><i class="fa-solid fa-box-open"></i></div>
                        <div>
                            <div class="table-error-title">Chưa có dữ liệu sản phẩm</div>
                            <div class="table-error-copy">Hiện tại hệ thống chưa trả về sản phẩm nào để hiển thị trên dashboard.</div>
                        </div>
                    </div>
                </div>
            </td>
        </tr>`;
}

function renderErrorState(message = "Không thể kết nối dữ liệu sản phẩm") {
    const tbody = document.getElementById("product-table-body");
    tbody.innerHTML = `
        <tr>
            <td colspan="5">
                <div class="table-error">
                    <div class="table-error-card">
                        <div class="table-error-icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
                        <div>
                            <div class="table-error-title">${message}</div>
                            <div class="table-error-copy">Vui lòng kiểm tra backend hoặc thử lại để tải danh sách sản phẩm.</div>
                            <button class="retry-btn" type="button" onclick="loadProductsFromAPI()">Thử lại</button>
                        </div>
                    </div>
                </div>
            </td>
        </tr>`;
}

function renderProductsToTable(listData) {
    const tbody = document.getElementById("product-table-body");
    const demoList = listData.slice(0, 5);

    if (demoList.length === 0) {
        renderEmptyState();
        return;
    }

    tbody.innerHTML = demoList.map(product => {
        const status = getProductStatus(product);
        return `
            <tr>
                <td>
                    <img src="${resolveProductImage(product)}" alt="${product.productName || 'Sản phẩm'}" class="product-image" onerror="this.onerror=null; this.src='/uploads/default.png'">
                </td>
                <td><span class="product-id">${product.productId || "—"}</span></td>
                <td><div class="product-name">${product.productName || "Sản phẩm chưa đặt tên"}</div></td>
                <td><div class="product-price">${Number(product.price || 0).toLocaleString('vi-VN')}đ</div></td>
                <td><span class="status-pill ${status.className}">${status.label}</span></td>
            </tr>`;
    }).join("");
}

function verifyAuthentication() {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");
    const accountType = localStorage.getItem("accountType");
    const isCustomerSession = role === "Customer" || accountType === "Customer";
    const isAdminSession = Boolean(token) && !isCustomerSession;
    const loginAlert = document.getElementById("login-alert");
    const authSection = document.getElementById("auth-section");

    if (!isAdminSession) {
        isAuthenticated = false;
        loginAlert.classList.remove("hidden");

        authSection.innerHTML = `
            <a href="/login.html" class="header-login-btn">
                <i class="fa-solid fa-right-to-bracket"></i>
                Đăng nhập
            </a>`;

        const panels = ["panel-admin", "panel-manager", "panel-cashier"];
        panels.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.style.opacity = "0.45";
                el.style.cursor = "not-allowed";
            }
        });
        return;
    }

    isAuthenticated = true;
    loginAlert.classList.add("hidden");

    let displayName = localStorage.getItem("displayName") || "Quản trị viên";
    try {
        const payloadBase64 = token.split(".")[1];
        const decodedPayload = JSON.parse(atob(payloadBase64));
        displayName = localStorage.getItem("displayName") || decodedPayload.userName || decodedPayload.sub || displayName;
    } catch (error) {
        console.warn("Không thể giải mã token", error);
    }

    authSection.innerHTML = `
        <div class="group relative">
            <div class="user-pill cursor-pointer">
                <div class="user-avatar">${displayName.charAt(0).toUpperCase()}</div>
                <div class="user-meta hidden sm:flex">
                    <span>${displayName}</span>
                    <span>Quản trị viên K-STORE</span>
                </div>
                <i class="fa-solid fa-chevron-down text-xs text-slate-500"></i>
            </div>

            <div class="absolute right-0 top-14 w-56 rounded-2xl border border-slate-100 bg-white/95 shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 overflow-hidden z-50">
                <a href="/profile.html" class="flex items-center gap-3 px-4 py-3 text-sm text-slate-700 hover:bg-stone-50 transition">
                    <i class="fa-solid fa-user"></i>
                    Hồ sơ của tôi
                </a>
                <button onclick="logout()" class="w-full flex items-center gap-3 px-4 py-3 text-sm text-red-600 hover:bg-red-50 transition text-left font-medium">
                    <i class="fa-solid fa-arrow-right-from-bracket"></i>
                    Đăng xuất
                </button>
            </div>
        </div>`;
}

function setupPanelClickEvents() {
    const panels = ["panel-admin", "panel-manager", "panel-cashier"];

    panels.forEach(panelId => {
        const panel = document.getElementById(panelId);
        if (!panel) {
            return;
        }

        panel.addEventListener("click", function (event) {
            if (!isAuthenticated) {
                event.preventDefault();
                event.stopPropagation();

                const loginAlert = document.getElementById("login-alert");
                loginAlert.classList.add("animate-bounce");
                setTimeout(() => loginAlert.classList.remove("animate-bounce"), 1000);

                window.scrollTo({ top: 0, behavior: "smooth" });
                showToast("Vui lòng đăng nhập để sử dụng chức năng này", "error");
            }
        });
    });
}

async function loadProductsFromAPI() {
    renderLoadingState();

    try {
        const response = await fetch("/public/Product/1/100");

        if (!response.ok) {
            throw new Error("HTTP " + response.status);
        }

        const rawData = await response.json();
        const normalizedProducts = normalizeProducts(rawData);

        if (!normalizedProducts.length) {
            renderEmptyState();
            return;
        }

        products = normalizedProducts;
        renderProductsToTable(products);
    } catch (error) {
        console.error("Lỗi API Sản phẩm:", error);
        renderErrorState("Không thể kết nối dữ liệu sản phẩm");
    }
}

function loadDashboardStatistics() {
    console.log("Hệ thống đang chuẩn bị kết nối tải KPI...");
}

function showToast(message, type = "success") {
    const container = document.getElementById("toast-container");
    const toast = document.createElement("div");
    const isSuccess = type === "success";

    toast.className = `flex items-center gap-3 text-white px-5 py-3.5 rounded-xl shadow-2xl toast-enter ${isSuccess ? "bg-green-600" : "bg-red-600"}`;
    toast.innerHTML = `<i class="fa-solid ${isSuccess ? "fa-circle-check" : "fa-circle-exclamation"} text-xl"></i> <span class="font-medium">${message}</span>`;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.transition = "opacity 0.3s ease, transform 0.3s ease";
        toast.style.opacity = "0";
        toast.style.transform = "translateX(100%)";
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("accountType");
    localStorage.removeItem("displayName");
    localStorage.removeItem("customerId");
    window.location.reload();
}

document.addEventListener("DOMContentLoaded", () => {
    verifyAuthentication();
    setupPanelClickEvents();

    if (isAuthenticated) {
        loadProductsFromAPI();
        loadDashboardStatistics();
    }
});
