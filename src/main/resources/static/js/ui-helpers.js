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

  function setupCopyButtons() {
    document.querySelectorAll("[data-copy-code]").forEach(function (button) {
      button.addEventListener("click", function () {
        copyText(button.dataset.copyCode || "", button);
      });
    });
  }

  function setupTableSearch() {
    document.querySelectorAll("[data-table-search]").forEach(function (input) {
      var table = document.querySelector(input.dataset.tableSearch);
      if (!table) {
        return;
      }

      input.addEventListener("input", function () {
        var keyword = input.value.trim().toLowerCase();
        table.querySelectorAll("tbody tr").forEach(function (row) {
          row.hidden = keyword !== "" && !row.textContent.toLowerCase().includes(keyword);
        });
      });
    });
  }

  window.quickView = function (button) {
    var dialog = document.getElementById("quickViewDialog");
    if (!dialog) {
      return;
    }

    document.getElementById("quickImg").src = button.dataset.img || "/img/no-image.png";
    document.getElementById("quickName").textContent = button.dataset.name || "";
    document.getElementById("quickPrice").textContent = button.dataset.price || "";
    document.getElementById("quickDesc").textContent = button.dataset.desc || "";
    dialog.showModal();
  };

  function init() {
    setupCopyButtons();
    setupTableSearch();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
