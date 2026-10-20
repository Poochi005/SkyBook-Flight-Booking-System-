package com.flightbooking.controller;

import com.flightbooking.dto.BookingRequest;
import com.flightbooking.dto.BookingResponse;
import com.flightbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping // PASSENGER
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody BookingRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookTicket(req, auth.getName()));
    }

    @GetMapping("/my") // PASSENGER
    public List<BookingResponse> mine(Authentication auth) {
        return bookingService.myBookings(auth.getName());
    }

    @PutMapping("/{id}/cancel") // PASSENGER
    public BookingResponse cancel(@PathVariable Long id, Authentication auth) {
        return bookingService.cancelBooking(id, auth.getName());
    }

    @GetMapping // ADMIN
    public List<BookingResponse> all() {
        return bookingService.allBookings();
    }
}
