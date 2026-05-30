function loadStaffs() {
    fetch('/manager/Staff/all', { 
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem("token") } 
    })
    .then(res => res.json())
    .then(data => {
        const tbody = document.getElementById("staff-tbody");
        tbody.innerHTML = "";
        data.forEach(s => {
            tbody.innerHTML += `
                <tr>
                    <td><b>${s.userId}</b></td>
                    <td>${s.fullName}</td>
                    <td><span style="color:#2563eb;font-weight:600;">${s.roleName}</span></td>
                    <td>${s.storeId}</td>
                    <td>
                        <button class="btn btn-edit" onclick="editStaff('${s.userId}', '${s.fullName}', '${s.roleName}', '${s.storeId}')">Sửa</button>
                        <button class="btn btn-delete" onclick="deleteStaff('${s.userId}')">Xóa</button>
                    </td>
                </tr>
            `;
        });
    });
}

function loadRolesAndStores() {
    const roleSelect = document.getElementById('ddlRole');
    const storeSelect = document.getElementById('ddlStoreId');

    Promise.all([
        fetch('/public/Roles').then(r => r.ok ? r.json() : []),
        fetch('/public/Store').then(r => r.ok ? r.json() : [])
    ]).then(([roles, stores]) => {
        roleSelect.innerHTML = '';
        if (!roles.length) {
            roleSelect.innerHTML = '<option value="">Không tìm thấy quyền</option>';
        } else {
            roleSelect.innerHTML = '<option value="">Chọn quyền</option>' + roles.map(role => `
                <option value="${role.roleId}">${role.roleName}</option>
            `).join('');
        }

        storeSelect.innerHTML = '';
        if (!stores.length) {
            storeSelect.innerHTML = '<option value="">Không tìm thấy chi nhánh</option>';
        } else {
            storeSelect.innerHTML = '<option value="">Chọn chi nhánh</option>' + stores.map(store => `
                <option value="${store.StoreId}">${store.StoreName}</option>
            `).join('');
        }
    }).catch(() => {
        roleSelect.innerHTML = '<option value="">Lỗi tải quyền</option>';
        storeSelect.innerHTML = '<option value="">Lỗi tải chi nhánh</option>';
    });
}

function saveStaff() {
    const id = document.getElementById("txtStaffIdHidden").value;
    const bodyData = {
        fullName: document.getElementById("txtName").value,
        roleName: document.getElementById("ddlRole").value,
        storeId: document.getElementById("txtStoreId").value
    };

    const url = id ? `/manager/Staff/update/${id}` : '/manager/Staff/createJson';
    const method = id ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: { 
            'Content-Type': 'application/json', 
            'Authorization': 'Bearer ' + localStorage.getItem("token") 
        },
        body: JSON.stringify(bodyData)
    }).then(() => { 
        alert("Thao tác dữ liệu nhân sự thành công!"); 
        cancelEditMode(); 
        loadStaffs(); 
    });
}

function editStaff(id, name, role, store) {
    document.getElementById("form-title").innerText = "⚙️ HIỆU CHỈNH NHÂN VIÊN";
    document.getElementById("txtStaffIdHidden").value = id;
    document.getElementById("txtName").value = name;
    document.getElementById("ddlRole").value = role;
    document.getElementById("ddlStoreId").value = store;
    document.getElementById("btnCancelEdit").style.display = "block";
}

function cancelEditMode() {
    document.getElementById("form-title").innerText = "➕ THÊM NHÂN VIÊN MỚI";
    document.getElementById("staffForm").reset();
    document.getElementById("txtStaffIdHidden").value = "";
    document.getElementById("ddlRole").selectedIndex = 0;
    document.getElementById("ddlStoreId").selectedIndex = 0;
    document.getElementById("btnCancelEdit").style.display = "none";
}

function deleteStaff(id) {
    if(confirm("Xác nhận xóa tài khoản nhân sự này?")) {
        fetch(`/manager/Staff/delete/${id}`, { 
            method: 'DELETE', 
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem("token") } 
        })
        .then(() => { alert("Đã xóa!"); loadStaffs(); });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadRolesAndStores();
    loadStaffs();
});