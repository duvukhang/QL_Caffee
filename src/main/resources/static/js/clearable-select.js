(function () {
  function ensureButton(wrapper) {
    var button = wrapper.querySelector(".clear-select-btn");
    if (button) {
      return button;
    }

    button = document.createElement("button");
    button.type = "button";
    button.className = "clear-select-btn";
    button.setAttribute("aria-label", "Xóa lựa chọn");
    button.textContent = "×";
    wrapper.appendChild(button);
    return button;
  }

  function updateState(wrapper) {
    var select = wrapper.querySelector("select");
    if (!select) {
      return;
    }
    wrapper.classList.toggle("has-value", Boolean(select.value));
  }

  function requestFormSubmit(form) {
    if (typeof form.requestSubmit === "function") {
      form.requestSubmit();
    } else {
      form.submit();
    }
  }

  function submitParentForm(element) {
    var form = element.closest("form[data-auto-submit='true']");
    if (form) {
      requestFormSubmit(form);
    }
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".clearable-select").forEach(function (wrapper) {
      var select = wrapper.querySelector("select");
      if (!select) {
        return;
      }
      var clearButton = ensureButton(wrapper);

      updateState(wrapper);
      select.addEventListener("change", function () {
        updateState(wrapper);
        submitParentForm(select);
      });

      clearButton.addEventListener("click", function () {
        select.value = "";
        select.dispatchEvent(new Event("change", { bubbles: true }));
        if (wrapper.dataset.submitOnClear === "true" && !select.closest("form[data-auto-submit='true']")) {
          var form = select.closest("form");
          if (form) {
            requestFormSubmit(form);
          }
        }
      });
    });
  });
})();
