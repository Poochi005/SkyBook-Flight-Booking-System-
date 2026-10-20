/* My Bookings page: list the passenger's bookings and cancel confirmed ones. */
const fmt = iso => new Date(iso).toLocaleString([], { dateStyle: "medium", timeStyle: "short" });

async function loadBookings() {
  const body = document.getElementById("bookingRows");
  try {
    const list = await api("/bookings/my");
    if (list.length === 0) { body.innerHTML = "<tr><td colspan='8'>You have no bookings yet.</td></tr>"; return; }
    body.innerHTML = list.map(b => `
      <tr>
        <td><strong>${esc(b.pnr)}</strong></td>
        <td>${esc(b.airline)} ${esc(b.flightNumber)}</td>
        <td>${esc(b.source)} to ${esc(b.destination)}</td>
        <td>${fmt(b.departureTime)}</td>
        <td>${b.passengers.map(p => esc(p.name) + " (" + esc(p.seatNumber) + ")").join("<br>")}</td>
        <td>Rs. ${Number(b.totalAmount).toLocaleString("en-IN")}</td>
        <td><span class="badge ${b.status.toLowerCase()}">${esc(b.status)}</span></td>
        <td>${b.status === "CONFIRMED"
          ? `<button class="btn-danger" onclick="cancelBooking(${b.bookingId})">Cancel</button>`
          : "Refunded"}</td>
      </tr>`).join("");
  } catch (err) {
    body.innerHTML = "<tr><td colspan='8' class='error'>" + esc(err.message) + "</td></tr>";
  }
}

async function cancelBooking(id) {
  if (!confirm("Cancel this booking?")) return;
  try {
    await api("/bookings/" + id + "/cancel", { method: "PUT" });
    loadBookings();
  } catch (err) { alert(err.message); }
}

document.addEventListener("DOMContentLoaded", () => { if (requireLogin("PASSENGER")) loadBookings(); });
