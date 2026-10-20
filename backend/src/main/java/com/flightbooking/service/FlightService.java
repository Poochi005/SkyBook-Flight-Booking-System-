package com.flightbooking.service;

import com.flightbooking.exception.BookingException;
import com.flightbooking.exception.ResourceNotFoundException;
import com.flightbooking.model.Flight;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;

    public FlightService(FlightRepository flightRepository, BookingRepository bookingRepository) {
        this.flightRepository = flightRepository;
        this.bookingRepository = bookingRepository;
    }

    /** A date is converted into the range [00:00, next day 00:00) so that flights at any time of the day are found. */
    @Transactional(readOnly = true)
    public List<Flight> search(String source, String destination, LocalDate date) {
        return flightRepository.searchFlights(source.trim(), destination.trim(),
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    @Transactional(readOnly = true)
    public List<Flight> findAll() {
        return flightRepository.findAllByOrderByDepartureTimeAsc();
    }

    @Transactional
    public Flight save(Flight flight) {
        validate(flight);
        String number = flight.getFlightNumber().trim().toUpperCase();
        if (flightRepository.existsByFlightNumber(number)) {
            // the schema keeps the flight number unique (as in the project report)
            throw new BookingException("Flight number " + number + " already exists");
        }
        flight.setFlightId(null);
        flight.setFlightNumber(number);
        flight.setAvailableSeats(flight.getTotalSeats());
        return flightRepository.save(flight);
    }

    @Transactional
    public Flight update(Long id, Flight in) {
        Flight f = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        validate(in);
        String number = in.getFlightNumber().trim().toUpperCase();
        if (!number.equalsIgnoreCase(f.getFlightNumber()) && flightRepository.existsByFlightNumber(number)) {
            throw new BookingException("Flight number " + number + " already exists");
        }
        int booked = f.getTotalSeats() - f.getAvailableSeats();
        if (in.getTotalSeats() < booked) {
            throw new BookingException("Total seats cannot be less than the " + booked + " seats already booked");
        }
        f.setFlightNumber(number);
        f.setAirline(in.getAirline());
        f.setSource(in.getSource());
        f.setDestination(in.getDestination());
        f.setDepartureTime(in.getDepartureTime());
        f.setArrivalTime(in.getArrivalTime());
        f.setPrice(in.getPrice());
        f.setTotalSeats(in.getTotalSeats());
        f.setAvailableSeats(in.getTotalSeats() - booked);
        return flightRepository.save(f);
    }

    @Transactional
    public void delete(Long id) {
        if (!flightRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flight not found");
        }
        if (bookingRepository.existsByFlightFlightId(id)) {
            throw new BookingException("This flight already has bookings and cannot be deleted");
        }
        flightRepository.deleteById(id);
    }

    private void validate(Flight f) {
        if (!f.getArrivalTime().isAfter(f.getDepartureTime())) {
            throw new BookingException("Arrival time must be after departure time");
        }
        if (f.getSource().trim().equalsIgnoreCase(f.getDestination().trim())) {
            throw new BookingException("Source and destination must be different");
        }
    }
}
