(function () {
  const STORAGE_KEY = "calc.auth";
  const USER_KEY = "calc.user";

  function authHeader() {
    const token = sessionStorage.getItem(STORAGE_KEY);
    return token ? { Authorization: "Basic " + token } : {};
  }

  async function api(method, path, body) {
    const headers = Object.assign({ Accept: "application/json" }, authHeader());
    if (body !== undefined) {
      headers["Content-Type"] = "application/json";
    }
    const response = await fetch(path, {
      method: method,
      headers: headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    });
    let payload = null;
    const text = await response.text();
    if (text) {
      try {
        payload = JSON.parse(text);
      } catch (e) {
        payload = { message: text };
      }
    }
    return { status: response.status, body: payload };
  }

  function show(element, text) {
    element.textContent = text;
    element.hidden = false;
  }

  function hide(element) {
    element.hidden = true;
    element.textContent = "";
  }

  function initLogin() {
    const form = document.getElementById("login-form");
    const error = document.getElementById("login-error");
    form.addEventListener("submit", async function (event) {
      event.preventDefault();
      hide(error);
      const username = document.getElementById("username").value.trim();
      const password = document.getElementById("password").value;
      if (!username || !password) {
        show(error, "Username and password are required");
        return;
      }
      const token = btoa(username + ":" + password);
      const response = await fetch("/api/me", { headers: { Authorization: "Basic " + token } });
      if (response.status === 200) {
        sessionStorage.setItem(STORAGE_KEY, token);
        sessionStorage.setItem(USER_KEY, username);
        window.location.href = "/calculator.html";
      } else if (response.status === 401) {
        show(error, "Invalid username or password");
      } else {
        show(error, "Unexpected error (" + response.status + ")");
      }
    });
  }

  function renderOperations(items) {
    const body = document.getElementById("operations-body");
    const empty = document.getElementById("operations-empty");
    body.innerHTML = "";
    empty.hidden = items.length > 0;
    items.forEach(function (operation) {
      const row = document.createElement("tr");
      row.dataset.testid = "operation-row";
      row.dataset.id = operation.id;
      const resultText = operation.status === "DONE" ? String(operation.result)
        : operation.status === "FAILED" ? (operation.error ? operation.error.code : "FAILED")
        : "";
      row.innerHTML =
        '<td><code class="op-id">' + operation.id + '</code></td>' +
        '<td class="op-equation"></td>' +
        '<td class="op-priority">' + operation.priority + '</td>' +
        '<td class="op-status">' + operation.status + '</td>' +
        '<td class="op-result">' + resultText + '</td>' +
        '<td><button type="button" class="small delete-button" data-testid="delete-button">Delete</button></td>';
      row.querySelector(".op-equation").textContent = operation.equation;
      row.querySelector(".delete-button").addEventListener("click", async function () {
        await api("DELETE", "/api/operations/" + operation.id);
        await loadOperations();
      });
      body.appendChild(row);
    });
  }

  async function loadOperations() {
    const response = await api("GET", "/api/operations?page=0&size=20");
    if (response.status === 200) {
      renderOperations(response.body.items);
    } else if (response.status === 401) {
      window.location.href = "/";
    }
  }

  function initCalculator() {
    if (!sessionStorage.getItem(STORAGE_KEY)) {
      window.location.href = "/";
      return;
    }
    document.getElementById("current-user").textContent = sessionStorage.getItem(USER_KEY) || "";

    document.getElementById("logout-button").addEventListener("click", function () {
      window.location.href = "/";
    });

    const submitForm = document.getElementById("submit-form");
    const submitError = document.getElementById("submit-error");
    const submitResult = document.getElementById("submit-result");
    submitForm.addEventListener("submit", async function (event) {
      event.preventDefault();
      hide(submitError);
      submitResult.hidden = true;
      const equation = document.getElementById("equation").value;
      const priority = document.getElementById("priority").value;
      const response = await api("POST", "/api/operations", { equation: equation, priority: priority });
      if (response.status === 200 || response.status === 201) {
        document.getElementById("submitted-id").textContent = response.body.id;
        document.getElementById("operation-id").value = response.body.id;
        submitResult.hidden = false;
        await loadOperations();
      } else if (response.status === 401) {
        window.location.href = "/";
      } else {
        show(submitError, (response.body && response.body.message) || "Request failed (" + response.status + ")");
      }
    });

    const resultForm = document.getElementById("result-form");
    const resultError = document.getElementById("result-error");
    const resultBox = document.getElementById("result-box");
    resultForm.addEventListener("submit", async function (event) {
      event.preventDefault();
      hide(resultError);
      resultBox.hidden = true;
      const id = document.getElementById("operation-id").value.trim();
      if (!id) {
        show(resultError, "Operation id is required");
        return;
      }
      const response = await api("GET", "/api/operations/" + encodeURIComponent(id));
      if (response.status === 200 || response.status === 202) {
        const operation = response.body;
        document.getElementById("result-status").textContent = operation.status;
        const value = operation.status === "FAILED"
          ? (operation.error ? operation.error.code + ": " + operation.error.message : "FAILED")
          : operation.result;
        document.getElementById("result-value").textContent = value;
        resultBox.hidden = false;
      } else if (response.status === 401) {
        window.location.href = "/";
      } else {
        show(resultError, (response.body && response.body.message) || "Request failed (" + response.status + ")");
      }
    });

    document.getElementById("refresh-button").addEventListener("click", loadOperations);
    loadOperations();
  }

  document.addEventListener("DOMContentLoaded", function () {
    if (document.getElementById("login-form")) {
      initLogin();
    } else if (document.getElementById("submit-form")) {
      initCalculator();
    }
  });
})();
