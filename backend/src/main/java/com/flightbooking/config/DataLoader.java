package com.flightbooking.config;

import com.flightbooking.model.Flight;
import com.flightbooking.model.User;
import com.flightbooking.repository.FlightRepository;
import com.flightbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Creates the default admin and some sample flights (dated from tomorrow) the first time the app runs. */
@Component
@ConditionalOnProperty(name = "skybook.seed.enabled", havingValue = "true")
public class DataLoader implements CommandLineRunner {

    private final UserRepository users;
    private final FlightRepository flights;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataLoader(UserRepository users, FlightRepository flights, PasswordEncoder encoder,
                      @Value("${skybook.admin.email}") String adminEmail,
                      @Value("${skybook.admin.password}") String adminPassword) {
        this.users = users;
        this.flights = flights;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmail(adminEmail)) {
            users.save(new User("Administrator", adminEmail, encoder.encode(adminPassword), "9876543210", "ADMIN"));
        }
        if (flights.count() == 0) {
            // one set of flights per day for the next 30 days, so a search always finds something
            for (int d = 0; d < 30; d++) {
                LocalDate day = LocalDate.now().plusDays(d + 1L);
                add("AI", 202 + d, "Air India", "Chennai", "Delhi", day, 6, 15, 165, 5450, 180);
                add("6E", 512 + d, "IndiGo", "Chennai", "Delhi", day, 9, 40, 175, 4890, 186);
                add("SG", 801 + d, "SpiceJet", "Chennai", "Mumbai", day, 7, 30, 120, 3650, 189);
                add("UK", 835 + d, "Vistara", "Delhi", "Chennai", day, 18, 5, 170, 5980, 158);
                add("6E", 231 + d, "IndiGo", "Bengaluru", "Chennai", day, 11, 0, 60, 2250, 186);
                add("AI", 540 + d, "Air India", "Mumbai", "Chennai", day, 20, 20, 115, 3890, 180);
            }
        }
    }

    /** Flight numbers are unique in the schema, so each day's flight gets its own number (AI-202, AI-203 ...). */
    private void add(String prefix, int number, String airline, String from, String to, LocalDate day,
                     int hour, int minute, int durationMinutes, int price, int seats) {
        LocalDateTime dep = LocalDateTime.of(day, LocalTime.of(hour, minute));
        Flight f = new Flight();
        f.setFlightNumber(prefix + "-" + number);
        f.setAirline(airline);
        f.setSource(from);
        f.setDestination(to);
        f.setDepartureTime(dep);
        f.setArrivalTime(dep.plusMinutes(durationMinutes));
        f.setPrice(BigDecimal.valueOf(price));
        f.setTotalSeats(seats);
        f.setAvailableSeats(seats);
        flights.save(f);
    }
}
