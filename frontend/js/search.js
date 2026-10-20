/* Flight search: the home page form sends the user to results.html, which loads and shows the flights. */
let foundFlights = [];

const fmtTime = iso => new Date(iso).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", hour12: false });
const fmtDate = iso => new Date(iso).toLocaleDateString([], { day: "2-digit", month: "short", year: "numeric" });
function duration(dep, arr) {
  const m = Math.round((new Date(arr) - new Date(dep)) / 60000);
  return Math.floor(m / 60) + "h " + String(m % 60).padStart(2, "0") + "m";
}

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("searchForm");
  const dateInput = document.getElementById("date");
  if (dateInput) dateInput.min = new Date().toISOString().slice(0, 10);

  if (form) {
    form.addEventListener("submit", e => {
      e.preventDefault();
      const q = new URLSearchParams({
        source: document.getElementById("source").value.trim(),
        destination: document.getElementById("destination").value.trim(),
        date: dateInput.value
      });
      location.href = "results.html?" + q.toString();
    });
  }

  if (document.getElementById("flightList")) loadResults();
});

async function loadResults() {
  const p = new URLSearchParams(location.search);
  const source = p.get("source"), destination = p.get("destination"), date = p.get("date");
  const box = document.getElementById("flightList");
  if (!source || !destination || !date) { box.innerHTML = "<p>Please search from the home page.</p>"; return; }

  document.getElementById("routeTitle").textContent = source + " to " + destination;
  document.getElementById("info").textContent = fmtDate(date + "T00:00:00");
  box.innerHTML = "<p class='muted'>Searching...</p>";
  try {
    foundFlights = await api("/flights/search?source=" + encodeURIComponent(source) +
      "&destination=" + encodeURIComponent(destination) + "&date=" + encodeURIComponent(date), { auth: false });
    box.innerHTML = foundFlights.length === 0 ? "<p class='card'>No flights found.</p>" : foundFlights.map(f => `
      <div class="flight-card">
        <div class="airline">
          <h3>${esc(f.airline)} (${esc(f.flightNumber)})</h3>
          <small>${esc(f.source)} to ${esc(f.destination)}</small>
        </div>
        <div class="timing">
          <strong>${fmtTime(f.departureTime)}</strong> &mdash; ${duration(f.departureTime, f.arrivalTime)} &mdash; <strong>${fmtTime(f.arrivalTime)}</strong>
          <small>${fmtDate(f.departureTime)}</small>
        </div>
        <div class="fare">
          <span class="price">Rs. ${Number(f.price).toLocaleString("en-IN")}</span>
          <small class="seats">${f.availableSeats} seats left</small>
          <button class="btn-primary" onclick="selectFlight(${f.flightId})">Book Now</button>
        </div>
      </div>`).join("");
  } catch (err) {
    box.innerHTML = "<p class='card error'>" + esc(err.message) + "</p>";
  }
}

function selectFlight(id) {
  const flight = foundFlights.find(f => f.flightId === id);
  if (!flight) return;
  const user = getUser();
  if (user && user.role === "ADMIN") { alert("Admin accounts cannot book tickets. Please use a passenger account."); return; }
  localStorage.setItem("flight", JSON.stringify(flight));
  if (!user) { location.href = "login.html?redirect=booking.html"; return; }
  location.href = "booking.html";
}
