package com.flightbooking.repository;

import com.flightbooking.model.Booking;
import com.flightbooking.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    int countByBooking(Booking booking);

    List<Passenger> findByBooking(Booking booking);

    /** Seats already taken on a flight by CONFIRMED bookings. */
    @Query("SELECT p.seatNumber FROM Passenger p WHERE p.booking.flight.flightId = :flightId " +
           "AND p.booking.status = 'CONFIRMED' AND UPPER(p.seatNumber) IN :seats")
    List<String> findTakenSeats(@Param("flightId") Long flightId, @Param("seats") Collection<String> seats);
}
