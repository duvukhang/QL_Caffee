(function () {
  var cart = new Map();
  var formatter = new Intl.NumberFormat("vi-VN");

  function money(value) {
    return formatter.format(Math.max(0, Number(value || 0))) + " đ";
  }

  function normalize(value) {
    return (value || "")
      .toString()
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "");
  }

  function itemsPayload() {
    return Array.from(cart.values()).map(function (item) {
      return { productId: item.id, quantity: item.quantity };
    });
  }

  function localSubtotal() {
    return Array.from(cart.values()).reduce(function (sum, item) {
      return sum + item.price * item.quantity;
    }, 0);
  }

  function syncHiddenInputs() {
    var holder = document.getElementById("posHiddenItems");
    if (!holder) {
      return;
    }
    holder.innerHTML = "";
    cart.forEach(function (item) {
      var productInput = document.createElement("input");
      productInput.type = "hidden";
      productInput.name = "productId";
      productInput.value = item.id;
      holder.appendChild(productInput);

      var quantityInput = document.createElement("input");
      quantityInput.type = "hidden";
      quantityInput.name = "quantity";
      quantityInput.value = item.quantity;
      holder.appendChild(quantityInput);
    });
  }

  function renderTicket() {
    var list = document.getElementById("posTicketList");
    var submit = document.getElementById("posSubmit");
    if (!list || !submit) {
      return;
    }

    list.innerHTML = "";
    if (cart.size === 0) {
      list.innerHTML = '<p class="muted">Chưa có sản phẩm nào trong hóa đơn.</p>';
      submit.disabled = true;
    } else {
      submit.disabled = false;
      cart.forEach(function (item) {
        var row = document.createElement("div");
        row.className = "pos-ticket-item";
        row.innerHTML =
          '<div class="pos-ticket-main">' +
          '<strong></strong>' +
          '<span></span>' +
          "</div>" +
          '<div class="pos-qty-control">' +
          '<button type="button" data-action="minus">-</button>' +
          '<input type="number" min="1">' +
          '<button type="button" data-action="plus">+</button>' +
          '<button class="pos-remove" type="button" data-action="remove">Xóa</button>' +
          "</div>";
        row.dataset.id = item.id;
        row.querySelector("strong").textContent = item.name;
        row.querySelector("span").textContent = money(item.price * item.quantity);
        row.querySelector("input").value = item.quantity;
        row.querySelector("input").max = item.stock;
        list.appendChild(row);
      });
    }

    syncHiddenInputs();
    updateTotals();
  }

  function updateTotals() {
    var subtotal = localSubtotal();
    document.getElementById("posSubtotal").textContent = money(subtotal);
    document.getElementById("posDiscount").textContent = money(0);
    document.getElementById("posTotal").textContent = money(subtotal);

    if (cart.size === 0) {
      return;
    }

    fetch("/staff/pos/quote", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        items: itemsPayload(),
        couponCode: document.getElementById("posCoupon").value,
        paymentMethod: document.getElementById("posPaymentMethod").value || "CASH",
        orderType: document.getElementById("posOrderType").value || "POS"
      })
    })
      .then(function (response) { return response.json(); })
      .then(function (quote) {
        document.getElementById("posSubtotal").textContent = money(quote.subtotal);
        document.getElementById("posDiscount").textContent = money(quote.discountAmount);
        document.getElementById("posTotal").textContent = money(quote.totalAmount);
        document.getElementById("posCouponMessage").textContent = quote.message || "";
        document.getElementById("posCouponMessage").className = quote.valid ? "muted status-success-text" : "muted status-danger-text";
      })
      .catch(function () {
        document.getElementById("posCouponMessage").textContent = "";
      });
  }

  function addProduct(card) {
    var id = card.dataset.id;
    var stock = Number(card.dataset.stock || 0);
    if (stock <= 0) {
      return;
    }
    var existing = cart.get(id);
    if (existing && existing.quantity >= stock) {
      window.alert("Không thể thêm quá tồn kho");
      return;
    }
    cart.set(id, {
      id: id,
      name: card.dataset.name,
      price: Number(card.dataset.price || 0),
      stock: stock,
      quantity: existing ? existing.quantity + 1 : 1
    });
    renderTicket();
  }

  function filterProducts() {
    var query = normalize(document.getElementById("posSearch").value);
    var category = normalize(document.getElementById("posCategory").value);
    document.querySelectorAll(".pos-product-card").forEach(function (card) {
      var matchedName = normalize(card.dataset.name).includes(query);
      var matchedCategory = category === "" || normalize(card.dataset.category) === category;
      card.hidden = !(matchedName && matchedCategory);
    });
  }

  function init() {
    if (!document.getElementById("posForm")) {
      return;
    }

    document.querySelectorAll(".pos-add-btn").forEach(function (button) {
      button.addEventListener("click", function () {
        addProduct(button.closest(".pos-product-card"));
      });
    });

    document.getElementById("posTicketList").addEventListener("click", function (event) {
      var button = event.target.closest("button[data-action]");
      if (!button) {
        return;
      }
      var row = button.closest(".pos-ticket-item");
      var item = cart.get(row.dataset.id);
      if (!item) {
        return;
      }
      if (button.dataset.action === "plus") {
        if (item.quantity >= item.stock) {
          window.alert("Không thể thêm quá tồn kho");
          return;
        }
        item.quantity += 1;
      } else if (button.dataset.action === "minus") {
        item.quantity -= 1;
        if (item.quantity <= 0) {
          cart.delete(item.id);
        }
      } else if (button.dataset.action === "remove") {
        cart.delete(item.id);
      }
      renderTicket();
    });

    document.getElementById("posTicketList").addEventListener("change", function (event) {
      if (event.target.tagName !== "INPUT") {
        return;
      }
      var row = event.target.closest(".pos-ticket-item");
      var item = cart.get(row.dataset.id);
      var value = Math.max(1, Number(event.target.value || 1));
      if (value > item.stock) {
        value = item.stock;
        window.alert("Không thể thêm quá tồn kho");
      }
      item.quantity = value;
      renderTicket();
    });

    document.getElementById("posSearch").addEventListener("input", filterProducts);
    document.getElementById("posCategory").addEventListener("change", filterProducts);
    document.getElementById("posCoupon").addEventListener("input", updateTotals);
    document.getElementById("posApplyCoupon").addEventListener("click", updateTotals);
    document.getElementById("posPaymentMethod").addEventListener("change", updateTotals);
    document.getElementById("posOrderType").addEventListener("change", updateTotals);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
