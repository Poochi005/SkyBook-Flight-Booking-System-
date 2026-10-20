package com.flightbooking.service;

import com.flightbooking.dto.BookingRequest;
import com.flightbooking.dto.BookingResponse;
import com.flightbooking.dto.PassengerDto;
import com.flightbooking.exception.BookingException;
import com.flightbooking.exception.ResourceNotFoundException;
import com.flightbooking.model.*;
import com.flightbooking.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final String PNR_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0, O, 1, I
    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final PaymentRepository paymentRepository;

    public BookingService(UserRepository userRepository, FlightRepository flightRepository,
                          BookingRepository bookingRepository, PassengerRepository passengerRepository,
                          PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.flightRepository = flightRepository;
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * The core logic. Every step runs inside ONE transaction: if anything fails the whole
     * operation is rolled back, so no seat is lost and no partial record is saved.
     */
    @Transactional
    public BookingResponse bookTicket(BookingRequest req, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // lock the flight row until the transaction ends -> no double booking
        Flight flight = flightRepository.findByIdForUpdate(req.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        if (!flight.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new BookingException("This flight has already departed");
        }

        int count = req.getPassengers().size();
        if (flight.getAvailableSeats() < count) {
            throw new BookingException("Only " + flight.getAvailableSeats() + " seats left");
        }

        // the same seat cannot be picked twice in one booking or by an earlier confirmed booking
        List<String> seats = req.getPassengers().stream()
                .map(p -> p.getSeatNumber().trim().toUpperCase(Locale.ROOT)).toList();
        if (Set.copyOf(seats).size() != seats.size()) {
            throw new BookingException("Each passenger must have a different seat");
        }
        List<String> taken = passengerRepository.findTakenSeats(flight.getFlightId(), seats);
        if (!taken.isEmpty()) {
            throw new BookingException("Seat already booked: " + String.join(", ", taken));
        }

        Booking booking = new Booking();
        booking.setPnr(generatePnr());
        booking.setUser(user);
        booking.setFlight(flight);
        booking.setStatus("CONFIRMED");
        booking.setBookingDate(LocalDateTime.now());
        booking.setTotalAmount(flight.getPrice().multiply(BigDecimal.valueOf(count)));
        booking = bookingRepository.save(booking);

        for (PassengerDto p : req.getPassengers()) {
            passengerRepository.save(new Passenger(booking, p.getName().trim(), p.getAge(),
                    p.getGender(), p.getSeatNumber().trim().toUpperCase(Locale.ROOT)));
        }

        // simulated payment - a real gateway (Razorpay / Stripe) would be called here
        Payment payment = paymentRepository.save(
                new Payment(booking, booking.getTotalAmount(), req.getPaymentMethod(), "SUCCESS"));

        flight.setAvailableSeats(flight.getAvailableSeats() - count);
        flightRepository.save(flight);

        return toResponse(booking, payment);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String email) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!b.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("Not your booking");
        }
        if ("CANCELLED".equals(b.getStatus())) {
            throw new BookingException("Booking already cancelled");
        }

        Flight f = flightRepository.findByIdForUpdate(b.getFlight().getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        if (!f.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new BookingException("The flight has already departed and cannot be cancelled");
        }

        int seats = passengerRepository.countByBooking(b);
        f.setAvailableSeats(f.getAvailableSeats() + seats);   // release the seats
        flightRepository.save(f);

        b.setStatus("CANCELLED");
        Payment payment = paymentRepository.findByBooking(b);
        if (payment != null) {
            payment.setStatus("REFUNDED");
        }
        bookingRepository.save(b);
        return toResponse(b, payment);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings(String email) {
        return bookingRepository.findByUserEmailOrderByBookingDateDesc(email).stream()
                .map(b -> toResponse(b, paymentRepository.findByBooking(b)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> allBookings() {
        return bookingRepository.findAllByOrderByBookingDateDesc().stream()
                .map(b -> toResponse(b, paymentRepository.findByBooking(b)))
                .collect(Collectors.toList());
    }

    private BookingResponse toResponse(Booking b, Payment pay) {
        return new BookingResponse(b, passengerRepository.findByBooking(b),
                pay == null ? null : pay.getMethod(), pay == null ? null : pay.getStatus());
    }

    /** 6-character PNR, regenerated until it is unique in the database. */
    private String generatePnr() {
        StringBuilder sb = new StringBuilder();
        do {
            sb.setLength(0);
            for (int i = 0; i < 6; i++) {
                sb.append(PNR_CHARS.charAt(random.nextInt(PNR_CHARS.length())));
            }
        } while (bookingRepository.existsByPnr(sb.toString()));
        return sb.toString();
    }
}
