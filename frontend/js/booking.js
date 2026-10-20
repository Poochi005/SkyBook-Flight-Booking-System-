/* Passenger details + payment page. */
const flight = JSON.parse(localStorage.getItem("flight") || "null");
const MAX_PASSENGERS = 9;

function passengerRow(n) {
  return `
    <fieldset class="card passenger">
      <legend>Passenger ${n}</legend>
      <input type="text" class="p-name" placeholder="Name" maxlength="100" required>
      <input type="number" class="p-age" placeholder="Age" min="1" max="120" required>
      <select class="p-gender" required>
        <option value="">Gender</option><option>Male</option><option>Female</option><option>Other</option>
      </select>
      <input type="text" class="p-seat" placeholder="Seat (e.g. 12A)" pattern="[0-9]{1,2}[A-Za-z]" title="Seat like 12A" maxlength="3" required>
      ${n > 1 ? '<button type="button" class="btn-outline remove">Remove</button>' : ""}
    </fieldset>`;
}

function collectPassengers() {
  return [...document.querySelectorAll("#passengerList .passenger")].map(row => ({
    name: row.querySelector(".p-name").value.trim(),
    age: Number(row.querySelector(".p-age").value),
    gender: row.querySelector(".p-gender").value,
    seatNumber: row.querySelector(".p-seat").value.trim().toUpperCase()
  }));
}

function renumber() {
  document.querySelectorAll("#passengerList .passenger").forEach((row, i) => row.querySelector("legend").textContent = "Passenger " + (i + 1));
  updateTotal();
}

function updateTotal() {
  const n = document.querySelectorAll("#passengerList .passenger").length;
  document.getElementById("count").textContent = n;
  document.getElementById("total").textContent = "Rs. " + (flight.price * n).toLocaleString("en-IN");
}

document.addEventListener("DOMContentLoaded", () => {
  const user = requireLogin("PASSENGER");
  if (!user) return;
  if (!flight) { location.href = "index.html"; return; }

  document.getElementById("flightInfo").innerHTML =
    `<strong>${esc(flight.airline)} (${esc(flight.flightNumber)})</strong><br>${esc(flight.source)} to ${esc(flight.destination)}<br>` +
    `${new Date(flight.departureTime).toLocaleString([], { dateStyle: "medium", timeStyle: "short" })}`;
  document.getElementById("fare").textContent = "Rs. " + Number(flight.price).toLocaleString("en-IN");

  const list = document.getElementById("passengerList");
  list.innerHTML = passengerRow(1);
  updateTotal();

  document.getElementById("addPassenger").addEventListener("click", () => {
    const n = list.querySelectorAll(".passenger").length;
    if (n >= MAX_PASSENGERS || n >= flight.availableSeats) { alert("No more passengers can be added."); return; }
    list.insertAdjacentHTML("beforeend", passengerRow(n + 1));
    renumber();
  });
  list.addEventListener("click", e => {
    if (e.target.classList.contains("remove")) { e.target.closest(".passenger").remove(); renumber(); }
  });
});

async function confirmBooking() {
  const form = document.getElementById("bookingForm");
  if (!form.reportValidity()) return;
  const seats = collectPassengers().map(p => p.seatNumber);
  if (new Set(seats).size !== seats.length) { alert("Each passenger must have a different seat."); return; }

  const btn = document.getElementById("payBtn");
  btn.disabled = true;
  try {
    const data = await api("/bookings", {
      method: "POST",
      body: JSON.stringify({
        flightId: flight.flightId,
        paymentMethod: document.querySelector("input[name=method]:checked").value,
        passengers: collectPassengers()
      })
    });
    showTicket(data);
  } catch (err) {
    alert(err.message);
    btn.disabled = false;
  }
}

function showTicket(b) {
  localStorage.removeItem("flight");
  document.getElementById("bookingArea").style.display = "none";
  document.getElementById("ticket").style.display = "block";
  document.getElementById("pnr").textContent = b.pnr;
  document.getElementById("ticketFlight").textContent =
    `${b.airline} ${b.flightNumber} | ${b.source} to ${b.destination} | ${new Date(b.departureTime).toLocaleString([], { dateStyle: "medium", timeStyle: "short" })}`;
  document.getElementById("ticketAmount").textContent =
    `Paid Rs. ${Number(b.totalAmount).toLocaleString("en-IN")} via ${b.paymentMethod}`;
  document.getElementById("ticketRows").innerHTML = b.passengers.map(p =>
    `<tr><td>${esc(p.name)}</td><td>${p.age}</td><td>${esc(p.gender)}</td><td>${esc(p.seatNumber)}</td></tr>`).join("");
}
