// Biến toàn cục
let isAuthenticated = false;
let products = []; 

// Khởi chạy khi DOM tải xong
document.addEventListener("DOMContentLoaded", () => {
    verifyAuthentication();
    setupPanelClickEvents();
    
    // Nếu đã đăng nhập thành công mới tiến hành gọi API để hiển thị dữ liệu
    if (isAuthenticated) {
        loadProductsFromAPI();
        loadDashboardStatistics();
    }
});

// --- XÁC THỰC VÀ HIỂN THỊ ACCOUNT ---
function verifyAuthentication() {
    const token = localStorage.getItem("token");
    const loginAlert = document.getElementById("login-alert");
    const authSection = document.getElementById("auth-section");

    if (!token) {
        isAuthenticated = false;
        loginAlert.classList.remove("hidden");
        
        // Hiển thị nút đăng nhập trên Header
        authSection.innerHTML = `<a href="login.html" class="bg-blue-600 text-white px-6 py-2 rounded-full font-semibold hover:bg-blue-700 hover:shadow-md transition">Đăng nhập</a>`;
        
        // Làm mờ và đổi con trỏ chuột ở các Panel chức năng
        const panels = ['panel-admin', 'panel-manager', 'panel-cashier'];
        panels.forEach(id => {
            const el = document.getElementById(id);
            if(el) {
                el.style.opacity = "0.4";
                el.style.cursor = "not-allowed";
            }
        });
    } else {
        isAuthenticated = true;
        loginAlert.classList.add("hidden");
        
        // Giải mã cơ bản Payload của JWT (Lấy Tên hoặc Role)
        let displayName = "Quản trị viên";
        try {
            const payloadBase64 = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payloadBase64));
            if(decodedPayload.sub) displayName = decodedPayload.sub;
        } catch(e) {}

        // Hiển thị Dropdown Profile trên Header
        authSection.innerHTML = `
            <div class="group relative cursor-pointer flex items-center gap-3 bg-indigo-800 hover:bg-indigo-700 px-4 py-2 rounded-lg transition border border-indigo-600">
                <img src="https://ui-avatars.com/api/?name=${displayName}&background=f59e0b&color=fff" class="w-8 h-8 rounded-full shadow-sm">
                <span class="font-semibold text-sm hidden sm:block">${displayName}</span>
                <i class="fa-solid fa-chevron-down text-xs text-indigo-300"></i>
                
                <div class="absolute right-0 top-12 w-56 bg-white border rounded-xl shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 overflow-hidden z-50 text-gray-800">
                    <a href="profile.html" class="flex items-center gap-3 px-4 py-3 text-sm hover:bg-gray-50 transition"><i class="fa-solid fa-user w-4 text-gray-400"></i> Hồ sơ của tôi</a>
                    <hr class="border-gray-100">
                    <button onclick="logout()" class="w-full flex items-center gap-3 px-4 py-3 text-sm text-red-600 hover:bg-red-50 transition text-left font-medium"><i class="fa-solid fa-arrow-right-from-bracket w-4"></i> Đăng xuất</button>
                </div>
            </div>`;
    }
}

// --- BẮT SỰ KIỆN CLICK CHẶN PANEL ---
function setupPanelClickEvents() {
    const panels = ['panel-admin', 'panel-manager', 'panel-cashier'];
    
    panels.forEach(panelId => {
        const panel = document.getElementById(panelId);
        if (panel) {
            panel.addEventListener('click', function(e) {
                if (!isAuthenticated) {
                    // Ngăn chặn chuyển trang nếu chưa đăng nhập
                    e.preventDefault(); 
                    e.stopPropagation();
                    
                    // Rung cảnh báo
                    const loginAlert = document.getElementById("login-alert");
                    loginAlert.classList.add('animate-bounce');
                    setTimeout(() => loginAlert.classList.remove('animate-bounce'), 1000);
                    
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                    showToast("Vui lòng đăng nhập để sử dụng chức năng này", "error");
                }
            });
        }
    });
}

// --- GỌI API SẢN PHẨM & RENDER ---
async function loadProductsFromAPI() {
    try {
        const response = await fetch("http://localhost:8080/public/api/products");
        const resData = await response.json();
        
        if (resData.status === "success") {
            products = resData.data;
            renderProductsToTable(products);
        }
    } catch (error) {
        console.error("Lỗi API Sản phẩm:", error);
        document.getElementById("product-table-body").innerHTML = `<tr><td colspan="4" class="p-4 text-center text-red-500">Lỗi kết nối Backend!</td></tr>`;
    }
}

function renderProductsToTable(listData) {
    const tbody = document.getElementById("product-table-body");
    
    // Chỉ hiển thị 5 sản phẩm demo lên Dashboard
    const demoList = listData.slice(0, 5); 
    
    if (demoList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="p-4 text-center text-gray-500">Không có dữ liệu.</td></tr>`;
        return;
    }
    
    tbody.innerHTML = demoList.map(p => `
        <tr class="hover:bg-gray-50 transition cursor-pointer">
            <td class="p-3">
                <img src="${p.imageUrl}" class="w-10 h-10 object-cover rounded-md border" onerror="this.src='http://localhost:8080/uploads/default.png'">
            </td>
            <td class="p-3 font-mono text-xs text-blue-600">${p.productId}</td>
            <td class="p-3 font-medium text-gray-800">${p.productName}</td>
            <td class="p-3 font-bold text-red-500">${Number(p.price).toLocaleString('vi-VN')}đ</td>
        </tr>
    `).join('');
}

// --- LOAD SỐ LIỆU ĐỘNG (Ví dụ) ---
function loadDashboardStatistics() {
    // Đoạn này sau này bạn viết Fetch API lấy số lượng Đơn hàng, Doanh thu từ Backend
    console.log("Hệ thống đang chuẩn bị kết nối tải KPI...");
}

// --- TOAST VÀ ĐĂNG XUẤT ---
function showToast(message, type = "success") {
    const container = document.getElementById("toast-container");
    const toast = document.createElement("div");
    const isSuccess = type === "success";
    
    toast.className = `flex items-center gap-3 text-white px-5 py-3.5 rounded-xl shadow-2xl toast-enter ${isSuccess ? "bg-green-600" : "bg-red-600"}`;
    toast.innerHTML = `<i class="fa-solid ${isSuccess ? "fa-circle-check" : "fa-circle-exclamation"} text-xl"></i> <span class="font-medium">${message}</span>`;
    
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function logout() {
    localStorage.removeItem("token");
    window.location.reload();
}