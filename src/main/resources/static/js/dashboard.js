let isAuthenticated = false;
let products = [];

document.addEventListener("DOMContentLoaded", () => {
    verifyAuthentication();
    setupPanelClickEvents();

    if (isAuthenticated) {
        loadProductsFromAPI();
        loadDashboardStatistics();
    }
});

function verifyAuthentication() {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");
    const isAdminSession = token && role !== "Customer";
    const loginAlert = document.getElementById("login-alert");
    const authSection = document.getElementById("auth-section");

    if (!isAdminSession) {
        isAuthenticated = false;
        loginAlert.classList.remove("hidden");
        authSection.innerHTML = `<a href="admin/login.html" class="bg-blue-600 text-white px-6 py-2 rounded-full font-semibold hover:bg-blue-700 hover:shadow-md transition">Dang nhap</a>`;

        ["panel-admin", "panel-manager", "panel-cashier"].forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.style.opacity = "0.4";
                el.style.cursor = "not-allowed";
            }
        });
        return;
    }

    isAuthenticated = true;
    loginAlert.classList.add("hidden");

    let displayName = localStorage.getItem("displayName") || "Quan tri vien";
    try {
        const payloadBase64 = token.split(".")[1];
        const decodedPayload = JSON.parse(atob(payloadBase64));
        displayName = localStorage.getItem("displayName") || decodedPayload.userName || decodedPayload.sub || displayName;
    } catch (e) {}

    authSection.innerHTML = `
        <div class="group relative cursor-pointer flex items-center gap-3 bg-indigo-800 hover:bg-indigo-700 px-4 py-2 rounded-lg transition border border-indigo-600">
            <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=f59e0b&color=fff" class="w-8 h-8 rounded-full shadow-sm">
            <span class="font-semibold text-sm hidden sm:block">${displayName}</span>
            <i class="fa-solid fa-chevron-down text-xs text-indigo-300"></i>
            <div class="absolute right-0 top-12 w-56 bg-white border rounded-xl shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 overflow-hidden z-50 text-gray-800">
                <a href="profile.html" class="flex items-center gap-3 px-4 py-3 text-sm hover:bg-gray-50 transition"><i class="fa-solid fa-user w-4 text-gray-400"></i> Ho so cua toi</a>
                <hr class="border-gray-100">
                <button onclick="logout()" class="w-full flex items-center gap-3 px-4 py-3 text-sm text-red-600 hover:bg-red-50 transition text-left font-medium"><i class="fa-solid fa-arrow-right-from-bracket w-4"></i> Dang xuat</button>
            </div>
        </div>`;
}

function setupPanelClickEvents() {
    ["panel-admin", "panel-manager", "panel-cashier"].forEach(panelId => {
        const panel = document.getElementById(panelId);
        if (!panel) return;

        panel.addEventListener("click", function(e) {
            if (!isAuthenticated) {
                e.preventDefault();
                e.stopPropagation();
                const loginAlert = document.getElementById("login-alert");
                loginAlert.classList.add("animate-bounce");
                setTimeout(() => loginAlert.classList.remove("animate-bounce"), 1000);
                window.scrollTo({ top: 0, behavior: "smooth" });
                showToast("Vui long dang nhap admin de su dung chuc nang nay", "error");
            }
        });
    });
}

async function loadProductsFromAPI() {
    try {
        const response = await fetch("/public/Product/1/5");
        if (!response.ok || response.status === 204) {
            renderProductsToTable([]);
            return;
        }

        const resData = await response.json();
        products = (resData.items || []).map(item => item.Value || item.value || {});
        renderProductsToTable(products);
    } catch (error) {
        console.error("Product API error:", error);
        document.getElementById("product-table-body").innerHTML = `<tr><td colspan="4" class="p-4 text-center text-red-500">Loi ket noi Backend!</td></tr>`;
    }
}

function renderProductsToTable(listData) {
    const tbody = document.getElementById("product-table-body");
    const demoList = listData.slice(0, 5);

    if (demoList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="p-4 text-center text-gray-500">Khong co du lieu.</td></tr>`;
        return;
    }

    tbody.innerHTML = demoList.map(p => `
        <tr class="hover:bg-gray-50 transition cursor-pointer">
            <td class="p-3">
                <img src="${p.img || "/uploads/default.png"}" class="w-10 h-10 object-cover rounded-md border" onerror="this.src='/uploads/default.png'">
            </td>
            <td class="p-3 font-mono text-xs text-blue-600">${p.productId || ""}</td>
            <td class="p-3 font-medium text-gray-800">${p.productName || ""}</td>
            <td class="p-3 font-bold text-red-500">${Number(p.price || 0).toLocaleString("vi-VN")}d</td>
        </tr>
    `).join("");
}

function loadDashboardStatistics() {
    console.log("Dashboard ready");
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
