package com.flightbooking.dto;

import com.flightbooking.model.Booking;
import com.flightbooking.model.Flight;
import com.flightbooking.model.Passenger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Flat response object - avoids lazy-loading problems and infinite JSON loops. */
public class BookingResponse {
    private final Long bookingId;
    private final String pnr;
    private final String status;
    private final LocalDateTime bookingDate;
    private final BigDecimal totalAmount;
    private final String paymentMethod;
    private final String paymentStatus;
    private final String passengerEmail;
    private final String flightNumber;
    private final String airline;
    private final String source;
    private final String destination;
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
    private final int passengerCount;
    private final List<PassengerDto> passengers;

    public BookingResponse(Booking b, List<Passenger> ps, String paymentMethod, String paymentStatus) {
        Flight f = b.getFlight();
        this.bookingId = b.getBookingId();
        this.pnr = b.getPnr();
        this.status = b.getStatus();
        this.bookingDate = b.getBookingDate();
        this.totalAmount = b.getTotalAmount();
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.passengerEmail = b.getUser().getEmail();
        this.flightNumber = f.getFlightNumber();
        this.airline = f.getAirline();
        this.source = f.getSource();
        this.destination = f.getDestination();
        this.departureTime = f.getDepartureTime();
        this.arrivalTime = f.getArrivalTime();
        this.passengerCount = ps.size();
        this.passengers = ps.stream()
                .map(p -> new PassengerDto(p.getName(), p.getAge(), p.getGender(), p.getSeatNumber()))
                .toList();
    }

    public Long getBookingId() { return bookingId; }
    public String getPnr() { return pnr; }
    public String getStatus() { return status; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPassengerEmail() { return passengerEmail; }
    public String getFlightNumber() { return flightNumber; }
    public String getAirline() { return airline; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public int getPassengerCount() { return passengerCount; }
    public List<PassengerDto> getPassengers() { return passengers; }
}
