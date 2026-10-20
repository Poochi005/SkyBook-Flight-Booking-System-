package com.flightbooking.repository;

import com.flightbooking.model.Flight;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT f FROM Flight f WHERE LOWER(f.source) = LOWER(:source) " +
           "AND LOWER(f.destination) = LOWER(:destination) " +
           "AND f.departureTime >= :start AND f.departureTime < :end " +
           "AND f.availableSeats > 0 ORDER BY f.departureTime")
    List<Flight> searchFlights(@Param("source") String source,
                               @Param("destination") String destination,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    boolean existsByFlightNumber(String flightNumber);

    /** Row lock so two users cannot book the last seats at the same moment. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flight f WHERE f.flightId = :id")
    Optional<Flight> findByIdForUpdate(@Param("id") Long id);

    List<Flight> findAllByOrderByDepartureTimeAsc();
}
