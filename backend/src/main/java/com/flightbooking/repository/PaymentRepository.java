package com.flightbooking.repository;

import com.flightbooking.model.Booking;
import com.flightbooking.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByBooking(Booking booking);
}
