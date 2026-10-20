package com.flightbooking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightbooking.model.Flight;
import com.flightbooking.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End-to-end test of the main report test cases: register, login, search, book, cancel, roles. */
@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired FlightRepository flights;

    private String basic(String email, String pw) {
        return "Basic " + Base64.getEncoder().encodeToString((email + ":" + pw).getBytes());
    }

    private MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder b, String body) {
        return b.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void fullPassengerJourney() throws Exception {
        // 1. register (+ duplicate e-mail rejected)
        String reg = "{\"name\":\"Martin\",\"email\":\"martin@test.com\",\"phone\":\"9876543210\",\"password\":\"secret1\"}";
        mvc.perform(withJson(post("/api/auth/register"), reg)).andExpect(status().isCreated());
        mvc.perform(withJson(post("/api/auth/register"), reg)).andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.error").value("E-mail already registered"));

        // 2. login ok / wrong password
        mvc.perform(withJson(post("/api/auth/login"), "{\"email\":\"martin@test.com\",\"password\":\"secret1\"}"))
           .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("PASSENGER"));
        mvc.perform(withJson(post("/api/auth/login"), "{\"email\":\"martin@test.com\",\"password\":\"bad\"}"))
           .andExpect(status().isUnauthorized());

        // 3. search
        String date = LocalDate.now().plusDays(1).toString();
        MvcResult res = mvc.perform(get("/api/flights/search")
                .param("source", "chennai").param("destination", "DELHI").param("date", date))
                .andExpect(status().isOk()).andReturn();
        JsonNode list = json.readTree(res.getResponse().getContentAsString());
        assertTrue(list.size() >= 2);
        long flightId = list.get(0).get("flightId").asLong();
        int seatsBefore = list.get(0).get("availableSeats").asInt();
        mvc.perform(get("/api/flights/search").param("source", "Chennai").param("destination", "Nowhere").param("date", date))
           .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));

        String auth = basic("martin@test.com", "secret1");

        // 4. book 2 passengers
        String book = "{\"flightId\":" + flightId + ",\"paymentMethod\":\"UPI\",\"passengers\":["
                + "{\"name\":\"A\",\"age\":30,\"gender\":\"Male\",\"seatNumber\":\"12A\"},"
                + "{\"name\":\"B\",\"age\":28,\"gender\":\"Female\",\"seatNumber\":\"12B\"}]}";
        MvcResult booked = mvc.perform(withJson(post("/api/bookings"), book).header("Authorization", auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pnr").isString())
                .andExpect(jsonPath("$.passengerCount").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED")).andReturn();
        JsonNode b = json.readTree(booked.getResponse().getContentAsString());
        assertEquals(6, b.get("pnr").asText().length());
        assertEquals(seatsBefore - 2, flights.findById(flightId).orElseThrow().getAvailableSeats());

        // same seat again -> rejected, nothing changes
        mvc.perform(withJson(post("/api/bookings"), book).header("Authorization", auth))
           .andExpect(status().isBadRequest());
        assertEquals(seatsBefore - 2, flights.findById(flightId).orElseThrow().getAvailableSeats());

        // 5. booking history
        mvc.perform(get("/api/bookings/my").header("Authorization", auth))
           .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));

        // 6. cancel -> seats restored; cancel again -> error
        long bookingId = b.get("bookingId").asLong();
        mvc.perform(put("/api/bookings/" + bookingId + "/cancel").header("Authorization", auth))
           .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"))
           .andExpect(jsonPath("$.paymentStatus").value("REFUNDED"));
        assertEquals(seatsBefore, flights.findById(flightId).orElseThrow().getAvailableSeats());
        mvc.perform(put("/api/bookings/" + bookingId + "/cancel").header("Authorization", auth))
           .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Booking already cancelled"));

        // 7. booking more seats than available -> error with seats left
        Flight f = flights.findById(flightId).orElseThrow();
        f.setAvailableSeats(1);
        flights.save(f);
        mvc.perform(withJson(post("/api/bookings"), book).header("Authorization", auth))
           .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Only 1 seats left"));

        // 8. another passenger cannot cancel this booking's owner's ticket
        mvc.perform(withJson(post("/api/auth/register"),
                "{\"name\":\"Other\",\"email\":\"other@test.com\",\"phone\":\"9876543211\",\"password\":\"secret2\"}"));
        mvc.perform(put("/api/bookings/" + bookingId + "/cancel").header("Authorization", basic("other@test.com", "secret2")))
           .andExpect(status().isForbidden());
    }

    @Test
    void rolesAreEnforced() throws Exception {
        String flight = "{\"flightNumber\":\"ZZ-1\",\"airline\":\"Test Air\",\"source\":\"A\",\"destination\":\"B\","
                + "\"departureTime\":\"2030-01-01T10:00:00\",\"arrivalTime\":\"2030-01-01T12:00:00\","
                + "\"price\":1000,\"totalSeats\":50,\"availableSeats\":50}";

        mvc.perform(withJson(post("/api/auth/register"),
                "{\"name\":\"Pax\",\"email\":\"pax@test.com\",\"phone\":\"9876543212\",\"password\":\"secret3\"}"));

        // no login -> 401, passenger -> 403, admin -> 201
        mvc.perform(withJson(post("/api/flights"), flight)).andExpect(status().isUnauthorized());
        mvc.perform(withJson(post("/api/flights"), flight).header("Authorization", basic("pax@test.com", "secret3")))
           .andExpect(status().isForbidden());
        String admin = basic("admin@skybook.com", "admin123");
        mvc.perform(withJson(post("/api/flights"), flight).header("Authorization", admin))
           .andExpect(status().isCreated());

        // validation error -> clear message
        mvc.perform(withJson(post("/api/flights"), "{\"flightNumber\":\"\"}").header("Authorization", admin))
           .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").isString());

        // admin cannot book, admin can list all bookings
        mvc.perform(get("/api/bookings").header("Authorization", admin)).andExpect(status().isOk());
        mvc.perform(get("/api/bookings/my").header("Authorization", admin)).andExpect(status().isForbidden());
    }
}
