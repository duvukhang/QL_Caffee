(function () {
  function copyText(value, button) {
    function done() {
      var original = button.textContent;
      button.textContent = "Đã copy";
      window.setTimeout(function () {
        button.textContent = original;
      }, 1400);
    }

    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(value).then(done).catch(done);
      return;
    }

    var input = document.createElement("input");
    input.value = value;
    input.setAttribute("readonly", "");
    input.style.position = "fixed";
    input.style.opacity = "0";
    document.body.appendChild(input);
    input.select();
    document.execCommand("copy");
    document.body.removeChild(input);
    done();
  }

  function normalizeText(value) {
    return (value || "")
      .toString()
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "");
  }

  function filterTable(input) {
    var table = document.querySelector(input.dataset.tableSearch);
    if (!table) {
      return;
    }

    var query = normalizeText(input.value.trim());
    var rows = table.querySelectorAll("tbody tr");
    var visibleCount = 0;

    rows.forEach(function (row) {
      var rowText = row.textContent + " ";
      row.querySelectorAll("input, select, textarea").forEach(function (field) {
        rowText += " " + (field.value || "");
        if (field.tagName === "SELECT" && field.selectedOptions.length > 0) {
          rowText += " " + field.selectedOptions[0].textContent;
        }
      });

      var matched = query.length === 0 || normalizeText(rowText).includes(query);
      row.hidden = !matched;
      if (matched) {
        visibleCount += 1;
      }
    });

    var searchForm = input.closest(".table-search");
    var counter = searchForm ? searchForm.querySelector("[data-table-search-count]") : null;
    if (counter) {
      counter.textContent = query.length === 0 ? "" : visibleCount + " kết quả";
    }
  }

  function enhanceTableSearch(input) {
    if (input.closest(".table-search")) {
      filterTable(input);
      return;
    }

    var form = document.createElement("form");
    form.className = "table-search";
    form.setAttribute("role", "search");
    form.addEventListener("submit", function (event) {
      event.preventDefault();
      filterTable(input);
    });

    input.parentNode.insertBefore(form, input);
    form.appendChild(input);

    var searchButton = document.createElement("button");
    searchButton.className = "btn secondary small";
    searchButton.type = "submit";
    searchButton.textContent = "Tìm";
    form.appendChild(searchButton);

    var clearButton = document.createElement("button");
    clearButton.className = "btn secondary small";
    clearButton.type = "button";
    clearButton.textContent = "Xóa";
    clearButton.addEventListener("click", function () {
      input.value = "";
      filterTable(input);
      input.focus();
    });
    form.appendChild(clearButton);

    var counter = document.createElement("span");
    counter.className = "muted table-search-count";
    counter.dataset.tableSearchCount = "true";
    counter.setAttribute("aria-live", "polite");
    form.appendChild(counter);

    input.addEventListener("input", function () {
      filterTable(input);
    });
    filterTable(input);
  }

  function setupPasswordToggles() {
    document.querySelectorAll("[data-password-toggle]").forEach(function (button) {
      var wrapper = button.closest(".password-input");
      var input = wrapper ? wrapper.querySelector("input[type='password'], input[type='text']") : null;
      if (!input) {
        return;
      }

      button.addEventListener("click", function () {
        var showing = input.type === "text";
        input.type = showing ? "password" : "text";
        button.textContent = showing ? "Hiện" : "Ẩn";
        button.setAttribute("aria-label", showing ? "Hiện mật khẩu" : "Ẩn mật khẩu");
      });
      button.setAttribute("aria-label", "Hiện mật khẩu");
    });
  }

  window.quickView = function (button) {
    var dialog = document.getElementById("quickViewDialog");
    if (!dialog) {
      return;
    }

    var image = document.getElementById("quickImg");
    var name = document.getElementById("quickName");
    var price = document.getElementById("quickPrice");
    var description = document.getElementById("quickDesc");

    if (image) {
      image.src = button.dataset.img || "/img/no-image.png";
      image.alt = button.dataset.name || "";
    }
    if (name) {
      name.textContent = button.dataset.name || "";
    }
    if (price) {
      price.textContent = button.dataset.price || "";
    }
    if (description) {
      description.textContent = button.dataset.desc || "";
    }

    if (typeof dialog.showModal === "function") {
      dialog.showModal();
    }
  };

  function setupConfirmations() {
    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
      form.addEventListener("submit", function (event) {
        var message = form.dataset.confirm || "Bạn có chắc muốn thực hiện thao tác này?";
        if (!window.confirm(message)) {
          event.preventDefault();
        }
      });
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-copy-code]").forEach(function (button) {
      button.addEventListener("click", function () {
        copyText(button.dataset.copyCode || "", button);
      });
    });
    setupConfirmations();
    setupPasswordToggles();
    document.querySelectorAll("input[data-table-search]").forEach(enhanceTableSearch);
  });
})();
