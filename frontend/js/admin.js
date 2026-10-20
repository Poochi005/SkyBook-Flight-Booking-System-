/* Admin panel: manage flights and see all bookings. */
let editingId = null;
let flightsCache = [];
const fmt = iso => new Date(iso).toLocaleString([], { dateStyle: "medium", timeStyle: "short" });
const $ = id => document.getElementById(id);

async function loadFlights() {
  try {
    flightsCache = await api("/flights");
    $("flightRows").innerHTML = flightsCache.length === 0 ? "<tr><td colspan='7'>No flights yet.</td></tr>" : flightsCache.map(f => `
      <tr>
        <td>${esc(f.flightNumber)}</td><td>${esc(f.airline)}</td>
        <td>${esc(f.source)} to ${esc(f.destination)}</td>
        <td>${fmt(f.departureTime)}</td>
        <td>Rs. ${Number(f.price).toLocaleString("en-IN")}</td>
        <td>${f.availableSeats} / ${f.totalSeats}</td>
        <td class="row-actions">
          <button class="btn-primary" onclick="editFlight(${f.flightId})">Edit</button>
          <button class="btn-danger" onclick="deleteFlight(${f.flightId})">Delete</button>
        </td>
      </tr>`).join("");
  } catch (err) { $("flightRows").innerHTML = "<tr><td colspan='7' class='error'>" + esc(err.message) + "</td></tr>"; }
}

async function loadAllBookings() {
  try {
    const list = await api("/bookings");
    $("allBookingRows").innerHTML = list.length === 0 ? "<tr><td colspan='8'>No bookings yet.</td></tr>" : list.map(b => `
      <tr>
        <td><strong>${esc(b.pnr)}</strong></td><td>${esc(b.passengerEmail)}</td>
        <td>${esc(b.flightNumber)}</td><td>${esc(b.source)} to ${esc(b.destination)}</td>
        <td>${b.passengerCount}</td><td>Rs. ${Number(b.totalAmount).toLocaleString("en-IN")}</td>
        <td>${esc(b.paymentMethod)}</td>
        <td><span class="badge ${b.status.toLowerCase()}">${esc(b.status)}</span></td>
      </tr>`).join("");
  } catch (err) { $("allBookingRows").innerHTML = "<tr><td colspan='8' class='error'>" + esc(err.message) + "</td></tr>"; }
}

function toLocalInput(iso) { return iso.slice(0, 16); }

function editFlight(id) {
  const f = flightsCache.find(x => x.flightId === id);
  editingId = id;
  $("flightNumber").value = f.flightNumber; $("airline").value = f.airline;
  $("source").value = f.source; $("destination").value = f.destination;
  $("departure").value = toLocalInput(f.departureTime); $("arrival").value = toLocalInput(f.arrivalTime);
  $("price").value = f.price; $("seats").value = f.totalSeats;
  $("saveBtn").textContent = "Update flight"; $("cancelEdit").style.display = "";
  $("formTitle").textContent = "Edit flight " + f.flightNumber;
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function resetForm() {
  editingId = null;
  $("flightForm").reset();
  $("saveBtn").textContent = "+ Add flight"; $("cancelEdit").style.display = "none";
  $("formTitle").textContent = "Add a flight";
}

async function deleteFlight(id) {
  if (!confirm("Delete this flight?")) return;
  try { await api("/flights/" + id, { method: "DELETE" }); loadFlights(); }
  catch (err) { alert(err.message); }
}

function showTab(name) {
  $("flightsTab").style.display = name === "flights" ? "" : "none";
  $("bookingsTab").style.display = name === "bookings" ? "" : "none";
  $("tabFlights").classList.toggle("active", name === "flights");
  $("tabBookings").classList.toggle("active", name === "bookings");
  if (name === "bookings") loadAllBookings();
}

document.addEventListener("DOMContentLoaded", () => {
  if (!requireLogin("ADMIN")) return;

  $("flightForm").addEventListener("submit", async e => {
    e.preventDefault();
    const seats = Number($("seats").value);
    const flight = {
      flightNumber: $("flightNumber").value.trim(), airline: $("airline").value.trim(),
      source: $("source").value.trim(), destination: $("destination").value.trim(),
      departureTime: $("departure").value, arrivalTime: $("arrival").value,
      price: Number($("price").value), totalSeats: seats, availableSeats: seats
    };
    try {
      await api(editingId ? "/flights/" + editingId : "/flights", {
        method: editingId ? "PUT" : "POST", body: JSON.stringify(flight)
      });
      resetForm(); loadFlights();
    } catch (err) { alert(err.message); }
  });
  $("cancelEdit").addEventListener("click", resetForm);
  $("tabFlights").addEventListener("click", () => showTab("flights"));
  $("tabBookings").addEventListener("click", () => showTab("bookings"));
  loadFlights();
});
