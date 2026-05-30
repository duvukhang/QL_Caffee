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

  function setup(wrapper) {
    var select = wrapper.querySelector("select");
    if (!select || select.dataset.clearableReady === "true") {
      return;
    }

    var button = ensureButton(wrapper);
    select.dataset.clearableReady = "true";

    function sync() {
      var hasValue = select.value !== "";
      wrapper.classList.toggle("has-value", hasValue);
      button.hidden = !hasValue;
    }

    button.addEventListener("click", function () {
      select.value = "";
      select.dispatchEvent(new Event("change", { bubbles: true }));
      sync();

      var form = select.closest("form");
      var shouldSubmit = wrapper.dataset.submitOnClear === "true" || (form && form.dataset.autoSubmit === "true");
      if (shouldSubmit && form) {
        if (typeof form.requestSubmit === "function") {
          form.requestSubmit();
        } else {
          form.submit();
        }
      }
    });

    select.addEventListener("change", sync);
    sync();
  }

  function init() {
    document.querySelectorAll(".clearable-select").forEach(setup);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
