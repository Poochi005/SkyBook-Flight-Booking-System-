/* Shared helpers: API address, login state, navigation bar, login and register forms. */
// Local default matches the current Windows setup (8080 is occupied by Tomcat9).
// For deployment, replace this with your Railway URL, e.g. https://your-service.up.railway.app/api
const API = (window.SKYBOOK_API_BASE || "http://localhost:8081/api").replace(/\\/$/, "");

/* Escape text before putting it in HTML (prevents HTML/script injection). */
function esc(v) {
  return String(v ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

function getUser() {
  try { return JSON.parse(localStorage.getItem("user")); } catch (e) { return null; }
}

function authHeader() {
  return { "Authorization": "Basic " + localStorage.getItem("auth") };
}

function saveLogin(user, email, password) {
  localStorage.setItem("user", JSON.stringify(user));
  localStorage.setItem("auth", btoa(unescape(encodeURIComponent(email + ":" + password))));
}

function logout() {
  localStorage.removeItem("user");
  localStorage.removeItem("auth");
  localStorage.removeItem("flight");
  location.href = "index.html";
}

/* Call from pages that need a logged-in user (optionally of one role). */
function requireLogin(role) {
  const user = getUser();
  if (!user) {
    location.href = "login.html?redirect=" + encodeURIComponent(location.pathname.split("/").pop() + location.search);
    return null;
  }
  if (role && user.role !== role) {
    alert("This page is only for " + role.toLowerCase() + " accounts.");
    location.href = "index.html";
    return null;
  }
  return user;
}

/* fetch() wrapper that sends the login and turns error responses into Error objects. */
async function api(path, options = {}) {
  const headers = Object.assign({ "Content-Type": "application/json" }, options.auth === false ? {} : authHeader(), options.headers);
  let res;
  try {
    res = await fetch(API + path, Object.assign({}, options, { headers }));
  } catch (e) {
    throw new Error("Cannot reach the server. Is the Spring Boot application running on port 8081?");
  }
  if (res.status === 401 && options.auth !== false) {
    localStorage.removeItem("user"); localStorage.removeItem("auth");
    throw new Error("Session expired. Please log in again.");
  }
  let data = null;
  if (res.status !== 204) { try { data = await res.json(); } catch (e) { data = null; } }
  if (!res.ok) throw new Error((data && data.error) || "Request failed (" + res.status + ")");
  return data;
}

/* Show the user name / logout button instead of the login and register links. */
function updateNav() {
  const user = getUser();
  const $ = id => document.getElementById(id);
  if (!$("userBox")) return;
  if (user) {
    if ($("loginLink")) $("loginLink").style.display = "none";
    if ($("registerLink")) $("registerLink").style.display = "none";
    $("userBox").style.display = "flex";
    $("userName").textContent = user.name + (user.role === "ADMIN" ? " (Admin)" : "");
    if ($("adminLink")) $("adminLink").style.display = user.role === "ADMIN" ? "" : "none";
    if ($("bookingsLink")) $("bookingsLink").style.display = user.role === "ADMIN" ? "none" : "";
    $("logoutBtn").addEventListener("click", logout);
  } else if ($("adminLink")) {
    $("adminLink").style.display = "none";
  }
}

document.addEventListener("DOMContentLoaded", () => {
  updateNav();

  const loginForm = document.getElementById("loginForm");
  if (loginForm) {
    loginForm.addEventListener("submit", async e => {
      e.preventDefault();
      const msg = document.getElementById("message");
      msg.textContent = "";
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;
      try {
        const user = await api("/auth/login", { method: "POST", auth: false, body: JSON.stringify({ email, password }) });
        saveLogin(user, user.email, password);
        const redirect = new URLSearchParams(location.search).get("redirect");
        location.href = user.role === "ADMIN" ? "admin.html" : (redirect || "index.html");
      } catch (err) { msg.textContent = err.message; }
    });
  }

  const registerForm = document.getElementById("registerForm");
  if (registerForm) {
    registerForm.addEventListener("submit", async e => {
      e.preventDefault();
      const msg = document.getElementById("message");
      msg.className = "error"; msg.textContent = "";
      const body = {
        name: document.getElementById("name").value.trim(),
        email: document.getElementById("email").value.trim(),
        phone: document.getElementById("phone").value.trim(),
        password: document.getElementById("password").value
      };
      try {
        await api("/auth/register", { method: "POST", auth: false, body: JSON.stringify(body) });
        msg.className = "success";
        msg.textContent = "Account created. Redirecting to login...";
        setTimeout(() => location.href = "login.html", 1200);
      } catch (err) { msg.textContent = err.message; }
    });
  }
});
