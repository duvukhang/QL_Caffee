(function () {
  var labels = {
    PENDING: "Chờ xác nhận",
    PENDING_PAYMENT: "Chờ thanh toán",
    WAITING_CONFIRMATION: "Chờ xác nhận",
    CONFIRMED: "Đã xác nhận",
    SHIPPING: "Đang giao",
    DELIVERING: "Đang giao",
    COMPLETED: "Hoàn thành",
    DELIVERED: "Hoàn thành",
    CANCELLED: "Đã hủy",
    CANCELED: "Đã hủy",
    UNPAID: "Chưa thanh toán",
    PAID: "Đã thanh toán",
    FAILED: "Thanh toán thất bại",
    COD: "Thanh toán khi nhận hàng",
    BANK_QR_MANUAL: "Chuyển khoản QR",
    CARD: "Thẻ ngân hàng",
    WALLET: "Ví điện tử",
    E_WALLET: "Ví điện tử",
    ACTIVE: "Đang hoạt động",
    INACTIVE: "Ngừng hoạt động",
    APPROVED: "Đã duyệt",
    PENDING_REVIEW: "Chờ duyệt",
    REJECTED: "Từ chối",
    PERCENT: "Giảm theo phần trăm",
    FIXED_AMOUNT: "Giảm số tiền",
    IMPORT: "Nhập kho",
    EXPORT: "Xuất kho",
    ORDER_CANCEL: "Hoàn kho đơn hủy",
    CUSTOMER: "Khách hàng",
    STAFF: "Nhân viên",
    ADMIN: "Quản trị viên",
    MANAGER: "Quản lý",
    SUPER_ADMIN: "Quản trị hệ thống"
  };

  function label(value) {
    if (!value) {
      return "";
    }
    return labels[value] || value.replace(/_/g, " ").toLowerCase();
  }

  function init() {
    document.querySelectorAll("[data-status-label]").forEach(function (node) {
      node.textContent = label(node.dataset.statusLabel);
    });

    document.querySelectorAll("select[data-enum-select] option").forEach(function (option) {
      if (option.value) {
        option.textContent = label(option.value);
      }
    });
  }

  window.ShopStatusLabels = { label: label };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
