package com.flightbooking.controller;

import com.flightbooking.model.Flight;
import com.flightbooking.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/search")
    public List<Flight> search(@RequestParam String source,
                               @RequestParam String destination,
                               @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        return flightService.search(source, destination, date);
    }

    @GetMapping // ADMIN only - full flight list for the admin panel
    public List<Flight> all() {
        return flightService.findAll();
    }

    @PostMapping // ADMIN only
    public ResponseEntity<Flight> add(@Valid @RequestBody Flight flight) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.save(flight));
    }

    @PutMapping("/{id}") // ADMIN only
    public Flight update(@PathVariable Long id, @Valid @RequestBody Flight flight) {
        return flightService.update(id, flight);
    }

    @DeleteMapping("/{id}") // ADMIN only
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        flightService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
