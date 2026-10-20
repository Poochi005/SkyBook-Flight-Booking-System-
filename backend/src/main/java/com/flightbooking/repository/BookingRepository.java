package com.flightbooking.repository;

import com.flightbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByPnr(String pnr);
    List<Booking> findByUserEmailOrderByBookingDateDesc(String email);
    List<Booking> findAllByOrderByBookingDateDesc();
    boolean existsByFlightFlightId(Long flightId);
}
